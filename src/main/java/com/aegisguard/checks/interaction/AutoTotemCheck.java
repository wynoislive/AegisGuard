package com.aegisguard.checks.interaction;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CheckInfo(name = "AutoTotem", category = CheckCategory.INTERACTION, configName = "autototem")
public final class AutoTotemCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        // Detect instant totem swap to offhand after totem pops
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() == Material.TOTEM_OF_UNDYING) {
            long lastDamage = profile.getCombatData().getLastDamageReceived();
            if (lastDamage > 0 && System.currentTimeMillis() - lastDamage < 100) {
                return CheckResult.fail(ViolationLevel.MEDIUM, 0.7,
                        "autoTotem: swapTime=" + (System.currentTimeMillis() - lastDamage) + "ms");
            }
        }
        return CheckResult.pass();
    }
}
