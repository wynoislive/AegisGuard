package com.aegisguard.storage;

import com.aegisguard.config.DatabaseConfig;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.storage.mysql.MySQLStorage;
import com.aegisguard.storage.sqlite.SQLiteStorage;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Database connection manager using HikariCP connection pooling.
 * Delegates to either SQLite or MySQL storage backends.
 */
public final class DatabaseManager {

    private final Plugin plugin;
    private final Logger logger;
    private final DatabaseConfig config;
    private StorageManager storage;

    public DatabaseManager(Plugin plugin, DatabaseConfig config) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = config;
    }

    /**
     * Initialize the database connection and schema.
     */
    public void initialize() throws Exception {
        if (config.isMySQL()) {
            logger.info("Using MySQL database: " + config.getHost() + ":" + config.getPort() + "/" + config.getDatabaseName());
            storage = new MySQLStorage(plugin, config);
        } else {
            logger.info("Using SQLite database (automatic, zero-config)");
            storage = new SQLiteStorage(plugin, config);
        }
        storage.initialize();
        logger.info("Database initialized successfully.");
    }

    /**
     * Shutdown the database connection.
     */
    public void shutdown() {
        if (storage != null) {
            storage.flushBatch();
            storage.shutdown();
            logger.info("Database connection closed.");
        }
    }

    /**
     * Flush pending batch operations.
     */
    public void flushBatch() {
        if (storage != null) {
            storage.flushBatch();
        }
    }

    // --- Delegate methods ---

    public void loadPlayer(PlayerProfile profile) {
        if (storage != null) storage.loadPlayer(profile);
    }

    public void savePlayer(PlayerProfile profile) {
        if (storage != null) storage.savePlayer(profile);
    }

    public void insertViolation(UUID playerUuid, String checkName, String category, double vl,
                                String severity, String details, String world,
                                double x, double y, double z, float yaw, float pitch,
                                int ping, double tps) {
        if (storage != null) storage.insertViolation(playerUuid, checkName, category, vl,
                severity, details, world, x, y, z, yaw, pitch, ping, tps);
    }

    public void insertAlert(UUID playerUuid, String checkName, String category, double vl, String message) {
        if (storage != null) storage.insertAlert(playerUuid, checkName, category, vl, message);
    }

    public void insertPunishment(UUID playerUuid, String action, String reason, long duration,
                                 UUID staffUuid, String staffName, boolean automatic,
                                 String evidenceId, String triggerChecks) {
        if (storage != null) storage.insertPunishment(playerUuid, action, reason, duration,
                staffUuid, staffName, automatic, evidenceId, triggerChecks);
    }

    public void insertEvidence(String evidenceId, UUID playerUuid, String world,
                               double x, double y, double z, float yaw, float pitch,
                               String targetUuid, String targetName,
                               String nearbyEntities, String recentPackets, String recentPath,
                               String triggeredChecks, String debugContext) {
        if (storage != null) storage.insertEvidence(evidenceId, playerUuid, world,
                x, y, z, yaw, pitch, targetUuid, targetName,
                nearbyEntities, recentPackets, recentPath, triggeredChecks, debugContext);
    }

    public void insertWebhookEvent(String webhookType, String payload, String status) {
        if (storage != null) storage.insertWebhookEvent(webhookType, payload, status);
    }

    public int getViolationCount(UUID playerUuid) {
        return storage != null ? storage.getViolationCount(playerUuid) : 0;
    }

    public int getPunishmentCount(UUID playerUuid) {
        return storage != null ? storage.getPunishmentCount(playerUuid) : 0;
    }

    public StorageManager getStorage() { return storage; }
}
