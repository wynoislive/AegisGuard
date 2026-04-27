package com.aegisguard.checks.movement;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.MovementData;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@CheckInfo(name = "Phase", category = CheckCategory.MOVEMENT, configName = "phase")
public final class PhaseCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        MovementData data = profile.getMovementData();
        if (data.isInVehicle() || data.isGliding()) return CheckResult.pass();
        Location from = data.getLastLocation();
        Location to = player.getLocation();
        if (from == null || from.getWorld() != to.getWorld()) return CheckResult.pass();
        // Check if the path between from and to passes through solid blocks
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 0.5) return CheckResult.pass();
        int steps = (int) Math.ceil(dist * 4);
        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            Location check = from.clone().add(dx * t, dy * t + 0.5, dz * t);
            Material blockType = check.getBlock().getType();
            if (blockType.isSolid() && blockType.isOccluding()) {
                return CheckResult.fail(ViolationLevel.STRONG, 0.9,
                        "phased through " + blockType.name() + " at " + String.format("%.1f,%.1f,%.1f",
                                check.getX(), check.getY(), check.getZ()));
            }
        }
        return CheckResult.pass();
    }
}
