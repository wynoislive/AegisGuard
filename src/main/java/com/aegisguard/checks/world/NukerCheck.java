package com.aegisguard.checks.world;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "Nuker", category = CheckCategory.WORLD, configName = "nuker")
public final class NukerCheck extends Check {
    private static final int MAX_BLOCKS_PER_SECOND = 25;
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        // Track block break rate from mining data
        int totalMined = profile.getMiningData().getTotalBlocksMined();
        long sessionMs = profile.getSessionDuration();
        if (sessionMs < 5000) return CheckResult.pass();
        double blocksPerSecond = (totalMined * 1000.0) / sessionMs;
        if (blocksPerSecond > MAX_BLOCKS_PER_SECOND) {
            return CheckResult.fail(ViolationLevel.STRONG, Math.min(1.0, blocksPerSecond / 50.0),
                    "nuker: bps=" + String.format("%.1f", blocksPerSecond));
        }
        return CheckResult.pass();
    }
}
