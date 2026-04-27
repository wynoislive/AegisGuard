package com.aegisguard.checks;

import com.aegisguard.config.ChecksConfig;
import com.aegisguard.config.ConfigManager;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

/**
 * Abstract base class for all check implementations.
 * Each check has a name, category, and configurable parameters
 * loaded from checks.yml via the @CheckInfo annotation.
 */
public abstract class Check {

    private final String name;
    private final CheckCategory category;
    private final String configName;
    private boolean enabled;
    private int threshold;
    private long cooldown;
    private double decayRate;
    private double weight;
    private boolean setbackEnabled;
    private boolean punishEnabled;
    private boolean verbose;

    protected Check() {
        CheckInfo info = this.getClass().getAnnotation(CheckInfo.class);
        if (info == null) {
            throw new IllegalStateException("Check class " + getClass().getSimpleName() + " missing @CheckInfo annotation!");
        }
        this.name = info.name();
        this.category = info.category();
        this.configName = info.configName();
    }

    /**
     * Load configuration from checks.yml.
     */
    public void loadConfig(ChecksConfig config) {
        String cat = category.getConfigKey();
        this.enabled = config.isEnabled(cat, configName);
        this.threshold = config.getThreshold(cat, configName);
        this.cooldown = config.getCooldown(cat, configName);
        this.decayRate = config.getDecayRate(cat, configName);
        this.weight = config.getWeight(cat, configName);
        this.setbackEnabled = config.isSetbackEnabled(cat, configName);
        this.punishEnabled = config.isPunishEnabled(cat, configName);
        this.verbose = config.isVerbose(cat, configName);
    }

    /**
     * Execute the check for a given player.
     * Implementations should return a CheckResult.
     */
    public abstract CheckResult check(Player player, PlayerProfile profile);

    /**
     * Calculate the VL points to add based on severity and weight.
     */
    public double calculateVL(ViolationLevel severity) {
        return severity.getPoints() * weight;
    }

    /**
     * Check if the player is on cooldown for this check.
     */
    public boolean isOnCooldown(PlayerProfile profile) {
        return profile.isAlertCooldown(name, cooldown);
    }

    // --- Getters ---

    public String getName() { return name; }
    public CheckCategory getCategory() { return category; }
    public String getConfigName() { return configName; }
    public boolean isEnabled() { return enabled; }
    public int getThreshold() { return threshold; }
    public long getCooldown() { return cooldown; }
    public double getDecayRate() { return decayRate; }
    public double getWeight() { return weight; }
    public boolean isSetbackEnabled() { return setbackEnabled; }
    public boolean isPunishEnabled() { return punishEnabled; }
    public boolean isVerbose() { return verbose; }
}
