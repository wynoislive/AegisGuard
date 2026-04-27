package com.aegisguard.bootstrap;

import com.aegisguard.core.AegisGuard;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin entry point for AegisGuard.
 * Delegates all logic to the AegisGuard core singleton.
 */
public final class AegisGuardPlugin extends JavaPlugin {

    private AegisGuard core;

    @Override
    public void onEnable() {
        core = AegisGuard.init(this);
        core.enable();
    }

    @Override
    public void onDisable() {
        if (core != null) {
            core.disable();
        }
    }
}
