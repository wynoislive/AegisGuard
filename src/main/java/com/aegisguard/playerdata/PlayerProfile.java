package com.aegisguard.playerdata;

import com.aegisguard.evidence.EvidenceRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Complete player profile containing all tracking data.
 * Thread-safe for concurrent access from check threads.
 */
public final class PlayerProfile {

    private final UUID uuid;
    private volatile String username;
    private volatile PlatformType platform;
    private final long firstJoin;
    private volatile long lastJoin;
    private volatile long totalPlaytime;
    private volatile int ping;
    private volatile double serverTps;

    // Sub-data systems
    private final TrustScore trustScore;
    private final MovementData movementData;
    private final CombatData combatData;
    private final MiningData miningData;
    private final PacketData packetData;

    // Violation levels by category
    private final Map<String, Double> violationLevels = new ConcurrentHashMap<>();
    private final Map<String, Long> lastFlagTime = new ConcurrentHashMap<>();
    private final Map<String, Integer> flagCounts = new ConcurrentHashMap<>();

    // Evidence records
    private final List<EvidenceRecord> evidenceRecords = new CopyOnWriteArrayList<>();

    // State flags
    private volatile boolean frozen;
    private volatile long exemptUntil;
    private volatile boolean staffWatching;
    private volatile boolean alertsEnabled = true;
    private volatile boolean verboseTarget;
    private volatile long sessionStart;
    private volatile int databaseId = -1;

    // Ping history for averaging
    private final Deque<Integer> pingHistory = new ArrayDeque<>();
    private static final int MAX_PING_HISTORY = 20;

    // Alert cooldowns per check
    private final Map<String, Long> alertCooldowns = new ConcurrentHashMap<>();

    public PlayerProfile(UUID uuid, String username, PlatformType platform) {
        this.uuid = uuid;
        this.username = username;
        this.platform = platform;
        this.firstJoin = System.currentTimeMillis();
        this.lastJoin = System.currentTimeMillis();
        this.sessionStart = System.currentTimeMillis();
        this.totalPlaytime = 0;
        this.trustScore = new TrustScore(50.0);
        this.movementData = new MovementData();
        this.combatData = new CombatData();
        this.miningData = new MiningData();
        this.packetData = new PacketData();
    }

    // --- Violation Management ---

    /**
     * Add violation level to a check.
     */
    public double addViolation(String checkName, double amount) {
        double current = violationLevels.getOrDefault(checkName, 0.0);
        double newVl = current + amount;
        violationLevels.put(checkName, newVl);
        lastFlagTime.put(checkName, System.currentTimeMillis());
        flagCounts.merge(checkName, 1, Integer::sum);
        return newVl;
    }

    /**
     * Get violation level for a check.
     */
    public double getViolation(String checkName) {
        return violationLevels.getOrDefault(checkName, 0.0);
    }

    /**
     * Get total violation level for a category.
     */
    public double getCategoryVL(String category) {
        return violationLevels.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains(category.toLowerCase()))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    /**
     * Get total violation level across all checks.
     */
    public double getTotalVL() {
        return violationLevels.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * Decay all violation levels.
     */
    public void decayViolations(double decayRate) {
        violationLevels.replaceAll((key, vl) -> Math.max(0, vl - decayRate));
        violationLevels.entrySet().removeIf(e -> e.getValue() <= 0);
    }

    /**
     * Decay a specific check's VL.
     */
    public void decayViolation(String checkName, double decayRate) {
        violationLevels.computeIfPresent(checkName, (k, vl) -> {
            double newVl = vl - decayRate;
            return newVl <= 0 ? null : newVl;
        });
    }

    /**
     * Get last flag time for a check.
     */
    public long getLastFlagTime(String checkName) {
        return lastFlagTime.getOrDefault(checkName, 0L);
    }

    /**
     * Get total flag count for a check.
     */
    public int getFlagCount(String checkName) {
        return flagCounts.getOrDefault(checkName, 0);
    }

    /**
     * Check if an alert cooldown is active for a check.
     */
    public boolean isAlertCooldown(String checkName, long cooldownMs) {
        long last = alertCooldowns.getOrDefault(checkName, 0L);
        return System.currentTimeMillis() - last < cooldownMs;
    }

    /**
     * Set alert cooldown for a check.
     */
    public void setAlertCooldown(String checkName) {
        alertCooldowns.put(checkName, System.currentTimeMillis());
    }

    // --- Ping ---

    public void updatePing(int ping) {
        this.ping = ping;
        pingHistory.addLast(ping);
        if (pingHistory.size() > MAX_PING_HISTORY) pingHistory.pollFirst();
    }

    public int getAveragePing() {
        if (pingHistory.isEmpty()) return ping;
        int sum = 0;
        for (int p : pingHistory) sum += p;
        return sum / pingHistory.size();
    }

    // --- Evidence ---

    public void addEvidence(EvidenceRecord record) {
        evidenceRecords.add(record);
        if (evidenceRecords.size() > 100) {
            evidenceRecords.removeFirst();
        }
    }

    // --- Exemption ---

    public boolean isExempt() {
        return exemptUntil > 0 && System.currentTimeMillis() < exemptUntil;
    }

    // --- Session ---

    public long getSessionDuration() {
        return System.currentTimeMillis() - sessionStart;
    }

    // --- Getters/Setters ---

    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public PlatformType getPlatform() { return platform; }
    public void setPlatform(PlatformType p) { this.platform = p; }
    public long getFirstJoin() { return firstJoin; }
    public long getLastJoin() { return lastJoin; }
    public void setLastJoin(long t) { this.lastJoin = t; }
    public long getTotalPlaytime() { return totalPlaytime; }
    public void setTotalPlaytime(long t) { this.totalPlaytime = t; }
    public void addPlaytime(long ms) { this.totalPlaytime += ms; }
    public int getPing() { return ping; }
    public double getServerTps() { return serverTps; }
    public void setServerTps(double tps) { this.serverTps = tps; }
    public TrustScore getTrustScore() { return trustScore; }
    public MovementData getMovementData() { return movementData; }
    public CombatData getCombatData() { return combatData; }
    public MiningData getMiningData() { return miningData; }
    public PacketData getPacketData() { return packetData; }
    public Map<String, Double> getViolationLevels() { return violationLevels; }
    public List<EvidenceRecord> getEvidenceRecords() { return evidenceRecords; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean f) { this.frozen = f; }
    public long getExemptUntil() { return exemptUntil; }
    public void setExemptUntil(long t) { this.exemptUntil = t; }
    public boolean isStaffWatching() { return staffWatching; }
    public void setStaffWatching(boolean w) { this.staffWatching = w; }
    public boolean isAlertsEnabled() { return alertsEnabled; }
    public void setAlertsEnabled(boolean a) { this.alertsEnabled = a; }
    public boolean isVerboseTarget() { return verboseTarget; }
    public void setVerboseTarget(boolean v) { this.verboseTarget = v; }
    public long getSessionStart() { return sessionStart; }
    public void setSessionStart(long t) { this.sessionStart = t; }
    public int getDatabaseId() { return databaseId; }
    public void setDatabaseId(int id) { this.databaseId = id; }
}
