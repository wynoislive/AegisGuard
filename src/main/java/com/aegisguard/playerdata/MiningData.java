package com.aegisguard.playerdata;

import org.bukkit.Material;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mining and ore tracking data for xray/ore cheat detection.
 * Maintains rolling window statistics.
 */
public final class MiningData {

    private final Map<String, AtomicInteger> oresByType = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> blocksMined = new ConcurrentHashMap<>();
    private final AtomicInteger totalBlocksMined = new AtomicInteger(0);
    private final AtomicInteger totalOresFound = new AtomicInteger(0);

    // Rolling window tracking
    private final Map<String, RollingCounter> shortWindow = new ConcurrentHashMap<>();  // 15 min
    private final Map<String, RollingCounter> mediumWindow = new ConcurrentHashMap<>(); // 1 hour
    private final Map<String, RollingCounter> longWindow = new ConcurrentHashMap<>();   // 24 hour

    // Route analysis
    private int directPathsToOre;
    private int totalMiningSessions;
    private int hiddenOreFinds;
    private int baitOreTriggered;
    private double routeConfidence;

    // Y-level distribution
    private final Map<Integer, AtomicInteger> yLevelDistribution = new ConcurrentHashMap<>();

    /**
     * Record a block being mined.
     */
    public void recordBlockMined(Material material, int yLevel) {
        String name = material.name().toLowerCase();
        totalBlocksMined.incrementAndGet();
        blocksMined.computeIfAbsent(name, k -> new AtomicInteger(0)).incrementAndGet();
        yLevelDistribution.computeIfAbsent(yLevel, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * Record an ore being found.
     */
    public void recordOreFound(String oreType, int yLevel) {
        totalOresFound.incrementAndGet();
        oresByType.computeIfAbsent(oreType, k -> new AtomicInteger(0)).incrementAndGet();

        shortWindow.computeIfAbsent(oreType, k -> new RollingCounter(15 * 60 * 1000L)).increment();
        mediumWindow.computeIfAbsent(oreType, k -> new RollingCounter(60 * 60 * 1000L)).increment();
        longWindow.computeIfAbsent(oreType, k -> new RollingCounter(24 * 60 * 60 * 1000L)).increment();
    }

    /**
     * Get diamonds per 1000 blocks mined.
     */
    public double getDiamondsPer1000() {
        int diamonds = getOreCount("diamond");
        int total = totalBlocksMined.get();
        if (total == 0) return 0;
        return (diamonds * 1000.0) / total;
    }

    /**
     * Get debris per hour based on medium window.
     */
    public double getDebrisPerHour() {
        RollingCounter counter = mediumWindow.get("ancient_debris");
        return counter != null ? counter.getCount() : 0;
    }

    /**
     * Get ore count by type.
     */
    public int getOreCount(String type) {
        AtomicInteger count = oresByType.get(type);
        return count != null ? count.get() : 0;
    }

    /**
     * Get total blocks mined.
     */
    public int getTotalBlocksMined() {
        return totalBlocksMined.get();
    }

    /**
     * Get total ores found.
     */
    public int getTotalOresFound() {
        return totalOresFound.get();
    }

    // Route analysis
    public void recordDirectPath() { directPathsToOre++; }
    public void recordMiningSession() { totalMiningSessions++; }
    public void recordHiddenOreFind() { hiddenOreFinds++; }
    public void recordBaitOreTriggered() { baitOreTriggered++; }
    public void setRouteConfidence(double c) { this.routeConfidence = c; }

    public int getDirectPathsToOre() { return directPathsToOre; }
    public int getTotalMiningSessions() { return totalMiningSessions; }
    public int getHiddenOreFinds() { return hiddenOreFinds; }
    public int getBaitOreTriggered() { return baitOreTriggered; }
    public double getRouteConfidence() { return routeConfidence; }

    /**
     * Get the hidden ore success rate.
     */
    public double getHiddenOreRate() {
        if (totalMiningSessions == 0) return 0;
        return (double) hiddenOreFinds / totalMiningSessions;
    }

    /**
     * Get Y-level distribution for analysis.
     */
    public Map<Integer, AtomicInteger> getYLevelDistribution() {
        return yLevelDistribution;
    }

    /**
     * Get rolling window count for an ore type.
     */
    public int getWindowCount(String oreType, String window) {
        Map<String, RollingCounter> target = switch (window) {
            case "short" -> shortWindow;
            case "medium" -> mediumWindow;
            case "long" -> longWindow;
            default -> shortWindow;
        };
        RollingCounter counter = target.get(oreType);
        return counter != null ? counter.getCount() : 0;
    }

    /**
     * Reset all mining data.
     */
    public void reset() {
        oresByType.clear();
        blocksMined.clear();
        totalBlocksMined.set(0);
        totalOresFound.set(0);
        shortWindow.clear();
        mediumWindow.clear();
        longWindow.clear();
        directPathsToOre = 0;
        totalMiningSessions = 0;
        hiddenOreFinds = 0;
        baitOreTriggered = 0;
        routeConfidence = 0;
        yLevelDistribution.clear();
    }

    /**
     * Simple rolling window counter using timestamp-based entries.
     */
    public static final class RollingCounter {
        private final long windowMs;
        private final java.util.Deque<Long> entries = new java.util.ArrayDeque<>();

        public RollingCounter(long windowMs) {
            this.windowMs = windowMs;
        }

        public synchronized void increment() {
            long now = System.currentTimeMillis();
            entries.addLast(now);
            cleanup(now);
        }

        public synchronized int getCount() {
            cleanup(System.currentTimeMillis());
            return entries.size();
        }

        private void cleanup(long now) {
            while (!entries.isEmpty() && now - entries.peekFirst() > windowMs) {
                entries.pollFirst();
            }
        }
    }
}
