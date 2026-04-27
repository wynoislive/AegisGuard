package com.aegisguard.checks.combat;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.CombatData;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.util.MathUtil;
import org.bukkit.entity.Player;

import java.util.Deque;

@CheckInfo(name = "AimAssist", category = CheckCategory.COMBAT, configName = "aimassist")
public final class AimAssistCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        CombatData data = profile.getCombatData();
        Deque<Float> yawChanges = data.getYawChanges();
        Deque<Float> pitchChanges = data.getPitchChanges();
        if (yawChanges.size() < 15) return CheckResult.pass();
        double[] yawArr = yawChanges.stream().mapToDouble(Float::doubleValue).toArray();
        double[] pitchArr = pitchChanges.stream().mapToDouble(Float::doubleValue).toArray();
        // GCD analysis for aim assist detection
        double gcd = 0;
        if (yawArr.length >= 2) {
            gcd = Math.abs(MathUtil.gcd(yawArr[0], yawArr[1]));
            for (int i = 2; i < Math.min(yawArr.length, 10); i++) {
                gcd = MathUtil.gcd(gcd, Math.abs(yawArr[i]));
            }
        }
        // Aim assist typically produces very consistent GCD values
        if (gcd > 0.001 && gcd < 0.01) {
            double cv = MathUtil.coefficientOfVariation(pitchArr);
            if (cv < 0.3) {
                return CheckResult.fail(ViolationLevel.MEDIUM, 0.6,
                        "aimGCD=" + String.format("%.5f", gcd) + " pitchCV=" + String.format("%.3f", cv));
            }
        }
        // Check for perfectly round yaw deltas which indicate snap aiming
        int roundCount = 0;
        for (double yaw : yawArr) {
            if (Math.abs(yaw - Math.round(yaw)) < 0.001 && yaw != 0) {
                roundCount++;
            }
        }
        if (roundCount > yawArr.length * 0.7) {
            return CheckResult.fail(ViolationLevel.MINOR, 0.5,
                    "roundYaw: " + roundCount + "/" + yawArr.length);
        }
        return CheckResult.pass();
    }
}
