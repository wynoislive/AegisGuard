package com.aegisguard.compat;

import com.aegisguard.compat.floodgate.FloodgateHook;
import com.aegisguard.compat.paper.PaperCompat;
import com.aegisguard.compat.protocollib.ProtocolLibHook;
import com.aegisguard.config.ConfigManager;
import com.aegisguard.playerdata.PlatformType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Compatibility layer managing integration with external plugins.
 */
public final class CompatManager {

    private final Plugin plugin;
    private final Logger logger;
    private final ConfigManager config;

    private FloodgateHook floodgateHook;
    private ProtocolLibHook protocolLibHook;
    private PaperCompat paperCompat;

    private boolean floodgateAvailable;
    private boolean protocolLibAvailable;

    public CompatManager(Plugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = config;
    }

    /**
     * Initialize compatibility hooks.
     */
    public void initialize() {
        // Paper compatibility
        paperCompat = new PaperCompat();
        logger.info("Paper compatibility layer loaded.");

        // Floodgate
        floodgateAvailable = Bukkit.getPluginManager().getPlugin("floodgate") != null;
        if (floodgateAvailable) {
            floodgateHook = new FloodgateHook();
            logger.info("Floodgate integration enabled.");
        } else {
            logger.info("Floodgate not found. Bedrock detection limited.");
        }

        // ProtocolLib
        protocolLibAvailable = Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
        if (protocolLibAvailable) {
            protocolLibHook = new ProtocolLibHook(plugin);
            protocolLibHook.initialize();
            logger.info("ProtocolLib integration enabled.");
        } else {
            logger.warning("ProtocolLib not found! Some checks may be limited.");
        }
    }

    /**
     * Detect a player's platform type.
     */
    public PlatformType detectPlatform(Player player) {
        if (!config.isBedrockDetection()) return PlatformType.JAVA;

        if (floodgateAvailable && floodgateHook != null) {
            return floodgateHook.detectPlatform(player);
        }

        // Fallback: check for Bedrock prefix (commonly ".")
        String name = player.getName();
        if (name.startsWith(".") || name.startsWith("*")) {
            return PlatformType.BEDROCK;
        }

        return PlatformType.JAVA;
    }

    public boolean isFloodgateAvailable() { return floodgateAvailable; }
    public boolean isProtocolLibAvailable() { return protocolLibAvailable; }
    public FloodgateHook getFloodgateHook() { return floodgateHook; }
    public ProtocolLibHook getProtocolLibHook() { return protocolLibHook; }
    public PaperCompat getPaperCompat() { return paperCompat; }
}
