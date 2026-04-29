-- ============================================================
-- AegisGuard - MySQL Schema
-- Author: wyno
-- ============================================================

CREATE TABLE IF NOT EXISTS ag_players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    username VARCHAR(16) NOT NULL,
    platform VARCHAR(20) NOT NULL DEFAULT 'JAVA',
    first_join BIGINT NOT NULL,
    last_join BIGINT NOT NULL,
    total_playtime BIGINT NOT NULL DEFAULT 0,
    trust_score DOUBLE NOT NULL DEFAULT 50.0,
    frozen TINYINT(1) NOT NULL DEFAULT 0,
    exempt_until BIGINT NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()),
    updated_at BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()),
    INDEX idx_uuid (uuid),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ag_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    session_start BIGINT NOT NULL,
    session_end BIGINT,
    avg_ping DOUBLE NOT NULL DEFAULT 0,
    avg_tps DOUBLE NOT NULL DEFAULT 20.0,
    blocks_mined INT NOT NULL DEFAULT 0,
    ores_found INT NOT NULL DEFAULT 0,
    kills INT NOT NULL DEFAULT 0,
    deaths INT NOT NULL DEFAULT 0,
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ag_violations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    check_name VARCHAR(64) NOT NULL,
    category VARCHAR(32) NOT NULL,
    vl DOUBLE NOT NULL,
    severity VARCHAR(16) NOT NULL,
    details TEXT,
    world VARCHAR(64),
    x DOUBLE,
    y DOUBLE,
    z DOUBLE,
    yaw FLOAT,
    pitch FLOAT,
    ping INT,
    tps DOUBLE,
    timestamp BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()),
    INDEX idx_player (player_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_check (check_name),
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ag_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    check_name VARCHAR(64) NOT NULL,
    category VARCHAR(32) NOT NULL,
    vl DOUBLE NOT NULL,
    message TEXT NOT NULL,
    staff_notified TINYINT(1) NOT NULL DEFAULT 0,
    webhook_sent TINYINT(1) NOT NULL DEFAULT 0,
    timestamp BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()),
    INDEX idx_player (player_id),
    INDEX idx_timestamp (timestamp),
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ag_punishments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    reason TEXT NOT NULL,
    duration BIGINT,
    staff_uuid VARCHAR(36),
    staff_name VARCHAR(16),
    automatic TINYINT(1) NOT NULL DEFAULT 1,
    evidence_id VARCHAR(36),
    trigger_checks TEXT,
    timestamp BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()),
    expires_at BIGINT,
    active TINYINT(1) NOT NULL DEFAULT 1,
    INDEX idx_player (player_id),
    INDEX idx_active (active),
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ag_evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evidence_id VARCHAR(36) NOT NULL UNIQUE,
    player_id BIGINT NOT NULL,
    world VARCHAR(64) NOT NULL,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    z DOUBLE NOT NULL,
    yaw FLOAT NOT NULL,
    pitch FLOAT NOT NULL,
    target_uuid VARCHAR(36),
    target_name VARCHAR(16),
    nearby_entities TEXT,
    recent_packets TEXT,
    recent_path TEXT,
    triggered_checks TEXT NOT NULL,
    debug_context TEXT,
    timestamp BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()),
    INDEX idx_evidence_id (evidence_id),
    INDEX idx_player (player_id),
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ag_settings (
    `key` VARCHAR(128) PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP())
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ag_webhook_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    webhook_type VARCHAR(32) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    retries INT NOT NULL DEFAULT 0,
    error TEXT,
    created_at BIGINT NOT NULL DEFAULT (UNIX_TIMESTAMP()),
    sent_at BIGINT,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ag_mining_stats (
    player_id BIGINT PRIMARY KEY,
    stone_mined INT DEFAULT 0,
    ores_mined INT DEFAULT 0,
    alerts_triggered INT DEFAULT 0,
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
