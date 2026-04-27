package com.aegisguard.storage.mysql;

import com.aegisguard.config.DatabaseConfig;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.storage.StorageManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * MySQL storage implementation for high-performance enterprise setups.
 * Uses HikariCP connection pooling with configurable pool settings.
 */
public final class MySQLStorage implements StorageManager {

    private final Plugin plugin;
    private final Logger logger;
    private final DatabaseConfig config;
    private HikariDataSource dataSource;

    public MySQLStorage(Plugin plugin, DatabaseConfig config) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = config;
    }

    @Override
    public void initialize() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getMySQLUrl());
        hikariConfig.setUsername(config.getUser());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getMinIdle());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setKeepaliveTime(config.getKeepaliveTime());
        hikariConfig.setPoolName("AegisGuard-MySQL");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");

        dataSource = new HikariDataSource(hikariConfig);
        executeSchema();
        logger.info("MySQL database initialized: " + config.getHost() + ":" + config.getPort() + "/" + config.getDatabaseName());
    }

    private void executeSchema() throws Exception {
        try (InputStream is = plugin.getResource("schema/mysql.sql")) {
            if (is == null) throw new RuntimeException("MySQL schema file not found!");
            String schema = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String sql : schema.split(";")) {
                    sql = sql.trim();
                    if (!sql.isEmpty() && !sql.startsWith("--")) {
                        stmt.execute(sql);
                    }
                }
            }
        }
    }

    @Override
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public void loadPlayer(PlayerProfile profile) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, total_playtime, trust_score, frozen, exempt_until FROM ag_players WHERE uuid = ?")) {
            ps.setString(1, profile.getUuid().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                profile.setDatabaseId(rs.getInt("id"));
                profile.setTotalPlaytime(rs.getLong("total_playtime"));
                profile.getTrustScore().setScore(rs.getDouble("trust_score"));
                profile.setFrozen(rs.getInt("frozen") == 1);
                profile.setExemptUntil(rs.getLong("exempt_until"));
            }
        } catch (SQLException e) {
            logger.severe("Failed to load player " + profile.getUsername() + ": " + e.getMessage());
        }
    }

    @Override
    public void savePlayer(PlayerProfile profile) {
        String sql = """
            INSERT INTO ag_players (uuid, username, platform, first_join, last_join, total_playtime, trust_score, frozen, exempt_until, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, UNIX_TIMESTAMP())
            ON DUPLICATE KEY UPDATE
                username = VALUES(username),
                platform = VALUES(platform),
                last_join = VALUES(last_join),
                total_playtime = VALUES(total_playtime),
                trust_score = VALUES(trust_score),
                frozen = VALUES(frozen),
                exempt_until = VALUES(exempt_until),
                updated_at = UNIX_TIMESTAMP()
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profile.getUuid().toString());
            ps.setString(2, profile.getUsername());
            ps.setString(3, profile.getPlatform().name());
            ps.setLong(4, profile.getFirstJoin() / 1000);
            ps.setLong(5, profile.getLastJoin() / 1000);
            ps.setLong(6, profile.getTotalPlaytime());
            ps.setDouble(7, profile.getTrustScore().getScore());
            ps.setInt(8, profile.isFrozen() ? 1 : 0);
            ps.setLong(9, profile.getExemptUntil());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to save player " + profile.getUsername() + ": " + e.getMessage());
        }
    }

    @Override
    public void insertViolation(UUID playerUuid, String checkName, String category, double vl,
                                String severity, String details, String world,
                                double x, double y, double z, float yaw, float pitch,
                                int ping, double tps) {
        String sql = """
            INSERT INTO ag_violations (player_id, check_name, category, vl, severity, details, world, x, y, z, yaw, pitch, ping, tps)
            SELECT id, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? FROM ag_players WHERE uuid = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, checkName);
            ps.setString(2, category);
            ps.setDouble(3, vl);
            ps.setString(4, severity);
            ps.setString(5, details);
            ps.setString(6, world);
            ps.setDouble(7, x);
            ps.setDouble(8, y);
            ps.setDouble(9, z);
            ps.setFloat(10, yaw);
            ps.setFloat(11, pitch);
            ps.setInt(12, ping);
            ps.setDouble(13, tps);
            ps.setString(14, playerUuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to insert violation: " + e.getMessage());
        }
    }

    @Override
    public void insertAlert(UUID playerUuid, String checkName, String category, double vl, String message) {
        String sql = "INSERT INTO ag_alerts (player_id, check_name, category, vl, message) SELECT id, ?, ?, ?, ? FROM ag_players WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, checkName);
            ps.setString(2, category);
            ps.setDouble(3, vl);
            ps.setString(4, message);
            ps.setString(5, playerUuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to insert alert: " + e.getMessage());
        }
    }

    @Override
    public void insertPunishment(UUID playerUuid, String action, String reason, long duration,
                                 UUID staffUuid, String staffName, boolean automatic,
                                 String evidenceId, String triggerChecks) {
        String sql = """
            INSERT INTO ag_punishments (player_id, action, reason, duration, staff_uuid, staff_name, automatic, evidence_id, trigger_checks, expires_at)
            SELECT id, ?, ?, ?, ?, ?, ?, ?, ?, ? FROM ag_players WHERE uuid = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setString(2, reason);
            ps.setLong(3, duration);
            ps.setString(4, staffUuid != null ? staffUuid.toString() : null);
            ps.setString(5, staffName);
            ps.setInt(6, automatic ? 1 : 0);
            ps.setString(7, evidenceId);
            ps.setString(8, triggerChecks);
            ps.setLong(9, duration > 0 ? (System.currentTimeMillis() / 1000) + duration : 0);
            ps.setString(10, playerUuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to insert punishment: " + e.getMessage());
        }
    }

    @Override
    public void insertEvidence(String evidenceId, UUID playerUuid, String world,
                               double x, double y, double z, float yaw, float pitch,
                               String targetUuid, String targetName,
                               String nearbyEntities, String recentPackets, String recentPath,
                               String triggeredChecks, String debugContext) {
        String sql = """
            INSERT INTO ag_evidence (evidence_id, player_id, world, x, y, z, yaw, pitch, target_uuid, target_name,
                nearby_entities, recent_packets, recent_path, triggered_checks, debug_context)
            SELECT ?, id, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? FROM ag_players WHERE uuid = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, evidenceId);
            ps.setString(2, world);
            ps.setDouble(3, x);
            ps.setDouble(4, y);
            ps.setDouble(5, z);
            ps.setFloat(6, yaw);
            ps.setFloat(7, pitch);
            ps.setString(8, targetUuid);
            ps.setString(9, targetName);
            ps.setString(10, nearbyEntities);
            ps.setString(11, recentPackets);
            ps.setString(12, recentPath);
            ps.setString(13, triggeredChecks);
            ps.setString(14, debugContext);
            ps.setString(15, playerUuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to insert evidence: " + e.getMessage());
        }
    }

    @Override
    public void insertWebhookEvent(String webhookType, String payload, String status) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO ag_webhook_events (webhook_type, payload, status) VALUES (?, ?, ?)")) {
            ps.setString(1, webhookType);
            ps.setString(2, payload);
            ps.setString(3, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to insert webhook event: " + e.getMessage());
        }
    }

    @Override
    public void flushBatch() {
        // MySQL uses immediate writes with connection pooling
    }

    @Override
    public int getViolationCount(UUID playerUuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM ag_violations v JOIN ag_players p ON v.player_id = p.id WHERE p.uuid = ?")) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.severe("Failed to count violations: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int getPunishmentCount(UUID playerUuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM ag_punishments p2 JOIN ag_players p ON p2.player_id = p.id WHERE p.uuid = ?")) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.severe("Failed to count punishments: " + e.getMessage());
        }
        return 0;
    }
}
