package com.aegisguard.checks.ore;

import com.aegisguard.playerdata.MiningData;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rolling window ore tracking and statistical analysis.
 */
public final class OreTracker {
    /**
     * Compute anomaly score for mining activity based on rolling windows.
     */
    public static double computeAnomalyScore(MiningData data) {
        double score = 0;
        int totalMined = data.getTotalBlocksMined();
        if (totalMined < 100) return 0;
        // Diamond anomaly
        double diaRate = data.getDiamondsPer1000();
        if (diaRate > 10) score += (diaRate - 10) * 2;
        // Hidden ore rate
        double hiddenRate = data.getHiddenOreRate();
        if (hiddenRate > 0.5) score += hiddenRate * 20;
        // Direct path ratio
        int directPaths = data.getDirectPathsToOre();
        int sessions = data.getTotalMiningSessions();
        if (sessions > 3 && directPaths > sessions * 0.6) score += 15;
        // Bait ore
        if (data.getBaitOreTriggered() > 0) score += 40;
        return score;
    }
}
