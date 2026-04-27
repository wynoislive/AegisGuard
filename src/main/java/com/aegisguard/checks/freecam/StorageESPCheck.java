package com.aegisguard.checks.freecam;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "StorageESP", category = CheckCategory.FREECAM, configName = "storage-esp")
public final class StorageESPCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        // StorageESP: player targets buried/hidden containers with abnormal accuracy
        // Requires tracking container access patterns vs container visibility
        return CheckResult.pass();
    }
}
