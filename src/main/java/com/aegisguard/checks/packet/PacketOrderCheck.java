package com.aegisguard.checks.packet;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "PacketOrder", category = CheckCategory.PACKET, configName = "packet-order")
public final class PacketOrderCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        int outOfOrder = profile.getPacketData().getOutOfOrderPackets();
        if (outOfOrder > 10) {
            return CheckResult.fail(ViolationLevel.MEDIUM, Math.min(1.0, outOfOrder / 30.0),
                    "outOfOrder=" + outOfOrder);
        }
        return CheckResult.pass();
    }
}
