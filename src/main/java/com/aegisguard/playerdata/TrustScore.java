package com.aegisguard.playerdata;

import com.aegisguard.util.MathUtil;

/**
 * Dynamic trust score computation for a player.
 * Scores range from 0 (untrusted) to 100 (fully trusted).
 */
public final class TrustScore {

    private double score;
    private long totalPlaytimeMinutes;
    private int clearedReports;
    private int punishmentCount;
    private int consecutiveCleanHours;
    private int linkedAccounts;
    private long lastUpdateTime = System.currentTimeMillis();

    public TrustScore(double initialScore) {
        this.score = MathUtil.clamp(initialScore, 0, 100);
    }

    /**
     * Add trust points (clamped to 0-100).
     */
    public void addTrust(double amount) {
        this.score = MathUtil.clamp(score + amount, 0, 100);
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Remove trust points (clamped to 0-100).
     */
    public void removeTrust(double amount) {
        this.score = MathUtil.clamp(score - amount, 0, 100);
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Recompute the trust score from all factors.
     */
    public void recompute(double gainPerHour, double defaultScore) {
        double computed = defaultScore;

        // Playtime bonus (up to +20)
        computed += Math.min(20, totalPlaytimeMinutes / 60.0 * gainPerHour);

        // Clean play bonus (up to +10)
        computed += Math.min(10, consecutiveCleanHours * 0.5);

        // Cleared reports bonus (up to +10)
        computed += Math.min(10, clearedReports * 2.0);

        // Punishment penalty
        computed -= punishmentCount * 10.0;

        // Linked accounts penalty
        if (linkedAccounts > 2) {
            computed -= (linkedAccounts - 2) * 5.0;
        }

        this.score = MathUtil.clamp(computed, 0, 100);
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Check if this is a high-trust player.
     */
    public boolean isHighTrust(int threshold) {
        return score >= threshold;
    }

    /**
     * Check if this is a low-trust player.
     */
    public boolean isLowTrust(int threshold) {
        return score <= threshold;
    }

    /**
     * Get the trust level as a display string.
     */
    public String getTrustLevel() {
        if (score >= 80) return "Very High";
        if (score >= 60) return "High";
        if (score >= 40) return "Moderate";
        if (score >= 20) return "Low";
        return "Very Low";
    }

    // --- Getters/Setters ---

    public double getScore() { return score; }
    public void setScore(double score) { this.score = MathUtil.clamp(score, 0, 100); }
    public long getTotalPlaytimeMinutes() { return totalPlaytimeMinutes; }
    public void setTotalPlaytimeMinutes(long t) { this.totalPlaytimeMinutes = t; }
    public int getClearedReports() { return clearedReports; }
    public void setClearedReports(int c) { this.clearedReports = c; }
    public int getPunishmentCount() { return punishmentCount; }
    public void setPunishmentCount(int p) { this.punishmentCount = p; }
    public int getConsecutiveCleanHours() { return consecutiveCleanHours; }
    public void setConsecutiveCleanHours(int c) { this.consecutiveCleanHours = c; }
    public void incrementCleanHours() { this.consecutiveCleanHours++; }
    public void resetCleanHours() { this.consecutiveCleanHours = 0; }
    public int getLinkedAccounts() { return linkedAccounts; }
    public void setLinkedAccounts(int l) { this.linkedAccounts = l; }
    public long getLastUpdateTime() { return lastUpdateTime; }
}
