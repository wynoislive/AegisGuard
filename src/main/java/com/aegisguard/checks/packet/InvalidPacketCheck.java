package com.aegisguard.checks.packet;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.PlayerProfile;
import org.bukkit.entity.Player;

@CheckInfo(name = "InvalidPacket", category = CheckCategory.PACKET, configName = "invalid-packet")
public final class InvalidPacketCheck extends Check {
    @Override
    public CheckResult check(Player player, PlayerProfile profile) {
        int invalid = profile.getPacketData().getInvalidPackets();
        if (invalid > 5) {
            return CheckResult.fail(ViolationLevel.STRONG, Math.min(1.0, invalid / 20.0),
                    "invalidPackets=" + invalid);
        }
        return CheckResult.pass();
    }
}
