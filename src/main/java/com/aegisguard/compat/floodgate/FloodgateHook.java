package com.aegisguard.compat.floodgate;

import com.aegisguard.playerdata.PlatformType;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

/**
 * Floodgate API integration for Bedrock player detection.
 */
public final class FloodgateHook {

    /**
     * Detect the platform type of a player using Floodgate API.
     */
    public PlatformType detectPlatform(Player player) {
        try {
            FloodgateApi api = FloodgateApi.getInstance();
            if (api.isFloodgatePlayer(player.getUniqueId())) {
                FloodgatePlayer fp = api.getPlayer(player.getUniqueId());
                if (fp != null) {
                    String deviceOs = fp.getDeviceOs().toString().toUpperCase();
                    if (deviceOs.contains("IOS") || deviceOs.contains("ANDROID")) {
                        return PlatformType.MOBILE_TOUCH;
                    }
                    if (deviceOs.contains("XBOX") || deviceOs.contains("PLAYSTATION")
                            || deviceOs.contains("SWITCH") || deviceOs.contains("NINTENDO")) {
                        return PlatformType.CONTROLLER;
                    }
                    return PlatformType.BEDROCK;
                }
                return PlatformType.BEDROCK;
            }
        } catch (Exception e) {
            // Floodgate API not available or error
        }
        return PlatformType.JAVA;
    }
}
