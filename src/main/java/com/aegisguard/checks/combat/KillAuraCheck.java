package com.aegisguard.checks.combat;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.CombatData;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.util.MathUtil;
import org.bukkit.entity.Player;

import java.util.Deque;

@CheckInfo(name = "KillAura", category = CheckCategory.COMBAT, configName = "killaura")
public final class KillAuraCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        CombatData data = profile.getCombatData();
        // Multi-target aura: rapid target switching
        if (data.getConsecutiveHits() > 3) {
            double hitRatio = data.getHitRatio();
            if (hitRatio > 0.95 && data.getClicksPerSecond() > 8) {
                return CheckResult.fail(ViolationLevel.STRONG, hitRatio,
                        "aura: hitRatio=" + String.format("%.2f", hitRatio)
                                + " cps=" + data.getClicksPerSecond()
                                + " streak=" + data.getConsecutiveHits());
            }
        }
        // Impossibly smooth aim patterns
        Deque<Float> yawChanges = data.getYawChanges();
        if (yawChanges.size() >= 10) {
            double[] yawArr = yawChanges.stream().mapToDouble(Float::doubleValue).toArray();
            double yawStdDev = MathUtil.standardDeviation(yawArr);
            if (yawStdDev < 0.5 && data.getHitCount() > 5) {
                return CheckResult.fail(ViolationLevel.MEDIUM, 0.7,
                        "smoothAim: yawStdDev=" + String.format("%.3f", yawStdDev));
            }
        }
        return CheckResult.pass();
    }
}
