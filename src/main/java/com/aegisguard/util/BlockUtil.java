package com.aegisguard.util;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;

/**
 * Block type utility methods for check logic.
 */
public final class BlockUtil {

    private BlockUtil() {}

    private static final Set<Material> ORES = EnumSet.of(
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.ANCIENT_DEBRIS
    );

    private static final Set<Material> VALUABLE_ORES = EnumSet.of(
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.ANCIENT_DEBRIS,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE
    );

    private static final Set<Material> CONTAINERS = EnumSet.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST,
            Material.BARREL, Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX, Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX,
            Material.HOPPER, Material.DISPENSER, Material.DROPPER,
            Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER,
            Material.BREWING_STAND
    );

    private static final Set<Material> PLACEABLE_BLOCKS = EnumSet.of(
            Material.COBBLESTONE, Material.STONE, Material.DIRT, Material.OAK_PLANKS,
            Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS, Material.JUNGLE_PLANKS,
            Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS,
            Material.NETHERRACK, Material.END_STONE,
            Material.SAND, Material.GRAVEL,
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG
    );

    /**
     * Check if a material is an ore.
     */
    public static boolean isOre(Material material) {
        return ORES.contains(material);
    }

    /**
     * Check if a material is a valuable ore.
     */
    public static boolean isValuableOre(Material material) {
        return VALUABLE_ORES.contains(material);
    }

    /**
     * Check if a material is a container/storage block.
     */
    public static boolean isContainer(Material material) {
        return CONTAINERS.contains(material);
    }

    /**
     * Check if a material is commonly used for scaffold bridging.
     */
    public static boolean isScaffoldBlock(Material material) {
        return PLACEABLE_BLOCKS.contains(material) || material.isBlock();
    }

    /**
     * Get the ore type name (normalized for tracking).
     */
    public static String getOreType(Material material) {
        String name = material.name().toLowerCase();
        if (name.contains("diamond")) return "diamond";
        if (name.contains("emerald")) return "emerald";
        if (name.contains("ancient_debris")) return "ancient_debris";
        if (name.contains("gold")) return "gold";
        if (name.contains("iron")) return "iron";
        if (name.contains("coal")) return "coal";
        if (name.contains("lapis")) return "lapis";
        if (name.contains("redstone")) return "redstone";
        if (name.contains("copper")) return "copper";
        return "unknown";
    }

    /**
     * Check if a material is a full solid block (not slab, stair, etc).
     */
    public static boolean isFullBlock(Material material) {
        return material.isSolid() && material.isOccluding();
    }

    /**
     * Check if a material is air or cave air.
     */
    public static boolean isAir(Material material) {
        return material.isAir();
    }
}
