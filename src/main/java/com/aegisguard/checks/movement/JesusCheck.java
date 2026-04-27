package com.aegisguard.checks.movement;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.MovementData;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.util.LocationUtil;
import org.bukkit.entity.Player;

@CheckInfo(name = "Jesus", category = CheckCategory.MOVEMENT, configName = "jesus")
public final class JesusCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        MovementData data = profile.getMovementData();
        if (data.isInVehicle() || data.isGliding() || data.isSwimming()) return CheckResult.pass();
        if (!LocationUtil.isInWater(player.getLocation())) return CheckResult.pass();
        // Walking on water: in water block but claiming ground and not sinking
        if (player.isOnGround() && data.getLastVerticalSpeed() >= -0.01 && data.getWaterTicks() > 10) {
            return CheckResult.fail(ViolationLevel.MEDIUM, 0.7,
                    "waterWalk: waterTicks=" + data.getWaterTicks() + " vertSpeed=" + String.format("%.3f", data.getLastVerticalSpeed()));
        }
        return CheckResult.pass();
    }
}
