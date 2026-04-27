package com.aegisguard.checks.movement;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.MovementData;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.util.LocationUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@CheckInfo(name = "Fly", category = CheckCategory.MOVEMENT, configName = "fly")
public final class FlyCheck extends Check {

    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        if (player.getAllowFlight() || player.isFlying()
                || player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR) {
            return CheckResult.pass();
        }

        MovementData data = profile.getMovementData();
        if (data.isInVehicle() || data.isGliding()) return CheckResult.pass();

        // Check for sustained air time without valid reason
        if (data.getAirTicks() < 40) return CheckResult.pass();

        var loc = player.getLocation();
        if (LocationUtil.isInLiquid(loc) || LocationUtil.isOnClimbable(loc)
                || LocationUtil.isInBubbleColumn(loc) || LocationUtil.isInPowderSnow(loc)) {
            return CheckResult.pass();
        }

        // Check if vertical velocity defies gravity
        double vertSpeed = data.getLastVerticalSpeed();
        if (data.getAirTicks() > 20 && vertSpeed >= 0) {
            // Sustained non-negative vertical velocity without any support
            double confidence = Math.min(1.0, data.getAirTicks() / 80.0);
            return CheckResult.fail(ViolationLevel.STRONG, confidence,
                    "airTicks=" + data.getAirTicks() + " vertSpeed=" + String.format("%.3f", vertSpeed));
        }

        // Check for hovering (very low fall rate for extended period)
        if (data.getAirTicks() > 60 && Math.abs(vertSpeed) < 0.01) {
            return CheckResult.fail(ViolationLevel.MEDIUM, 0.8,
                    "hover: airTicks=" + data.getAirTicks() + " vertSpeed=" + String.format("%.5f", vertSpeed));
        }

        return CheckResult.pass();
    }
}
