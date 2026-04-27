package com.aegisguard.checks.ore;

import com.aegisguard.checks.*;
import com.aegisguard.config.ConfigManager;
import com.aegisguard.playerdata.MiningData;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "Xray", category = CheckCategory.ORE, configName = "xray")
public final class XrayCheck extends Check {

    private final double diamondThreshold;
    private final double debrisThreshold;

    public XrayCheck(ConfigManager config) {
        // Configurable thresholds for WynoWorldGen custom ore rates
        var oreConfig = config.getChecksConfig().getCheckSection("ore", "xray");
        double customDiamondRate = oreConfig != null ? oreConfig.getDouble("custom-ore-rates.diamond", -1) : -1;
        this.diamondThreshold = customDiamondRate > 0 ? customDiamondRate * 3.0 : 15.0;
        this.debrisThreshold = 5.0;
    }

    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        MiningData data = profile.getMiningData();
        if (data.getTotalBlocksMined() < 200) return CheckResult.pass();

        double diamondsPer1000 = data.getDiamondsPer1000();
        double debrisPerHour = data.getDebrisPerHour();
        double hiddenOreRate = data.getHiddenOreRate();

        // Check diamond rate
        if (diamondsPer1000 > diamondThreshold) {
            double confidence = Math.min(1.0, diamondsPer1000 / (diamondThreshold * 2));
            return CheckResult.fail(ViolationLevel.STRONG, confidence,
                    "xray: dia/1k=" + String.format("%.1f", diamondsPer1000)
                            + " threshold=" + String.format("%.1f", diamondThreshold)
                            + " totalMined=" + data.getTotalBlocksMined());
        }

        // Check debris rate
        if (debrisPerHour > debrisThreshold) {
            return CheckResult.fail(ViolationLevel.STRONG, Math.min(1.0, debrisPerHour / 10.0),
                    "xray: debris/h=" + String.format("%.1f", debrisPerHour));
        }

        // Check hidden ore routing confidence
        if (data.getDirectPathsToOre() > 5 && hiddenOreRate > 0.7) {
            return CheckResult.fail(ViolationLevel.CRITICAL, hiddenOreRate,
                    "oreRoute: directPaths=" + data.getDirectPathsToOre()
                            + " hiddenRate=" + String.format("%.2f", hiddenOreRate));
        }

        // Bait ore detection
        if (data.getBaitOreTriggered() > 0) {
            return CheckResult.fail(ViolationLevel.CRITICAL, 0.95,
                    "baitOre: triggered=" + data.getBaitOreTriggered());
        }

        return CheckResult.pass();
    }
}
