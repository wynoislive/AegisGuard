package com.aegisguard.checks.ore;

import com.aegisguard.core.AegisGuard;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitors rapid ore mining within short time windows.
 * Ported from OreHider 1.4 (com.wyno.orehider.MiningListener).
 */
public final class OreThresholdCheck implements Listener {

    private final Plugin plugin;
    // UUID -> Material -> List of Timestamps
    private final Map<UUID, Map<Material, List<Long>>> miningHistory = new ConcurrentHashMap<>();

    public OreThresholdCheck(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMine(BlockBreakEvent event) {
        AegisGuard core = AegisGuard.get();
        if (!core.getConfigManager().getChecksConfig().getConfig().getBoolean("ore-hider.heuristics.enabled", true)) return;

        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        String blockName = material.name().toLowerCase();
        List<String> hiddenBlocks = core.getConfigManager().getChecksConfig().getConfig().getStringList("ore-hider.global.hidden-blocks");

        if (hiddenBlocks.contains(blockName) || (blockName.contains("_ore") && !blockName.contains("copper"))) {
            checkSuspiciousActivity(player, material, event);
        }
    }

    private void checkSuspiciousActivity(Player player, Material material, BlockBreakEvent event) {
        AegisGuard core = AegisGuard.get();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        int timeWindow = core.getConfigManager().getChecksConfig().getConfig().getInt("ore-hider.heuristics.time-window", 60) * 1000;
        int threshold = core.getConfigManager().getChecksConfig().getConfig().getInt("ore-hider.heuristics.threshold", 5);

        miningHistory.putIfAbsent(uuid, new ConcurrentHashMap<>());
        Map<Material, List<Long>> playerHistory = miningHistory.get(uuid);
        playerHistory.putIfAbsent(material, Collections.synchronizedList(new ArrayList<>()));

        List<Long> timestamps = playerHistory.get(material);
        
        synchronized (timestamps) {
            timestamps.add(now);
            timestamps.removeIf(time -> (now - time) > timeWindow);

            if (timestamps.size() >= threshold) {
                core.getAlertManager().handleAlert(
                        player,
                        "OreThreshold",
                        "X-RAY",
                        (double) timestamps.size() / threshold,
                        String.format("Mined %d %s in %ds", timestamps.size(), material.name(), timeWindow / 1000)
                );

                if (core.getConfigManager().getChecksConfig().getConfig().getBoolean("ore-hider.heuristics.cancel-event", false)) {
                    event.setCancelled(true);
                }
                
                timestamps.clear();
            }
        }
    }
}
