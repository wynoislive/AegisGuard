package com.aegisguard.checks.bedrock;

import com.aegisguard.checks.Check;
import com.aegisguard.config.ConfigManager;

import java.util.Set;

/**
 * Handles platform-aware exemptions for Bedrock/mobile players.
 * These players have different input characteristics that would
 * cause false positives on desktop-precision checks.
 */
public final class BedrockExemptions {

    private static final Set<String> PRECISION_CHECKS = Set.of(
            "AimAssist", "AutoClicker", "InventoryWalk", "Critical"
    );

    private final ConfigManager config;

    public BedrockExemptions(ConfigManager config) {
        this.config = config;
    }

    /**
     * Check if a specific check should be exempted for Bedrock players.
     */
    public boolean isExempt(Check check) {
        if (!config.getMainConfig().getBoolean("platform.disable-touch-precision-checks", true)) {
            return false;
        }
        return PRECISION_CHECKS.contains(check.getName());
    }
}
