package com.aegisguard.checks;

/**
 * Check category enum for grouping violations.
 */
public enum CheckCategory {
    MOVEMENT("movement"),
    COMBAT("combat"),
    WORLD("world"),
    ORE("ore"),
    INTERACTION("interaction"),
    EXPLOIT("exploit"),
    PACKET("packet"),
    FREECAM("freecam"),
    ECONOMY("economy"),
    BEDROCK("bedrock");

    private final String configKey;

    CheckCategory(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() { return configKey; }
}
