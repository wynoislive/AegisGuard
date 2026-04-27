package com.aegisguard.checks.world;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.MovementData;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "Scaffold", category = CheckCategory.WORLD, configName = "scaffold")
public final class ScaffoldCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        MovementData data = profile.getMovementData();
        // Scaffold: placing blocks below while walking backward with inhuman timing
        if (data.getLastHorizontalSpeed() > 0.15 && !data.isOnGround()) {
            float pitch = player.getLocation().getPitch();
            // Looking straight down while moving fast = scaffold pattern
            if (pitch > 75.0f && data.getAirTicks() <= 2) {
                return CheckResult.fail(ViolationLevel.MINOR, 0.4,
                        "scaffold: pitch=" + String.format("%.1f", pitch) + " speed=" + String.format("%.2f", data.getLastHorizontalSpeed()));
            }
        }
        return CheckResult.pass();
    }
}
