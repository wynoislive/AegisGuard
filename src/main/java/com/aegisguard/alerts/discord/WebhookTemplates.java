package com.aegisguard.alerts.discord;

import com.aegisguard.alerts.AlertLevel;
import com.aegisguard.util.TimeUtil;

/**
 * Pre-built webhook embed templates for different event types.
 */
public final class WebhookTemplates {

    private final String footerText;
    private final String footerIcon;
    private final String avatarBaseUrl;
    private final int infoColor;
    private final int warnColor;
    private final int dangerColor;
    private final int successColor;
    private final int criticalColor;

    public WebhookTemplates(String footerText, String footerIcon, String avatarBaseUrl,
                            int infoColor, int warnColor, int dangerColor, int successColor, int criticalColor) {
        this.footerText = footerText;
        this.footerIcon = footerIcon;
        this.avatarBaseUrl = avatarBaseUrl;
        this.infoColor = infoColor;
        this.warnColor = warnColor;
        this.dangerColor = dangerColor;
        this.successColor = successColor;
        this.criticalColor = criticalColor;
    }

    /**
     * Build a suspicious alert embed.
     */
    public EmbedBuilder alertEmbed(String player, String uuid, String platform, String world, String xyz,
                                   int ping, double tps, String check, String category, double vl,
                                   double trust, String summary, AlertLevel level) {
        int color = switch (level) {
            case LOW -> infoColor;
            case MEDIUM -> warnColor;
            case HIGH -> dangerColor;
            case CRITICAL -> criticalColor;
        };
        String emoji = switch (level) {
            case LOW -> "\u2139\uFE0F";
            case MEDIUM -> "\u26A0\uFE0F";
            case HIGH -> "\uD83D\uDED1";
            case CRITICAL -> "\uD83D\uDEA8";
        };
        return new EmbedBuilder()
                .title(emoji + " Suspicious Activity Detected")
                .color(color)
                .thumbnail(avatarBaseUrl.replace("{uuid}", uuid))
                .field("Player", player, true)
                .field("UUID", "`" + uuid.substring(0, 8) + "...`", true)
                .field("Platform", platform, true)
                .field("World", world, true)
                .field("Location", xyz, true)
                .field("Ping", ping + "ms", true)
                .field("TPS", String.format("%.1f", tps), true)
                .field("Check", check, true)
                .field("Category", category, true)
                .field("VL", String.format("%.0f", vl), true)
                .field("Trust", String.format("%.0f", trust), true)
                .field("Summary", summary, false)
                .footer(footerText, footerIcon)
                .timestamp(TimeUtil.toISO(System.currentTimeMillis()));
    }

    /**
     * Build a punishment embed.
     */
    public EmbedBuilder punishmentEmbed(String player, String action, String reason, String triggerChecks,
                                        boolean automatic, String duration, String evidenceId) {
        return new EmbedBuilder()
                .title("\uD83D\uDD28 Punishment Issued")
                .color(dangerColor)
                .field("Player", player, true)
                .field("Action", action, true)
                .field("Auto", automatic ? "Yes" : "No", true)
                .field("Reason", reason, false)
                .field("Trigger Checks", triggerChecks, true)
                .field("Duration", duration, true)
                .field("Evidence ID", evidenceId != null ? evidenceId : "N/A", true)
                .footer(footerText, footerIcon)
                .timestamp(TimeUtil.toISO(System.currentTimeMillis()));
    }

    /**
     * Build a lifecycle startup embed.
     */
    public EmbedBuilder startupEmbed(String version, String dbType, int checkCount, String mcVersion, long startupMs) {
        return new EmbedBuilder()
                .title("\u2705 AegisGuard Started")
                .color(successColor)
                .field("Version", version, true)
                .field("MC Version", mcVersion, true)
                .field("Database", dbType, true)
                .field("Checks Loaded", String.valueOf(checkCount), true)
                .field("Startup Time", startupMs + "ms", true)
                .footer(footerText, footerIcon)
                .timestamp(TimeUtil.toISO(System.currentTimeMillis()));
    }

    /**
     * Build a lifecycle shutdown embed.
     */
    public EmbedBuilder shutdownEmbed() {
        return new EmbedBuilder()
                .title("\uD83D\uDED1 AegisGuard Shutdown")
                .color(warnColor)
                .description("Plugin is shutting down gracefully.")
                .footer(footerText, footerIcon)
                .timestamp(TimeUtil.toISO(System.currentTimeMillis()));
    }

    /**
     * Build an error embed.
     */
    public EmbedBuilder errorEmbed(String subsystem, String message, String stackExcerpt) {
        return new EmbedBuilder()
                .title("\u274C Error Detected")
                .color(dangerColor)
                .field("Subsystem", subsystem, true)
                .field("Error", message, false)
                .field("Stack", "```\n" + (stackExcerpt.length() > 500 ? stackExcerpt.substring(0, 500) : stackExcerpt) + "\n```", false)
                .footer(footerText, footerIcon)
                .timestamp(TimeUtil.toISO(System.currentTimeMillis()));
    }
}
