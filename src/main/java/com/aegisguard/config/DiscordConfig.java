package com.aegisguard.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Typed wrapper for discord.yml configuration.
 */
public final class DiscordConfig {

    private final FileConfiguration config;

    public DiscordConfig(FileConfiguration config) {
        this.config = config;
    }

    public boolean isEnabled() {
        return config.getBoolean("discord.enabled", false);
    }

    // Webhook URLs
    public String getWebhookUrl(String type) {
        return config.getString("discord.webhooks." + type + ".url", "");
    }

    public boolean isWebhookEnabled(String type) {
        return config.getBoolean("discord.webhooks." + type + ".enabled", false);
    }

    // Style
    public String getFooterText() {
        return config.getString("discord.style.footer-text", "AegisGuard | Paper 1.21.1");
    }

    public String getFooterIcon() {
        return config.getString("discord.style.footer-icon", "");
    }

    public boolean isThumbnailEnabled() {
        return config.getBoolean("discord.style.thumbnail-enabled", true);
    }

    public String getAvatarUrl() {
        return config.getString("discord.style.avatar-url", "https://crafthead.net/helm/{uuid}");
    }

    public int getColor(String type) {
        return config.getInt("discord.style.colors." + type, 3447003);
    }

    // Delivery settings
    public int getWorkers() {
        return config.getInt("discord.delivery.workers", 2);
    }

    public int getQueueCapacity() {
        return config.getInt("discord.delivery.queue-capacity", 1000);
    }

    public int getMaxRetries() {
        return config.getInt("discord.delivery.max-retries", 3);
    }

    public long getRetryDelayMs() {
        return config.getLong("discord.delivery.retry-delay-ms", 1000);
    }

    public boolean isExponentialBackoff() {
        return config.getBoolean("discord.delivery.exponential-backoff", true);
    }

    // Rate limiter
    public int getTokensPerSecond() {
        return config.getInt("discord.delivery.rate-limit.tokens-per-second", 5);
    }

    public int getBurstCapacity() {
        return config.getInt("discord.delivery.rate-limit.burst-capacity", 10);
    }

    // Aggregation
    public boolean isAggregationEnabled() {
        return config.getBoolean("discord.aggregation.enabled", true);
    }

    public int getAggregationWindowSeconds() {
        return config.getInt("discord.aggregation.window-seconds", 30);
    }

    public int getMaxAggregated() {
        return config.getInt("discord.aggregation.max-aggregated", 15);
    }

    // Daily summary
    public boolean isDailySummaryEnabled() {
        return config.getBoolean("discord.daily-summary.enabled", true);
    }

    public String getDailySummaryTime() {
        return config.getString("discord.daily-summary.time", "00:00");
    }

    public String getDailySummaryWebhook() {
        return config.getString("discord.daily-summary.webhook", "lifecycle");
    }

    // Correlation
    public boolean isCorrelationEnabled() {
        return config.getBoolean("discord.correlation.enabled", true);
    }

    @SuppressWarnings("unchecked")
    public List<List<String>> getHighRiskPatterns() {
        List<?> raw = config.getList("discord.correlation.high-risk-patterns");
        if (raw == null) return Collections.emptyList();
        List<List<String>> result = new ArrayList<>();
        for (Object item : raw) {
            if (item instanceof List<?> list) {
                List<String> pattern = new ArrayList<>();
                for (Object s : list) {
                    pattern.add(String.valueOf(s));
                }
                result.add(pattern);
            }
        }
        return result;
    }

    // Security
    public boolean isMaskUrls() {
        return config.getBoolean("discord.security.mask-urls", true);
    }

    public boolean isValidateUrls() {
        return config.getBoolean("discord.security.validate-urls", true);
    }
}
