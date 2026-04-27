package com.aegisguard.api;

import com.aegisguard.core.AegisGuard;
import com.aegisguard.playerdata.PlatformType;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

/**
 * Public API for external plugins to interact with AegisGuard.
 */
public final class AegisGuardAPI {

    /**
     * Get a player's profile.
     */
    public static PlayerProfile getProfile(UUID uuid) {
        return AegisGuard.get().getProfileManager().getProfile(uuid);
    }

    /**
     * Get a player's profile by name.
     */
    public static PlayerProfile getProfile(String name) {
        return AegisGuard.get().getProfileManager().getProfile(name);
    }

    /**
     * Get all online profiles.
     */
    public static Collection<PlayerProfile> getAllProfiles() {
        return AegisGuard.get().getProfileManager().getAllProfiles();
    }

    /**
     * Get a player's trust score.
     */
    public static double getTrustScore(UUID uuid) {
        PlayerProfile profile = getProfile(uuid);
        return profile != null ? profile.getTrustScore().getScore() : -1;
    }

    /**
     * Get a player's total violation level.
     */
    public static double getTotalVL(UUID uuid) {
        PlayerProfile profile = getProfile(uuid);
        return profile != null ? profile.getTotalVL() : -1;
    }

    /**
     * Check if a player is frozen.
     */
    public static boolean isFrozen(UUID uuid) {
        PlayerProfile profile = getProfile(uuid);
        return profile != null && profile.isFrozen();
    }

    /**
     * Get a player's detected platform.
     */
    public static PlatformType getPlatform(UUID uuid) {
        PlayerProfile profile = getProfile(uuid);
        return profile != null ? profile.getPlatform() : PlatformType.UNKNOWN;
    }

    /**
     * Exempt a player from checks for a duration.
     */
    public static void exemptPlayer(UUID uuid, long durationMs) {
        PlayerProfile profile = getProfile(uuid);
        if (profile != null) {
            profile.setExemptUntil(System.currentTimeMillis() + durationMs);
        }
    }

    /**
     * Check if AegisGuard is enabled.
     */
    public static boolean isEnabled() {
        try {
            return AegisGuard.get().isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
}
