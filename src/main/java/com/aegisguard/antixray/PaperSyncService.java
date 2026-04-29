package com.aegisguard.antixray;

import com.aegisguard.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * Service to manage Paper's native Anti-Xray configurations.
 * Ported from OreHider 1.4 logic.
 */
public final class PaperSyncService {

    private final Plugin plugin;
    private final ConfigManager configManager;
    private File globalDefaultsFile;
    private boolean isModern;

    public PaperSyncService(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        detectEnvironment();
    }

    private void detectEnvironment() {
        File modern = new File(plugin.getServer().getWorldContainer(), "config/paper-world-defaults.yml");
        if (modern.exists()) {
            this.globalDefaultsFile = modern;
            this.isModern = true;
            return;
        }

        File legacy = new File(plugin.getServer().getWorldContainer(), "paper.yml");
        if (legacy.exists()) {
            this.globalDefaultsFile = legacy;
            this.isModern = false;
            return;
        }

        plugin.getLogger().warning("[OreHider] Could not locate 'paper-world-defaults.yml' or 'paper.yml'. Global sync will be skipped.");
    }

    public boolean syncAll() {
        boolean changed = false;

        // 1. Sync Global Defaults
        ConfigurationSection global = configManager.getChecksConfig().getConfig().getConfigurationSection("ore-hider.global");
        if (globalDefaultsFile != null && global != null && global.getBoolean("enabled", true)) {
            if (applyConfig(globalDefaultsFile, global)) {
                changed = true;
                plugin.getLogger().info("[OreHider] Updated Global Defaults (" + globalDefaultsFile.getName() + ").");
            }
        }

        // 2. Sync Per-World
        for (World world : Bukkit.getWorlds()) {
            changed |= syncWorld(world);
        }

        return changed;
    }

    private boolean syncWorld(World world) {
        if (!isModern) return false;

        File worldConfigFile = new File(world.getWorldFolder(), "paper-world.yml");
        ConfigurationSection worldOverrides = configManager.getChecksConfig().getConfig().getConfigurationSection("ore-hider.worlds." + world.getName());
        ConfigurationSection global = configManager.getChecksConfig().getConfig().getConfigurationSection("ore-hider.global");
        
        ConfigurationSection settingsToApply = (worldOverrides != null) ? worldOverrides : global;

        // Dimension-Aware Auto-Adjustment (OreHider parity)
        if (worldOverrides == null && settingsToApply != null) {
            if (world.getEnvironment() == World.Environment.NETHER) {
                if (settingsToApply.getInt("max-block-height") == 64) {
                    plugin.getLogger().info("[OreHider] Auto-adjusting Anti-Xray height to 128 for Nether: " + world.getName());
                    // In-memory adjustment for the current sync cycle
                }
            }
        }

        if (worldConfigFile.exists() && settingsToApply != null) {
            // Create a temporary copy to adjust values without polluting the global config object
            YamlConfiguration tempConfig = new YamlConfiguration();
            for (String key : settingsToApply.getKeys(true)) {
                tempConfig.set(key, settingsToApply.get(key));
            }
            
            if (world.getEnvironment() == World.Environment.NETHER && tempConfig.getInt("max-block-height") == 64) {
                tempConfig.set("max-block-height", 128);
            }

            if (applyConfig(worldConfigFile, tempConfig)) {
                plugin.getLogger().info("[OreHider] Enforced settings on world: " + world.getName());
                return true;
            }
        }
        
        return false;
    }

    private boolean applyConfig(File target, ConfigurationSection source) {
        if (target == null || source == null) return false;

        YamlConfiguration paperConfig = YamlConfiguration.loadConfiguration(target);
        boolean changed = false;

        String basePath = isModern ? "anticheat.anti-xray." : "world-settings.default.anti-xray.";

        if (source.contains("enabled") && syncValue(paperConfig, basePath + "enabled", source.getBoolean("enabled"))) changed = true;
        if (source.contains("engine-mode") && syncValue(paperConfig, basePath + "engine-mode", source.getInt("engine-mode"))) changed = true;
        if (source.contains("max-block-height") && syncValue(paperConfig, basePath + "max-block-height", source.getInt("max-block-height"))) changed = true;
        if (source.contains("update-radius") && syncValue(paperConfig, basePath + "update-radius", source.getInt("update-radius"))) changed = true;
        if (source.contains("lava-obscures") && syncValue(paperConfig, basePath + "lava-obscures", source.getBoolean("lava-obscures"))) changed = true;

        if (source.contains("hidden-blocks")) {
            List<String> oreList = source.getStringList("hidden-blocks");
            String listPath = basePath + "hidden-blocks";
            List<String> currentList = paperConfig.getStringList(listPath);
            
            if (!currentList.equals(oreList)) {
                paperConfig.set(listPath, oreList);
                changed = true;
            }
        }

        if (changed) {
            try {
                paperConfig.save(target);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "[OreHider] Failed to save Paper config: " + target.getPath(), e);
                return false;
            }
        }

        return changed;
    }

    private boolean syncValue(YamlConfiguration config, String path, Object desiredValue) {
        Object current = config.get(path);
        if (current == null || !current.equals(desiredValue)) {
            config.set(path, desiredValue);
            return true;
        }
        return false;
    }
}
