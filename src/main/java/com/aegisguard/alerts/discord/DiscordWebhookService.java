package com.aegisguard.alerts.discord;

import com.aegisguard.alerts.AlertLevel;
import com.aegisguard.config.DiscordConfig;
import com.aegisguard.scheduler.TaskScheduler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Enterprise Discord webhook service orchestrating all webhook operations.
 * Manages dispatch workers, rate limiting, queuing, and aggregation.
 */
public final class DiscordWebhookService {

    private final DiscordConfig config;
    private final TaskScheduler scheduler;
    private final Logger logger;
    private final Gson gson = new GsonBuilder().create();

    private WebhookDispatcher dispatcher;
    private WebhookQueue queue;
    private WebhookRateLimiter rateLimiter;
    private WebhookTemplates templates;
    private ExecutorService workerPool;
    private volatile boolean running;

    public DiscordWebhookService(DiscordConfig config, TaskScheduler scheduler, Logger logger) {
        this.config = config;
        this.scheduler = scheduler;
        this.logger = logger;
    }

    /**
     * Initialize the webhook service.
     */
    public void initialize() {
        if (!config.isEnabled()) {
            logger.info("Discord webhooks disabled.");
            return;
        }

        this.rateLimiter = new WebhookRateLimiter(config.getTokensPerSecond(), config.getBurstCapacity());
        this.queue = new WebhookQueue(config.getQueueCapacity());
        this.dispatcher = new WebhookDispatcher(logger, rateLimiter,
                config.getMaxRetries(), config.getRetryDelayMs(), config.isExponentialBackoff());

        this.templates = new WebhookTemplates(
                config.getFooterText(), config.getFooterIcon(), config.getAvatarUrl(),
                config.getColor("info"), config.getColor("warn"),
                config.getColor("danger"), config.getColor("success"),
                config.getColor("critical")
        );

        // Start worker threads
        int workers = config.getWorkers();
        this.workerPool = Executors.newFixedThreadPool(workers, r -> {
            Thread t = new Thread(r, "AegisGuard-Webhook");
            t.setDaemon(true);
            return t;
        });

        this.running = true;
        for (int i = 0; i < workers; i++) {
            workerPool.submit(this::workerLoop);
        }

        logger.info("Discord webhook service initialized with " + workers + " workers.");
    }

    private void workerLoop() {
        while (running) {
            try {
                WebhookQueue.WebhookMessage msg = queue.poll(1000);
                if (msg != null) {
                    boolean success = dispatcher.send(msg.webhookUrl(), msg.payload());
                    if (!success && msg.retryCount() < config.getMaxRetries()) {
                        queue.enqueue(new WebhookQueue.WebhookMessage(
                                msg.webhookUrl(), msg.payload(), msg.type(), msg.retryCount() + 1));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.warning("Webhook worker error: " + e.getMessage());
            }
        }
    }

    /**
     * Send an alert webhook.
     */
    public void sendAlert(String player, String uuid, String platform, String world, String xyz,
                          int ping, double tps, String check, String category, double vl,
                          double trust, String summary, AlertLevel level) {
        if (!config.isEnabled() || !config.isWebhookEnabled("alerts")) return;
        String url = config.getWebhookUrl("alerts");
        if (url.isEmpty()) return;

        var embed = templates.alertEmbed(player, uuid, platform, world, xyz, ping, tps,
                check, category, vl, trust, summary, level);
        String payload = buildPayload(embed);
        queue.enqueue(new WebhookQueue.WebhookMessage(url, payload, "alerts", 0));
    }

    /**
     * Send a punishment webhook.
     */
    public void sendPunishment(String player, String action, String reason, String triggerChecks,
                               boolean automatic, String duration, String evidenceId) {
        if (!config.isEnabled() || !config.isWebhookEnabled("punishments")) return;
        String url = config.getWebhookUrl("punishments");
        if (url.isEmpty()) return;

        var embed = templates.punishmentEmbed(player, action, reason, triggerChecks, automatic, duration, evidenceId);
        queue.enqueue(new WebhookQueue.WebhookMessage(url, buildPayload(embed), "punishments", 0));
    }

    /**
     * Send a lifecycle startup webhook.
     */
    public void sendLifecycleStartup(String version, String dbType, int checkCount, String mcVersion, long startupMs) {
        if (!config.isEnabled() || !config.isWebhookEnabled("lifecycle")) return;
        String url = config.getWebhookUrl("lifecycle");
        if (url.isEmpty()) return;

        var embed = templates.startupEmbed(version, dbType, checkCount, mcVersion, startupMs);
        queue.enqueue(new WebhookQueue.WebhookMessage(url, buildPayload(embed), "lifecycle", 0));
    }

    /**
     * Send a lifecycle shutdown webhook.
     */
    public void sendLifecycleShutdown() {
        if (!config.isEnabled() || !config.isWebhookEnabled("lifecycle")) return;
        String url = config.getWebhookUrl("lifecycle");
        if (url.isEmpty()) return;

        var embed = templates.shutdownEmbed();
        // Direct send for shutdown (don't queue, we're shutting down)
        try {
            dispatcher.send(url, buildPayload(embed));
        } catch (Exception e) {
            logger.warning("Failed to send shutdown webhook: " + e.getMessage());
        }
    }

    /**
     * Send an error webhook.
     */
    public void sendError(String subsystem, String message, String stack) {
        if (!config.isEnabled() || !config.isWebhookEnabled("errors")) return;
        String url = config.getWebhookUrl("errors");
        if (url.isEmpty()) return;

        var embed = templates.errorEmbed(subsystem, message, stack);
        queue.enqueue(new WebhookQueue.WebhookMessage(url, buildPayload(embed), "errors", 0));
    }

    private String buildPayload(EmbedBuilder embed) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("embeds", List.of(embed.build()));
        return gson.toJson(payload);
    }

    /**
     * Shutdown the webhook service.
     */
    public void shutdown() {
        running = false;
        if (workerPool != null) {
            workerPool.shutdown();
            try {
                if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    workerPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                workerPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Flush the queue (attempt to send all pending messages).
     */
    public void flush() {
        if (queue == null) return;
        while (!queue.isEmpty()) {
            try {
                WebhookQueue.WebhookMessage msg = queue.poll(100);
                if (msg != null) {
                    dispatcher.send(msg.webhookUrl(), msg.payload());
                }
            } catch (Exception e) {
                break;
            }
        }
    }

    /**
     * Test a webhook endpoint.
     */
    public boolean testWebhook(String type) {
        String url = config.getWebhookUrl(type);
        if (url == null || url.isEmpty()) return false;
        var embed = new EmbedBuilder()
                .title("\u2705 AegisGuard Webhook Test")
                .description("This is a test message from AegisGuard.")
                .color(config.getColor("success"))
                .footer(config.getFooterText(), config.getFooterIcon());
        return dispatcher.send(url, buildPayload(embed));
    }

    public int getQueueSize() { return queue != null ? queue.size() : 0; }
    public boolean isRateLimited() { return rateLimiter != null && rateLimiter.isLimited(); }
}
