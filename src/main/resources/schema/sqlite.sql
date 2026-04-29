-- ============================================================
-- AegisGuard - SQLite Schema
-- Author: wyno
-- ============================================================

CREATE TABLE IF NOT EXISTS ag_players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid TEXT NOT NULL UNIQUE,
    username TEXT NOT NULL,
    platform TEXT NOT NULL DEFAULT 'JAVA',
    first_join INTEGER NOT NULL,
    last_join INTEGER NOT NULL,
    total_playtime INTEGER NOT NULL DEFAULT 0,
    trust_score REAL NOT NULL DEFAULT 50.0,
    frozen INTEGER NOT NULL DEFAULT 0,
    exempt_until INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
    updated_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
);

CREATE TABLE IF NOT EXISTS ag_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id INTEGER NOT NULL,
    session_start INTEGER NOT NULL,
    session_end INTEGER,
    avg_ping REAL NOT NULL DEFAULT 0,
    avg_tps REAL NOT NULL DEFAULT 20.0,
    blocks_mined INTEGER NOT NULL DEFAULT 0,
    ores_found INTEGER NOT NULL DEFAULT 0,
    kills INTEGER NOT NULL DEFAULT 0,
    deaths INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ag_violations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id INTEGER NOT NULL,
    check_name TEXT NOT NULL,
    category TEXT NOT NULL,
    vl REAL NOT NULL,
    severity TEXT NOT NULL,
    details TEXT,
    world TEXT,
    x REAL,
    y REAL,
    z REAL,
    yaw REAL,
    pitch REAL,
    ping INTEGER,
    tps REAL,
    timestamp INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ag_alerts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id INTEGER NOT NULL,
    check_name TEXT NOT NULL,
    category TEXT NOT NULL,
    vl REAL NOT NULL,
    message TEXT NOT NULL,
    staff_notified INTEGER NOT NULL DEFAULT 0,
    webhook_sent INTEGER NOT NULL DEFAULT 0,
    timestamp INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ag_punishments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id INTEGER NOT NULL,
    action TEXT NOT NULL,
    reason TEXT NOT NULL,
    duration INTEGER,
    staff_uuid TEXT,
    staff_name TEXT,
    automatic INTEGER NOT NULL DEFAULT 1,
    evidence_id TEXT,
    trigger_checks TEXT,
    timestamp INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
    expires_at INTEGER,
    active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ag_evidence (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    evidence_id TEXT NOT NULL UNIQUE,
    player_id INTEGER NOT NULL,
    world TEXT NOT NULL,
    x REAL NOT NULL,
    y REAL NOT NULL,
    z REAL NOT NULL,
    yaw REAL NOT NULL,
    pitch REAL NOT NULL,
    target_uuid TEXT,
    target_name TEXT,
    nearby_entities TEXT,
    recent_packets TEXT,
    recent_path TEXT,
    triggered_checks TEXT NOT NULL,
    debug_context TEXT,
    timestamp INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ag_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
);

CREATE TABLE IF NOT EXISTS ag_webhook_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    webhook_type TEXT NOT NULL,
    payload TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    retries INTEGER NOT NULL DEFAULT 0,
    error TEXT,
    created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
    sent_at INTEGER
);

CREATE TABLE IF NOT EXISTS ag_mining_stats (
    player_id INTEGER PRIMARY KEY,
    stone_mined INTEGER DEFAULT 0,
    ores_mined INTEGER DEFAULT 0,
    alerts_triggered INTEGER DEFAULT 0,
    FOREIGN KEY (player_id) REFERENCES ag_players(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_violations_player ON ag_violations(player_id);
CREATE INDEX IF NOT EXISTS idx_violations_timestamp ON ag_violations(timestamp);
CREATE INDEX IF NOT EXISTS idx_violations_check ON ag_violations(check_name);
CREATE INDEX IF NOT EXISTS idx_alerts_player ON ag_alerts(player_id);
CREATE INDEX IF NOT EXISTS idx_alerts_timestamp ON ag_alerts(timestamp);
CREATE INDEX IF NOT EXISTS idx_punishments_player ON ag_punishments(player_id);
CREATE INDEX IF NOT EXISTS idx_punishments_active ON ag_punishments(active);
CREATE INDEX IF NOT EXISTS idx_evidence_player ON ag_evidence(player_id);
CREATE INDEX IF NOT EXISTS idx_evidence_id ON ag_evidence(evidence_id);
CREATE INDEX IF NOT EXISTS idx_webhook_status ON ag_webhook_events(status);
CREATE INDEX IF NOT EXISTS idx_players_uuid ON ag_players(uuid);
CREATE INDEX IF NOT EXISTS idx_mining_stats_player ON ag_mining_stats(player_id);
