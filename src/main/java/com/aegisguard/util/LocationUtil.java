package com.aegisguard.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * Location and spatial utility methods.
 */
public final class LocationUtil {

    private LocationUtil() {}

    /**
     * Check if a location is on solid ground.
     */
    public static boolean isOnGround(Location location) {
        Block below = location.clone().subtract(0, 0.1, 0).getBlock();
        return below.getType().isSolid();
    }

    /**
     * Check if a location is in water.
     */
    public static boolean isInWater(Location location) {
        Material type = location.getBlock().getType();
        return type == Material.WATER;
    }

    /**
     * Check if a location is in lava.
     */
    public static boolean isInLava(Location location) {
        Material type = location.getBlock().getType();
        return type == Material.LAVA;
    }

    /**
     * Check if the player is in a liquid (water or lava).
     */
    public static boolean isInLiquid(Location location) {
        return isInWater(location) || isInLava(location);
    }

    /**
     * Check if a player is on a climbable block (ladder or vine).
     */
    public static boolean isOnClimbable(Location location) {
        Material type = location.getBlock().getType();
        return type == Material.LADDER || type == Material.VINE
                || type == Material.TWISTING_VINES || type == Material.WEEPING_VINES
                || type == Material.TWISTING_VINES_PLANT || type == Material.WEEPING_VINES_PLANT
                || type == Material.CAVE_VINES || type == Material.CAVE_VINES_PLANT;
    }

    /**
     * Check if a player is in a bubble column.
     */
    public static boolean isInBubbleColumn(Location location) {
        return location.getBlock().getType() == Material.BUBBLE_COLUMN;
    }

    /**
     * Check if a player is on slime block.
     */
    public static boolean isOnSlime(Location location) {
        Block below = location.clone().subtract(0, 0.1, 0).getBlock();
        return below.getType() == Material.SLIME_BLOCK;
    }

    /**
     * Check if a player is on honey block.
     */
    public static boolean isOnHoney(Location location) {
        Block below = location.clone().subtract(0, 0.1, 0).getBlock();
        return below.getType() == Material.HONEY_BLOCK;
    }

    /**
     * Check if a player is in powder snow.
     */
    public static boolean isInPowderSnow(Location location) {
        return location.getBlock().getType() == Material.POWDER_SNOW;
    }

    /**
     * Check if a player is on ice.
     */
    public static boolean isOnIce(Location location) {
        Block below = location.clone().subtract(0, 0.1, 0).getBlock();
        Material type = below.getType();
        return type == Material.ICE || type == Material.PACKED_ICE
                || type == Material.BLUE_ICE || type == Material.FROSTED_ICE;
    }

    /**
     * Check if a player is near a block face (like a wall).
     */
    public static boolean isNearWall(Location location) {
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block relative = location.getBlock().getRelative(face);
            if (relative.getType().isSolid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if there's a solid block above.
     */
    public static boolean hasBlockAbove(Location location) {
        Block above = location.clone().add(0, 2, 0).getBlock();
        return above.getType().isSolid();
    }

    /**
     * Check if a player has a bed nearby (for spawn point checks).
     */
    public static boolean isNearBed(Location location, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = location.clone().add(x, y, z).getBlock();
                    if (block.getType().name().contains("BED")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Format a location as a readable string.
     */
    public static String formatLocation(Location location) {
        if (location == null) return "null";
        World world = location.getWorld();
        String worldName = world != null ? world.getName() : "unknown";
        return String.format("%s %.1f, %.1f, %.1f", worldName,
                location.getX(), location.getY(), location.getZ());
    }

    /**
     * Check if two locations are in the same chunk.
     */
    public static boolean sameChunk(Location a, Location b) {
        if (a == null || b == null) return false;
        if (a.getWorld() != b.getWorld()) return false;
        return (a.getBlockX() >> 4) == (b.getBlockX() >> 4)
                && (a.getBlockZ() >> 4) == (b.getBlockZ() >> 4);
    }
}
