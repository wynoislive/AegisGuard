package com.aegisguard.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Professional World Manager for AegisGuard.
 * Handles per-world configuration, protection states, and multi-dimension support.
 * Mirrors the enterprise logic found in WynoWorldGen.
 */
public final class WorldManager {

    private final Plugin plugin;
    private final Logger logger;
    private final Map<String, WorldSettings> worldSettingsMap = new HashMap<>();
    private FileConfiguration config;

    public WorldManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Load all world configurations from worlds.yml.
     */
    public void load() {
        File file = new File(plugin.getDataFolder(), "worlds.yml");
        if (!file.exists()) {
            plugin.saveResource("worlds.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        
        worldSettingsMap.clear();
        ConfigurationSection section = config.getConfigurationSection("worlds");
        if (section != null) {
            for (String worldName : section.getKeys(false)) {
                ConfigurationSection worldSection = section.getConfigurationSection(worldName);
                if (worldSection != null) {
                    worldSettingsMap.put(worldName, new WorldSettings(worldSection));
                }
            }
        }
        
        logger.info("Loaded multi-world configuration for " + worldSettingsMap.size() + " worlds.");
    }

    /**
     * Get settings for a specific world. Returns default if not explicitly configured.
     */
    public WorldSettings getSettings(World world) {
        return worldSettingsMap.getOrDefault(world.getName(), WorldSettings.DEFAULT);
    }

    /**
     * Check if a world has protection enabled.
     */
    public boolean isEnabled(World world) {
        return getSettings(world).isEnabled();
    }

    /**
     * Settings data class for per-world configuration.
     */
    public static class WorldSettings {
        public static final WorldSettings DEFAULT = new WorldSettings();

        private final boolean enabled;
        private final boolean antiXray;
        private final int antiXrayMode;

        private WorldSettings() {
            this.enabled = true;
            this.antiXray = true;
            this.antiXrayMode = 2;
        }

        public WorldSettings(ConfigurationSection section) {
            this.enabled = section.getBoolean("enabled", true);
            this.antiXray = section.getBoolean("anti-xray.enabled", true);
            this.antiXrayMode = section.getInt("anti-xray.mode", 2);
        }

        public boolean isEnabled() { return enabled; }
        public boolean isAntiXrayEnabled() { return antiXray; }
        public int getAntiXrayMode() { return antiXrayMode; }
    }
}
