package com.aegisguard.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Centralized configuration manager for all YAML config files.
 */
public final class ConfigManager {

    private final Plugin plugin;
    private final Logger logger;
    private final Map<String, FileConfiguration> configs = new HashMap<>();

    private ChecksConfig checksConfig;
    private MessagesConfig messagesConfig;
    private PunishmentsConfig punishmentsConfig;
    private GuiConfig guiConfig;
    private DatabaseConfig databaseConfig;
    private DiscordConfig discordConfig;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Load all configuration files.
     */
    public void loadAll() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        loadConfig("checks.yml");
        loadConfig("messages.yml");
        loadConfig("punishments.yml");
        loadConfig("gui.yml");
        loadConfig("database.yml");
        loadConfig("discord.yml");

        this.checksConfig = new ChecksConfig(getConfig("checks.yml"));
        this.messagesConfig = new MessagesConfig(getConfig("messages.yml"));
        this.punishmentsConfig = new PunishmentsConfig(getConfig("punishments.yml"));
        this.guiConfig = new GuiConfig(getConfig("gui.yml"));
        this.databaseConfig = new DatabaseConfig(getConfig("database.yml"));
        this.discordConfig = new DiscordConfig(getConfig("discord.yml"));

        logger.info("All configuration files loaded successfully.");
    }

    /**
     * Reload all configuration files.
     */
    public void reloadAll() {
        configs.clear();
        loadAll();
    }

    /**
     * Load a specific YAML configuration file, saving from resources if needed.
     */
    private void loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Apply defaults from jar resource
        InputStream defaultStream = plugin.getResource(fileName);
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaults);
        }

        configs.put(fileName, config);
    }

    /**
     * Get the main plugin config.
     */
    public FileConfiguration getMainConfig() {
        return plugin.getConfig();
    }

    /**
     * Get a specific config file.
     */
    public FileConfiguration getConfig(String fileName) {
        return configs.get(fileName);
    }

    /**
     * Save a specific config file.
     */
    public void saveConfig(String fileName) {
        FileConfiguration config = configs.get(fileName);
        if (config == null) return;
        File file = new File(plugin.getDataFolder(), fileName);
        try {
            config.save(file);
        } catch (IOException e) {
            logger.severe("Failed to save config " + fileName + ": " + e.getMessage());
        }
    }

    // --- Typed config accessors ---

    public boolean isEnabled() {
        return getMainConfig().getBoolean("general.enabled", true);
    }

    public boolean isDebug() {
        return getMainConfig().getBoolean("general.debug", false);
    }

    public String getPrefix() {
        return getMainConfig().getString("general.prefix", "&8[&b&lAegisGuard&8] ");
    }

    public int getAsyncThreads() {
        return getMainConfig().getInt("performance.async-threads", 4);
    }

    public int getTaskInterval() {
        return getMainConfig().getInt("performance.task-interval", 20);
    }

    public boolean isTpsCompensation() {
        return getMainConfig().getBoolean("performance.tps-compensation", true);
    }

    public double getLowTpsThreshold() {
        return getMainConfig().getDouble("performance.low-tps-threshold", 18.0);
    }

    public boolean isPingCompensation() {
        return getMainConfig().getBoolean("performance.ping-compensation", true);
    }

    public int getHighPingThreshold() {
        return getMainConfig().getInt("performance.high-ping-threshold", 200);
    }

    public int getMaxPacketRate() {
        return getMainConfig().getInt("performance.max-packet-rate", 500);
    }

    public boolean isBedrockDetection() {
        return getMainConfig().getBoolean("platform.bedrock-detection", true);
    }

    public double getBedrockMovementMultiplier() {
        return getMainConfig().getDouble("platform.bedrock-movement-multiplier", 1.5);
    }

    public double getBedrockCombatMultiplier() {
        return getMainConfig().getDouble("platform.bedrock-combat-multiplier", 1.4);
    }

    public int getBedrockLatencyMargin() {
        return getMainConfig().getInt("platform.bedrock-latency-margin", 100);
    }

    public int getMaxViolationsMemory() {
        return getMainConfig().getInt("general.max-violations-memory", 500);
    }

    public int getDecayInterval() {
        return getMainConfig().getInt("general.decay-interval", 30);
    }

    public boolean isAntiXrayEnabled() {
        return getMainConfig().getBoolean("prevention.antixray.enabled", true);
    }

    public int getAntiXrayMode() {
        return getMainConfig().getInt("prevention.antixray.mode", 2);
    }

    public ChecksConfig getChecksConfig() { return checksConfig; }
    public MessagesConfig getMessagesConfig() { return messagesConfig; }
    public PunishmentsConfig getPunishmentsConfig() { return punishmentsConfig; }
    public GuiConfig getGuiConfig() { return guiConfig; }
    public DatabaseConfig getDatabaseConfig() { return databaseConfig; }
    public DiscordConfig getDiscordConfig() { return discordConfig; }
}
