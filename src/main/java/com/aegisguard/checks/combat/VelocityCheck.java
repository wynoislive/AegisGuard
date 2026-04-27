package com.aegisguard.checks.combat;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.CombatData;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "Velocity", category = CheckCategory.COMBAT, configName = "velocity")
public final class VelocityCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        CombatData data = profile.getCombatData();
        if (!data.hasReceivedKnockback()) return CheckResult.pass();
        long timeSinceKb = System.currentTimeMillis() - data.getLastKnockbackTime();
        if (timeSinceKb < 100 || timeSinceKb > 1000) return CheckResult.pass();
        // Player should have moved from knockback by now
        double actualHoriz = profile.getMovementData().getLastHorizontalSpeed();
        double expectedMin = data.getKnockbackHorizontal() * 0.4;
        if (actualHoriz < expectedMin && data.getKnockbackHorizontal() > 0.1) {
            data.consumeKnockback();
            return CheckResult.fail(ViolationLevel.MEDIUM, 0.7,
                    "kb: expected>" + String.format("%.2f", expectedMin) + " actual=" + String.format("%.2f", actualHoriz));
        }
        data.consumeKnockback();
        return CheckResult.pass();
    }
}
