package com.aegisguard.storage;

import com.aegisguard.playerdata.PlayerProfile;

import java.util.List;
import java.util.UUID;

/**
 * Abstract storage interface for database operations.
 */
public interface StorageManager {

    /**
     * Initialize the storage backend (create tables, etc).
     */
    void initialize() throws Exception;

    /**
     * Shutdown the storage backend.
     */
    void shutdown();

    /**
     * Load a player from the database into the profile.
     */
    void loadPlayer(PlayerProfile profile);

    /**
     * Save a player profile to the database.
     */
    void savePlayer(PlayerProfile profile);

    /**
     * Insert a violation record.
     */
    void insertViolation(UUID playerUuid, String checkName, String category, double vl,
                         String severity, String details, String world,
                         double x, double y, double z, float yaw, float pitch,
                         int ping, double tps);

    /**
     * Insert an alert record.
     */
    void insertAlert(UUID playerUuid, String checkName, String category, double vl, String message);

    /**
     * Insert a punishment record.
     */
    void insertPunishment(UUID playerUuid, String action, String reason, long duration,
                          UUID staffUuid, String staffName, boolean automatic,
                          String evidenceId, String triggerChecks);

    /**
     * Insert an evidence record.
     */
    void insertEvidence(String evidenceId, UUID playerUuid, String world,
                        double x, double y, double z, float yaw, float pitch,
                        String targetUuid, String targetName,
                        String nearbyEntities, String recentPackets, String recentPath,
                        String triggeredChecks, String debugContext);

    /**
     * Insert a webhook event record.
     */
    void insertWebhookEvent(String webhookType, String payload, String status);

    /**
     * Flush any pending batch operations.
     */
    void flushBatch();

    /**
     * Get violation count for a player.
     */
    int getViolationCount(UUID playerUuid);

    /**
     * Get punishment count for a player.
     */
    int getPunishmentCount(UUID playerUuid);
}
