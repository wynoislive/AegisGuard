package com.aegisguard.playerdata;

/**
 * Platform type classification for connected players.
 */
public enum PlatformType {
    JAVA,
    BEDROCK,
    MOBILE_TOUCH,
    CONTROLLER,
    UNKNOWN;

    /**
     * Check if this platform is a Bedrock variant.
     */
    public boolean isBedrock() {
        return this == BEDROCK || this == MOBILE_TOUCH || this == CONTROLLER;
    }

    /**
     * Check if this platform uses touch input.
     */
    public boolean isTouch() {
        return this == MOBILE_TOUCH;
    }
}
