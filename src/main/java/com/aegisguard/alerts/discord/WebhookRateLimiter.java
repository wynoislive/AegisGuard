package com.aegisguard.alerts.discord;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Token bucket rate limiter for webhook dispatch.
 */
public final class WebhookRateLimiter {

    private final int tokensPerSecond;
    private final int burstCapacity;
    private double currentTokens;
    private long lastRefill;

    public WebhookRateLimiter(int tokensPerSecond, int burstCapacity) {
        this.tokensPerSecond = tokensPerSecond;
        this.burstCapacity = burstCapacity;
        this.currentTokens = burstCapacity;
        this.lastRefill = System.currentTimeMillis();
    }

    /**
     * Try to acquire a token. Returns true if allowed, false if rate limited.
     */
    public synchronized boolean tryAcquire() {
        refill();
        if (currentTokens >= 1.0) {
            currentTokens -= 1.0;
            return true;
        }
        return false;
    }

    /**
     * Get the estimated wait time in ms before a token is available.
     */
    public synchronized long getWaitTimeMs() {
        refill();
        if (currentTokens >= 1.0) return 0;
        double tokensNeeded = 1.0 - currentTokens;
        return (long) (tokensNeeded / tokensPerSecond * 1000);
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefill;
        double refilled = (elapsed / 1000.0) * tokensPerSecond;
        currentTokens = Math.min(burstCapacity, currentTokens + refilled);
        lastRefill = now;
    }

    public boolean isLimited() {
        return currentTokens < 1.0;
    }
}
