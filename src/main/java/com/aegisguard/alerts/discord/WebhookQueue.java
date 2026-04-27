package com.aegisguard.alerts.discord;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Async queue for webhook messages.
 */
public final class WebhookQueue {

    private final LinkedBlockingQueue<WebhookMessage> queue;

    public WebhookQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * Enqueue a webhook message. Returns false if queue is full.
     */
    public boolean enqueue(WebhookMessage message) {
        return queue.offer(message);
    }

    /**
     * Take the next message, blocking until available.
     */
    public WebhookMessage take() throws InterruptedException {
        return queue.take();
    }

    /**
     * Poll for a message with timeout.
     */
    public WebhookMessage poll(long timeoutMs) throws InterruptedException {
        return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public int size() { return queue.size(); }
    public boolean isEmpty() { return queue.isEmpty(); }

    /**
     * Drain all messages from the queue.
     */
    public void clear() { queue.clear(); }

    /**
     * Internal webhook message wrapper.
     */
    public record WebhookMessage(
            String webhookUrl,
            String payload,
            String type,
            int retryCount
    ) {}
}
