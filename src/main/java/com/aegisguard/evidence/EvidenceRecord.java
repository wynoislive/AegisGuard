package com.aegisguard.evidence;

import java.util.UUID;

/**
 * Immutable evidence record capturing full context of a detection event.
 */
public record EvidenceRecord(
        String evidenceId,
        UUID playerUuid,
        String playerName,
        long timestamp,
        String world,
        double x, double y, double z,
        float yaw, float pitch,
        String targetUuid,
        String targetName,
        String nearbyEntities,
        String recentPackets,
        String recentPath,
        String triggeredChecks,
        String debugContext
) {
    /**
     * Create a new evidence record with a generated unique ID.
     */
    public static EvidenceRecord create(UUID playerUuid, String playerName,
                                         String world, double x, double y, double z,
                                         float yaw, float pitch,
                                         String targetUuid, String targetName,
                                         String nearbyEntities, String recentPackets,
                                         String recentPath, String triggeredChecks,
                                         String debugContext) {
        return new EvidenceRecord(
                UUID.randomUUID().toString().substring(0, 8),
                playerUuid, playerName,
                System.currentTimeMillis(),
                world, x, y, z, yaw, pitch,
                targetUuid, targetName,
                nearbyEntities, recentPackets, recentPath,
                triggeredChecks, debugContext
        );
    }
}
