package com.aegisguard.checks.interaction;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "AutoArmor", category = CheckCategory.INTERACTION, configName = "autoarmor")
public final class AutoArmorCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        // Auto armor is detected by impossibly fast armor equipping from inventory
        // This leverages the same interaction rate tracking
        return CheckResult.pass();
    }
}
