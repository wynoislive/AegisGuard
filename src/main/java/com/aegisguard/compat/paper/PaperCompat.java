package com.aegisguard.compat.paper;

/**
 * Paper-specific compatibility utilities.
 */
public final class PaperCompat {

    /**
     * Check if the server supports Paper's async event system.
     */
    public boolean supportsAsyncEvents() {
        try {
            Class.forName("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if Adventure API is natively available.
     */
    public boolean hasAdventure() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
