package com.aegisguard.metrics;

import com.aegisguard.core.AegisGuard;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance metrics collection for monitoring AegisGuard health.
 */
public final class MetricsManager {

    private final Plugin plugin;
    private final AegisGuard core;
    private final AtomicLong totalChecksRun = new AtomicLong(0);
    private final AtomicLong totalViolations = new AtomicLong(0);
    private final AtomicLong totalAlerts = new AtomicLong(0);
    private volatile double lastTps = 20.0;
    private volatile int onlinePlayers = 0;
    private volatile long lastCollectTime = 0;

    public MetricsManager(Plugin plugin, AegisGuard core) {
        this.plugin = plugin;
        this.core = core;
    }

    /**
     * Start metrics collection.
     */
    public void start() {
        // Metrics are collected via the scheduled task in AegisGuard core
    }

    /**
     * Stop metrics collection.
     */
    public void stop() {
        // Cleanup if needed
    }

    /**
     * Collect current metrics snapshot.
     */
    public void collect() {
        lastTps = Bukkit.getTPS()[0];
        onlinePlayers = Bukkit.getOnlinePlayers().size();
        lastCollectTime = System.currentTimeMillis();
    }

    /**
     * Increment check counter.
     */
    public void incrementChecks() { totalChecksRun.incrementAndGet(); }

    /**
     * Increment violation counter.
     */
    public void incrementViolations() { totalViolations.incrementAndGet(); }

    /**
     * Increment alert counter.
     */
    public void incrementAlerts() { totalAlerts.incrementAndGet(); }

    // --- Getters ---

    public long getTotalChecksRun() { return totalChecksRun.get(); }
    public long getTotalViolations() { return totalViolations.get(); }
    public long getTotalAlerts() { return totalAlerts.get(); }
    public double getLastTps() { return lastTps; }
    public int getOnlinePlayers() { return onlinePlayers; }
}
