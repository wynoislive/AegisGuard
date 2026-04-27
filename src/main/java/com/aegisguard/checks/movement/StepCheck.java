package com.aegisguard.checks.movement;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.MovementData;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "Step", category = CheckCategory.MOVEMENT, configName = "step")
public final class StepCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        MovementData data = profile.getMovementData();
        if (data.isInVehicle() || data.isGliding()) return CheckResult.pass();
        double vertSpeed = data.getLastVerticalSpeed();
        if (vertSpeed <= 0) return CheckResult.pass();
        // Normal step is 0.5 blocks max, jump is ~0.42 initial velocity
        if (vertSpeed > 0.6 && data.wasOnGround() && data.getAirTicks() <= 1) {
            return CheckResult.fail(ViolationLevel.MEDIUM, 0.7,
                    "step=" + String.format("%.3f", vertSpeed) + " prevGround=" + data.wasOnGround());
        }
        return CheckResult.pass();
    }
}
