package com.aegisguard.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed wrapper for database.yml configuration.
 */
public final class DatabaseConfig {

    private final FileConfiguration config;

    public DatabaseConfig(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Get database type (SQLITE or MYSQL).
     */
    public String getType() {
        return config.getString("database.type", "SQLITE").toUpperCase();
    }

    public boolean isMySQL() {
        return "MYSQL".equals(getType());
    }

    public boolean isSQLite() {
        return "SQLITE".equals(getType());
    }

    // MySQL connection details
    public String getHost() {
        return config.getString("database.DB_HOST", "localhost");
    }

    public int getPort() {
        return config.getInt("database.DB_PORT", 3306);
    }

    public String getUser() {
        return config.getString("database.DB_USER", "root");
    }

    public String getPassword() {
        return config.getString("database.DB_PASSWORD", "");
    }

    public String getDatabaseName() {
        return config.getString("database.DB_NAME", "aegisguard_db");
    }

    // Pool settings
    public int getMaxPoolSize() {
        return config.getInt("database.pool.maximum-pool-size", 10);
    }

    public int getMinIdle() {
        return config.getInt("database.pool.minimum-idle", 2);
    }

    public long getIdleTimeout() {
        return config.getLong("database.pool.idle-timeout", 300000);
    }

    public long getMaxLifetime() {
        return config.getLong("database.pool.max-lifetime", 600000);
    }

    public long getConnectionTimeout() {
        return config.getLong("database.pool.connection-timeout", 30000);
    }

    public long getKeepaliveTime() {
        return config.getLong("database.pool.keepalive-time", 30000);
    }

    // Batch settings
    public int getBatchSize() {
        return config.getInt("database.batch.size", 100);
    }

    public int getBatchFlushInterval() {
        return config.getInt("database.batch.flush-interval", 30);
    }

    /**
     * Build a JDBC URL for MySQL.
     */
    public String getMySQLUrl() {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true&characterEncoding=UTF-8",
                getHost(), getPort(), getDatabaseName());
    }
}
