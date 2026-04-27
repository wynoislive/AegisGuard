package com.aegisguard.checks.combat;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "Critical", category = CheckCategory.COMBAT, configName = "critical")
public final class CriticalCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        // Criticals require: falling, not on ground, not in water/ladder/flying, not sprinting
        var moveData = profile.getMovementData();
        if (moveData.getLastVerticalSpeed() >= 0) return CheckResult.pass();
        // Check if player is consistently getting criticals without valid falling
        if (moveData.isOnGround() && moveData.getGroundTicks() > 2) {
            // Can't crit while on ground — if damage event showed crit, flag
            // This is detected from the damage event context rather than movement alone
            return CheckResult.pass();
        }
        // Micro-jump crits: going up by tiny amount then immediately attacking
        if (moveData.getAirTicks() == 1 && moveData.getLastVerticalSpeed() > 0.1
                && moveData.getLastVerticalSpeed() < 0.2) {
            // Suspicious micro-jump pattern for crit abuse
            return CheckResult.fail(ViolationLevel.MINOR, 0.4,
                    "microCrit: vertSpeed=" + String.format("%.3f", moveData.getLastVerticalSpeed()));
        }
        return CheckResult.pass();
    }
}
