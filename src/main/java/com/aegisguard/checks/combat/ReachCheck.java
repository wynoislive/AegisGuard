package com.aegisguard.checks.combat;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.CombatData;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "Reach", category = CheckCategory.COMBAT, configName = "reach")
public final class ReachCheck extends Check {
    private static final double MAX_REACH = 3.1;
    private static final double MAX_REACH_CREATIVE = 5.1;

    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        CombatData data = profile.getCombatData();
        double reach = data.getLastReach();
        if (reach <= 0) return CheckResult.pass();
        double maxReach = player.getGameMode().isInvulnerable() ? MAX_REACH_CREATIVE : MAX_REACH;
        // Ping compensation
        int ping = profile.getPing();
        maxReach += Math.min(0.5, ping * 0.002);
        // TPS compensation
        double tps = profile.getServerTps();
        if (tps < 20 && tps > 0) maxReach += (20 - tps) * 0.05;

        if (reach > maxReach) {
            double excess = reach - maxReach;
            ViolationLevel severity = excess > 1.0 ? ViolationLevel.STRONG : ViolationLevel.MEDIUM;
            return CheckResult.fail(severity, Math.min(1.0, excess / 2.0),
                    "reach=" + String.format("%.2f", reach) + " max=" + String.format("%.2f", maxReach));
        }
        return CheckResult.pass();
    }
}
