package com.aegisguard.checks.movement;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.MovementData;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

@CheckInfo(name = "InventoryWalk", category = CheckCategory.MOVEMENT, configName = "inventory-walk")
public final class InventoryWalkCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        MovementData data = profile.getMovementData();
        var openInv = player.getOpenInventory();
        if (openInv.getType() == InventoryType.CRAFTING) return CheckResult.pass(); // player inventory is always open
        // Player has another inventory open and is moving
        if (data.getLastHorizontalSpeed() > 0.15 && data.isSprinting()) {
            return CheckResult.fail(ViolationLevel.MINOR, 0.5,
                    "invWalk: speed=" + String.format("%.3f", data.getLastHorizontalSpeed())
                            + " inv=" + openInv.getType().name());
        }
        return CheckResult.pass();
    }
}
