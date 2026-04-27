package com.aegisguard.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed wrapper for gui.yml.
 */
public final class GuiConfig {

    private final FileConfiguration config;

    public GuiConfig(FileConfiguration config) {
        this.config = config;
    }

    public String getMainTitle() {
        return config.getString("main-panel.title", "&8AegisGuard Dashboard");
    }

    public int getMainSize() {
        return config.getInt("main-panel.size", 54);
    }

    public String getPlayerTitle() {
        return config.getString("player-panel.title", "&8Player: {player}");
    }

    public int getPlayerSize() {
        return config.getInt("player-panel.size", 54);
    }

    public int getItemSlot(String panel, String itemId) {
        return config.getInt(panel + ".items." + itemId + ".slot", 0);
    }

    public String getItemMaterial(String panel, String itemId) {
        return config.getString(panel + ".items." + itemId + ".material", "STONE");
    }

    public String getItemName(String panel, String itemId) {
        return config.getString(panel + ".items." + itemId + ".name", "&7Unknown");
    }

    public java.util.List<String> getItemLore(String panel, String itemId) {
        return config.getStringList(panel + ".items." + itemId + ".lore");
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
