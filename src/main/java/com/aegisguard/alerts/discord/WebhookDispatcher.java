package com.aegisguard.alerts.discord;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * HTTP webhook dispatcher using Java's built-in HttpClient.
 * Handles retries with exponential backoff.
 */
public final class WebhookDispatcher {

    private final HttpClient httpClient;
    private final Logger logger;
    private final WebhookRateLimiter rateLimiter;
    private final int maxRetries;
    private final long retryDelayMs;
    private final boolean exponentialBackoff;

    public WebhookDispatcher(Logger logger, WebhookRateLimiter rateLimiter,
                             int maxRetries, long retryDelayMs, boolean exponentialBackoff) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.logger = logger;
        this.rateLimiter = rateLimiter;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.exponentialBackoff = exponentialBackoff;
    }

    /**
     * Send a webhook payload synchronously (call from async thread).
     */
    public boolean send(String url, String jsonPayload) {
        return send(url, jsonPayload, 0);
    }

    private boolean send(String url, String jsonPayload, int attempt) {
        if (!rateLimiter.tryAcquire()) {
            long waitTime = rateLimiter.getWaitTimeMs();
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            if (!rateLimiter.tryAcquire()) return false;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status >= 200 && status < 300) {
                return true;
            }

            if (status == 429 || status >= 500) {
                // Rate limited or server error — retry
                if (attempt < maxRetries) {
                    long delay = exponentialBackoff ? retryDelayMs * (long) Math.pow(2, attempt) : retryDelayMs;
                    Thread.sleep(delay);
                    return send(url, jsonPayload, attempt + 1);
                }
            }

            logger.warning("Webhook failed with status " + status + ": " + response.body());
            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            logger.warning("Webhook dispatch error: " + e.getMessage());
            if (attempt < maxRetries) {
                try {
                    long delay = exponentialBackoff ? retryDelayMs * (long) Math.pow(2, attempt) : retryDelayMs;
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                return send(url, jsonPayload, attempt + 1);
            }
            return false;
        }
    }

    /**
     * Mask a webhook URL for safe logging.
     */
    public static String maskUrl(String url) {
        if (url == null || url.length() < 20) return "***";
        return url.substring(0, 40) + "..." + url.substring(url.length() - 10);
    }
}
