package com.aegisguard.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed wrapper for punishments.yml.
 */
public final class PunishmentsConfig {

    private final FileConfiguration config;

    public PunishmentsConfig(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Get the action type for a given escalation stage.
     */
    public String getAction(String stage) {
        return config.getString("escalation." + stage + ".action", "SILENT_LOG");
    }

    /**
     * Get the VL range string for a stage.
     */
    public String getVlRange(String stage) {
        return config.getString("escalation." + stage + ".vl-range", "1-10");
    }

    /**
     * Get the duration in seconds for a stage.
     */
    public long getDuration(String stage) {
        return config.getLong("escalation." + stage + ".duration", 0);
    }

    /**
     * Get stage message.
     */
    public String getMessage(String stage) {
        return config.getString("escalation." + stage + ".message", "");
    }

    /**
     * Get the escalation speed for a category.
     */
    public double getCategorySpeed(String category) {
        return config.getDouble("categories." + category + ".escalation-speed", 1.0);
    }

    /**
     * Check if a category's punishments are enabled.
     */
    public boolean isCategoryEnabled(String category) {
        return config.getBoolean("categories." + category + ".enabled", true);
    }

    /**
     * Get high trust threshold.
     */
    public int getHighTrustThreshold() {
        return config.getInt("trust-modifiers.high-trust.threshold", 75);
    }

    /**
     * Get high trust speed multiplier.
     */
    public double getHighTrustMultiplier() {
        return config.getDouble("trust-modifiers.high-trust.speed-multiplier", 0.5);
    }

    /**
     * Get low trust threshold.
     */
    public int getLowTrustThreshold() {
        return config.getInt("trust-modifiers.low-trust.threshold", 25);
    }

    /**
     * Get low trust speed multiplier.
     */
    public double getLowTrustMultiplier() {
        return config.getDouble("trust-modifiers.low-trust.speed-multiplier", 2.0);
    }

    /**
     * Get minimum flags before action is taken.
     */
    public int getMinFlagsBeforeAction() {
        return config.getInt("safety.min-flags-before-action", 3);
    }

    /**
     * Get minimum time (seconds) before a ban can be applied.
     */
    public long getMinTimeBeforeBan() {
        return config.getLong("safety.min-time-before-ban", 300);
    }

    /**
     * Whether bans require staff confirmation.
     */
    public boolean requireStaffConfirmation() {
        return config.getBoolean("safety.require-staff-confirmation-for-ban", true);
    }

    /**
     * Parse a VL range string like "11-25" or "121+" into min/max.
     */
    public int[] parseVlRange(String rangeStr) {
        if (rangeStr == null) return new int[]{0, Integer.MAX_VALUE};
        rangeStr = rangeStr.trim();
        if (rangeStr.endsWith("+")) {
            int min = Integer.parseInt(rangeStr.substring(0, rangeStr.length() - 1));
            return new int[]{min, Integer.MAX_VALUE};
        }
        String[] parts = rangeStr.split("-");
        if (parts.length == 2) {
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        }
        return new int[]{0, Integer.MAX_VALUE};
    }
}
