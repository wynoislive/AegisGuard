package com.aegisguard.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed wrapper for checks.yml configuration.
 */
public final class ChecksConfig {

    private final FileConfiguration config;

    public ChecksConfig(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Check if a specific check is enabled.
     */
    public boolean isEnabled(String category, String checkName) {
        return config.getBoolean(category + "." + checkName + ".enabled", true);
    }

    /**
     * Get the VL threshold for a check.
     */
    public int getThreshold(String category, String checkName) {
        return config.getInt(category + "." + checkName + ".threshold", 20);
    }

    /**
     * Get the cooldown in ms between flags.
     */
    public long getCooldown(String category, String checkName) {
        return config.getLong(category + "." + checkName + ".cooldown", 500);
    }

    /**
     * Get the VL decay rate per interval.
     */
    public double getDecayRate(String category, String checkName) {
        return config.getDouble(category + "." + checkName + ".decay-rate", 1.0);
    }

    /**
     * Get the weight multiplier for VL gain.
     */
    public double getWeight(String category, String checkName) {
        return config.getDouble(category + "." + checkName + ".weight", 1.0);
    }

    /**
     * Check if setback is enabled for a check.
     */
    public boolean isSetbackEnabled(String category, String checkName) {
        return config.getBoolean(category + "." + checkName + ".setback", false);
    }

    /**
     * Check if auto-punish is enabled for a check.
     */
    public boolean isPunishEnabled(String category, String checkName) {
        return config.getBoolean(category + "." + checkName + ".punish", true);
    }

    /**
     * Check if verbose output is enabled for a check.
     */
    public boolean isVerbose(String category, String checkName) {
        return config.getBoolean(category + "." + checkName + ".verbose", false);
    }

    /**
     * Get a check's configuration section directly.
     */
    public ConfigurationSection getCheckSection(String category, String checkName) {
        return config.getConfigurationSection(category + "." + checkName);
    }

    /**
     * Get the full config for direct access.
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
