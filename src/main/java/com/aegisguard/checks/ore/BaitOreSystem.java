package com.aegisguard.checks.ore;

/**
 * Bait ore system for xray detection.
 * Creates fake visible ore clusters that only xray users would target.
 */
public final class BaitOreSystem {
    // Bait ore is implemented server-side by sending fake block data
    // to suspected xray users. If they mine toward bait positions,
    // it's a strong xray indicator.

    private final boolean enabled;
    private final int clusterSize;
    private final int detectionRadius;

    public BaitOreSystem(boolean enabled, int clusterSize, int detectionRadius) {
        this.enabled = enabled;
        this.clusterSize = clusterSize;
        this.detectionRadius = detectionRadius;
    }

    public boolean isEnabled() { return enabled; }
    public int getClusterSize() { return clusterSize; }
    public int getDetectionRadius() { return detectionRadius; }
}
