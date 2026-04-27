package com.aegisguard.checks.ore;

import com.aegisguard.util.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * Analyzes vein mining patterns for ore detection anomalies.
 */
public final class VeinMineAnalyzer {

    /**
     * Count the ore vein size starting from a block.
     */
    public static int countVeinSize(Block start) {
        if (!BlockUtil.isOre(start.getType())) return 0;
        Material oreType = start.getType();
        boolean[][] visited = new boolean[16][16];
        return floodFill(start, oreType, visited, 0);
    }

    private static int floodFill(Block block, Material target, boolean[][] visited, int depth) {
        if (depth > 32) return 0;
        int relX = block.getX() & 15;
        int relZ = block.getZ() & 15;
        if (relX < 0 || relX >= 16 || relZ < 0 || relZ >= 16) return 0;
        if (visited[relX][relZ]) return 0;
        if (block.getType() != target) return 0;
        visited[relX][relZ] = true;
        int count = 1;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    count += floodFill(block.getRelative(dx, dy, dz), target, visited, depth + 1);
                }
            }
        }
        return count;
    }

    /**
     * Check if a mined block had line-of-sight from the player or was hidden.
     */
    public static boolean isHiddenOre(Location playerLoc, Block ore) {
        // An ore is "hidden" if it's surrounded by solid blocks on all 6 faces
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) continue; // Only cardinal
                    Block neighbor = ore.getRelative(dx, dy, dz);
                    if (neighbor.getType().isAir() || !neighbor.getType().isOccluding()) {
                        return false; // Exposed face = not hidden
                    }
                }
            }
        }
        return true;
    }
}
