package com.aegisguard.playerdata;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/**
 * Combat tracking data for a player session.
 */
public final class CombatData {

    private static final int MAX_CPS_HISTORY = 40;
    private static final int MAX_REACH_HISTORY = 20;

    private UUID lastTarget;
    private long lastAttackTime;
    private long lastDamageReceived;
    private int clicksThisSecond;
    private long lastClickReset;
    private double lastReach;
    private int consecutiveHits;
    private int swingCount;
    private int hitCount;
    private int missCount;
    private boolean receivedKnockback;
    private long lastKnockbackTime;
    private double knockbackHorizontal;
    private double knockbackVertical;
    private final Deque<Integer> cpsHistory = new ArrayDeque<>();
    private final Deque<Double> reachHistory = new ArrayDeque<>();
    private final Deque<Long> clickTimestamps = new ArrayDeque<>();
    private final Deque<Float> yawChanges = new ArrayDeque<>();
    private final Deque<Float> pitchChanges = new ArrayDeque<>();

    /**
     * Record a click/swing.
     */
    public void recordClick() {
        long now = System.currentTimeMillis();
        clickTimestamps.addLast(now);
        while (!clickTimestamps.isEmpty() && now - clickTimestamps.peekFirst() > 1000) {
            clickTimestamps.pollFirst();
        }
        clicksThisSecond = clickTimestamps.size();
        swingCount++;

        // CPS history (per-second snapshots)
        if (now - lastClickReset >= 1000) {
            cpsHistory.addLast(clicksThisSecond);
            if (cpsHistory.size() > MAX_CPS_HISTORY) cpsHistory.pollFirst();
            lastClickReset = now;
        }
    }

    /**
     * Record a hit on an entity.
     */
    public void recordHit(UUID target, double reach) {
        this.lastTarget = target;
        this.lastAttackTime = System.currentTimeMillis();
        this.lastReach = reach;
        this.hitCount++;
        this.consecutiveHits++;

        reachHistory.addLast(reach);
        if (reachHistory.size() > MAX_REACH_HISTORY) reachHistory.pollFirst();
    }

    /**
     * Record a miss (swing with no hit).
     */
    public void recordMiss() {
        this.missCount++;
        this.consecutiveHits = 0;
    }

    /**
     * Record aim changes for aim-assist detection.
     */
    public void recordAim(float yawDelta, float pitchDelta) {
        yawChanges.addLast(yawDelta);
        pitchChanges.addLast(pitchDelta);
        if (yawChanges.size() > MAX_CPS_HISTORY) yawChanges.pollFirst();
        if (pitchChanges.size() > MAX_CPS_HISTORY) pitchChanges.pollFirst();
    }

    /**
     * Record incoming knockback.
     */
    public void recordKnockback(double horizontal, double vertical) {
        this.receivedKnockback = true;
        this.lastKnockbackTime = System.currentTimeMillis();
        this.knockbackHorizontal = horizontal;
        this.knockbackVertical = vertical;
    }

    /**
     * Consume the knockback flag.
     */
    public void consumeKnockback() {
        this.receivedKnockback = false;
    }

    /**
     * Get the click variance (standard deviation of click intervals).
     */
    public double getClickVariance() {
        if (clickTimestamps.size() < 3) return 999.0;
        long[] timestamps = clickTimestamps.stream().mapToLong(Long::longValue).toArray();
        double[] intervals = new double[timestamps.length - 1];
        for (int i = 0; i < intervals.length; i++) {
            intervals[i] = timestamps[i + 1] - timestamps[i];
        }
        double mean = 0;
        for (double d : intervals) mean += d;
        mean /= intervals.length;
        double variance = 0;
        for (double d : intervals) {
            double diff = d - mean;
            variance += diff * diff;
        }
        return Math.sqrt(variance / intervals.length);
    }

    /**
     * Get hit/miss ratio.
     */
    public double getHitRatio() {
        int total = hitCount + missCount;
        if (total == 0) return 0;
        return (double) hitCount / total;
    }

    /**
     * Reset combat data.
     */
    public void reset() {
        lastTarget = null;
        lastAttackTime = 0;
        clicksThisSecond = 0;
        lastClickReset = 0;
        lastReach = 0;
        consecutiveHits = 0;
        swingCount = 0;
        hitCount = 0;
        missCount = 0;
        receivedKnockback = false;
        cpsHistory.clear();
        reachHistory.clear();
        clickTimestamps.clear();
        yawChanges.clear();
        pitchChanges.clear();
    }

    // --- Getters ---

    public UUID getLastTarget() { return lastTarget; }
    public long getLastAttackTime() { return lastAttackTime; }
    public long getLastDamageReceived() { return lastDamageReceived; }
    public void setLastDamageReceived(long t) { this.lastDamageReceived = t; }
    public int getClicksPerSecond() { return clickTimestamps.size(); }
    public double getLastReach() { return lastReach; }
    public int getConsecutiveHits() { return consecutiveHits; }
    public int getSwingCount() { return swingCount; }
    public int getHitCount() { return hitCount; }
    public int getMissCount() { return missCount; }
    public boolean hasReceivedKnockback() { return receivedKnockback; }
    public long getLastKnockbackTime() { return lastKnockbackTime; }
    public double getKnockbackHorizontal() { return knockbackHorizontal; }
    public double getKnockbackVertical() { return knockbackVertical; }
    public Deque<Integer> getCpsHistory() { return cpsHistory; }
    public Deque<Double> getReachHistory() { return reachHistory; }
    public Deque<Float> getYawChanges() { return yawChanges; }
    public Deque<Float> getPitchChanges() { return pitchChanges; }
}
