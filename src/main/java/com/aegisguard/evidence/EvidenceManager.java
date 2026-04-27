package com.aegisguard.evidence;

import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.scheduler.TaskScheduler;
import com.aegisguard.storage.DatabaseManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

/**
 * Manages collection and storage of evidence records.
 */
public final class EvidenceManager {

    private final DatabaseManager database;
    private final TaskScheduler scheduler;

    public EvidenceManager(DatabaseManager database, TaskScheduler scheduler) {
        this.database = database;
        this.scheduler = scheduler;
    }

    /**
     * Collect and store an evidence record for a player.
     */
    public EvidenceRecord collectEvidence(Player player, PlayerProfile profile, String triggeredChecks, String debugContext) {
        Location loc = player.getLocation();
        String world = loc.getWorld() != null ? loc.getWorld().getName() : "unknown";

        // Gather nearby entities
        String nearby = player.getNearbyEntities(16, 16, 16).stream()
                .map(e -> e.getType().name() + ":" + String.format("%.1f", e.getLocation().distance(loc)))
                .limit(10)
                .collect(Collectors.joining(","));

        // Recent packet types
        String recentPackets = String.join(",",
                profile.getPacketData().getRecentPacketTypes().stream()
                        .limit(20)
                        .toList());

        // Recent path
        String recentPath = profile.getMovementData().getLocationHistory().stream()
                .limit(10)
                .map(l -> String.format("%.1f/%.1f/%.1f", l.getX(), l.getY(), l.getZ()))
                .collect(Collectors.joining(";"));

        // Target info
        Entity target = null;
        if (profile.getCombatData().getLastTarget() != null) {
            target = player.getServer().getEntity(profile.getCombatData().getLastTarget());
        }
        String targetUuid = target != null ? target.getUniqueId().toString() : null;
        String targetName = target != null ? target.getName() : null;

        EvidenceRecord record = EvidenceRecord.create(
                player.getUniqueId(), player.getName(),
                world, loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch(),
                targetUuid, targetName,
                nearby, recentPackets, recentPath,
                triggeredChecks, debugContext
        );

        profile.addEvidence(record);

        // Store to database async
        scheduler.runAsync(() -> database.insertEvidence(
                record.evidenceId(), record.playerUuid(), record.world(),
                record.x(), record.y(), record.z(), record.yaw(), record.pitch(),
                record.targetUuid(), record.targetName(),
                record.nearbyEntities(), record.recentPackets(), record.recentPath(),
                record.triggeredChecks(), record.debugContext()
        ));

        return record;
    }
}
