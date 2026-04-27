package com.aegisguard.checks.movement;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.MovementData;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "Speed", category = CheckCategory.MOVEMENT, configName = "speed")
public final class SpeedCheck extends Check {

    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        MovementData data = profile.getMovementData();
        if (data.isInVehicle() || data.isGliding()) return CheckResult.pass();

        double speed = data.getLastHorizontalSpeed();
        double maxSpeed = MovementSimulator.getMaxHorizontalSpeed(player, profile);

        if (speed <= maxSpeed) return CheckResult.pass();

        double ratio = speed / maxSpeed;
        if (ratio > 2.0) {
            return CheckResult.fail(ViolationLevel.STRONG, Math.min(1.0, ratio / 3.0),
                    "speed=" + String.format("%.3f", speed) + " max=" + String.format("%.3f", maxSpeed)
                            + " ratio=" + String.format("%.2f", ratio));
        }
        if (ratio > 1.3) {
            return CheckResult.fail(ViolationLevel.MEDIUM, Math.min(1.0, (ratio - 1.0) / 2.0),
                    "speed=" + String.format("%.3f", speed) + " max=" + String.format("%.3f", maxSpeed));
        }

        return CheckResult.pass();
    }
}
