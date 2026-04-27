package com.aegisguard.checks.movement;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.MovementData;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "Blink", category = CheckCategory.MOVEMENT, configName = "blink")
public final class BlinkCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        MovementData data = profile.getMovementData();
        long timeSinceMove = System.currentTimeMillis() - data.getLastMoveTime();
        // If player hasn't sent movement packets for a while but is still connected
        if (timeSinceMove > 3000 && player.isOnline()) {
            double horizSpeed = data.getLastHorizontalSpeed();
            if (horizSpeed > 1.0) {
                return CheckResult.fail(ViolationLevel.STRONG, 0.8,
                        "blink: noPacket=" + timeSinceMove + "ms thenSpeed=" + String.format("%.3f", horizSpeed));
            }
        }
        return CheckResult.pass();
    }
}
