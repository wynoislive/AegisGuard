package com.aegisguard.checks.interaction;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "ChestStealer", category = CheckCategory.INTERACTION, configName = "cheststealer")
public final class ChestStealerCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        // Detect impossibly fast inventory clicks (handled from InventoryClickEvent timing)
        long clickRate = profile.getPacketData().getInteractionsThisSecond();
        if (clickRate > 20) {
            return CheckResult.fail(ViolationLevel.STRONG, Math.min(1.0, clickRate / 40.0),
                    "chestStealer: clicks/s=" + clickRate);
        }
        return CheckResult.pass();
    }
}
