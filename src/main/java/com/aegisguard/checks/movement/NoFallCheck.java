package com.aegisguard.checks.movement;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.MovementData;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.util.LocationUtil;
import org.bukkit.entity.Player;

@CheckInfo(name = "NoFall", category = CheckCategory.MOVEMENT, configName = "nofall")
public final class NoFallCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        MovementData data = profile.getMovementData();
        if (data.isInVehicle() || data.isGliding()) return CheckResult.pass();
        if (LocationUtil.isInLiquid(player.getLocation()) || LocationUtil.isOnClimbable(player.getLocation())) return CheckResult.pass();
        if (LocationUtil.isOnSlime(player.getLocation())) return CheckResult.pass();
        // Detect claiming on-ground while in air
        if (player.isOnGround() && data.getAirTicks() > 10 && data.getLastVerticalSpeed() < -0.5) {
            return CheckResult.fail(ViolationLevel.MEDIUM, 0.7,
                    "claimGround=true airTicks=" + data.getAirTicks() + " fallSpeed=" + String.format("%.3f", data.getLastVerticalSpeed()));
        }
        return CheckResult.pass();
    }
}
