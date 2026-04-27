package com.aegisguard.checks.combat;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.CombatData;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "AutoClicker", category = CheckCategory.COMBAT, configName = "autoclicker")
public final class AutoClickerCheck extends Check {
    private static final int MAX_CPS = 22;
    private static final double MIN_VARIANCE = 15.0;

    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        CombatData data = profile.getCombatData();
        int cps = data.getClicksPerSecond();
        if (cps <= MAX_CPS) return CheckResult.pass();
        // High CPS with very consistent intervals = autoclicker
        double variance = data.getClickVariance();
        if (cps > MAX_CPS && variance < MIN_VARIANCE) {
            return CheckResult.fail(ViolationLevel.STRONG, Math.min(1.0, (cps - MAX_CPS) / 10.0),
                    "cps=" + cps + " variance=" + String.format("%.1f", variance));
        }
        if (cps > MAX_CPS) {
            return CheckResult.fail(ViolationLevel.MEDIUM, 0.6,
                    "highCPS=" + cps);
        }
        return CheckResult.pass();
    }
}
