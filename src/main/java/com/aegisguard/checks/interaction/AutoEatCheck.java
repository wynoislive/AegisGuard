package com.aegisguard.checks.interaction;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "AutoEat", category = CheckCategory.INTERACTION, configName = "autoeat")
public final class AutoEatCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        // AutoEat: instant food consumption without holding use for required duration
        return CheckResult.pass();
    }
}
