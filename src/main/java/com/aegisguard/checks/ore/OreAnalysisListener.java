package com.aegisguard.checks.ore;

import com.aegisguard.alerts.AlertManager;
import com.aegisguard.core.AegisGuard;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

/**
 * Tracks mining ratios and analyzes suspicious mining patterns.
 * Ported from OreHider 1.4 (com.wyno.orehider.analysis.MiningListener).
 */
public final class OreAnalysisListener implements Listener {

    private final Plugin plugin;
    private final boolean hasWorldGuard;

    public OreAnalysisListener(Plugin plugin) {
        this.plugin = plugin;
        this.hasWorldGuard = plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("aegisguard.bypass.ore")) return;

        // --- WorldGuard Region Check ---
        if (hasWorldGuard) {
            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(event.getBlock().getLocation());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            com.sk89q.worldguard.LocalPlayer localPlayer = com.sk89q.worldguard.bukkit.WorldGuardPlugin.inst().wrapPlayer(player);
            if (!query.testState(loc, localPlayer, Flags.BUILD)) return;
        }

        Material mat = event.getBlock().getType();
        World.Environment env = event.getBlock().getWorld().getEnvironment();
        
        // Dimension-Aware Block Classification
        boolean isOre = mat.name().contains("_ORE") || mat == Material.ANCIENT_DEBRIS || 
                        mat == Material.NETHER_QUARTZ_ORE || mat == Material.NETHER_GOLD_ORE ||
                        mat == Material.GILDED_BLACKSTONE;
        boolean isStone = mat == Material.STONE || mat == Material.DEEPSLATE || mat == Material.TUFF || 
                         mat == Material.NETHERRACK || mat == Material.END_STONE || mat == Material.BLACKSTONE;

        if (isOre || isStone) {
            AegisGuard core = AegisGuard.get();
            core.getScheduler().runAsync(() -> {
                core.getDatabaseManager().updateMiningStats(player.getUniqueId(), isOre);
                
                if (isOre) {
                    double ratio = core.getDatabaseManager().getMiningRatio(player.getUniqueId());
                    int y = event.getBlock().getLocation().getBlockY();
                    
                    // Dimension-Aware Suspicion Logic
                    boolean suspiciousHeight = false;
                    if (env == World.Environment.NORMAL && y < 64) suspiciousHeight = true;
                    else if (env == World.Environment.NETHER && y < 128) suspiciousHeight = true;
                    else if (env == World.Environment.THE_END) suspiciousHeight = true;

                    double threshold = core.getConfigManager().getChecksConfig().getConfig().getDouble("ore-hider.analysis.ratio-threshold", 40.0);

                    if (ratio > threshold && suspiciousHeight) {
                        String dimension = env.name().toLowerCase().replace("the_", "");
                        core.getAlertManager().handleAlert(
                                player, 
                                "OreAnalysis", 
                                "X-RAY", 
                                ratio / 10.0, // VL scaled by ratio
                                String.format("High mining ratio: %.2f%% in %s", ratio, dimension)
                        );
                        core.getDatabaseManager().incrementMiningAlerts(player.getUniqueId());
                    }
                }
            });
        }
    }
}
