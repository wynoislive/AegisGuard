package com.aegisguard.playerdata;

import com.aegisguard.compat.CompatManager;
import com.aegisguard.config.ConfigManager;
import com.aegisguard.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player profile lifecycle — creation, loading, saving, and cleanup.
 */
public final class PlayerProfileManager implements Listener {

    private final Plugin plugin;
    private final DatabaseManager database;
    private final ConfigManager config;
    private final CompatManager compat;
    private final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();

    public PlayerProfileManager(Plugin plugin, DatabaseManager database, ConfigManager config, CompatManager compat) {
        this.plugin = plugin;
        this.database = database;
        this.config = config;
        this.compat = compat;

        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Load profiles for already-online players (reload scenario)
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadOrCreateProfile(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadOrCreateProfile(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerProfile profile = profiles.get(uuid);
        if (profile != null) {
            profile.addPlaytime(profile.getSessionDuration());
            saveProfileAsync(profile);
            profiles.remove(uuid);
        }
    }

    /**
     * Load or create a profile for a player.
     */
    private void loadOrCreateProfile(Player player) {
        UUID uuid = player.getUniqueId();
        PlatformType platform = compat.detectPlatform(player);
        PlayerProfile profile = new PlayerProfile(uuid, player.getName(), platform);
        profile.setLastJoin(System.currentTimeMillis());
        profile.setSessionStart(System.currentTimeMillis());

        // Load from database async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            database.loadPlayer(profile);
            profiles.put(uuid, profile);
        });
    }

    /**
     * Get a player's profile.
     */
    public PlayerProfile getProfile(UUID uuid) {
        return profiles.get(uuid);
    }

    /**
     * Get a player's profile by name.
     */
    public PlayerProfile getProfile(String name) {
        return profiles.values().stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    /**
     * Get all online profiles.
     */
    public Collection<PlayerProfile> getAllProfiles() {
        return profiles.values();
    }

    /**
     * Get the number of tracked profiles.
     */
    public int getProfileCount() {
        return profiles.size();
    }

    /**
     * Save a profile asynchronously.
     */
    public void saveProfileAsync(PlayerProfile profile) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> database.savePlayer(profile));
    }

    /**
     * Save all profiles (called on shutdown).
     */
    public void saveAll() {
        for (PlayerProfile profile : profiles.values()) {
            profile.addPlaytime(profile.getSessionDuration());
            database.savePlayer(profile);
        }
        profiles.clear();
    }

    /**
     * Decay all violation levels for all online profiles.
     */
    public void decayAllViolations(double decayRate) {
        for (PlayerProfile profile : profiles.values()) {
            profile.decayViolations(decayRate);
        }
    }

    /**
     * Get profiles with violations above a threshold.
     */
    public Collection<PlayerProfile> getSuspiciousProfiles(double minVL) {
        return profiles.values().stream()
                .filter(p -> p.getTotalVL() >= minVL)
                .sorted((a, b) -> Double.compare(b.getTotalVL(), a.getTotalVL()))
                .toList();
    }

    /**
     * Get frozen profiles.
     */
    public Collection<PlayerProfile> getFrozenProfiles() {
        return profiles.values().stream()
                .filter(PlayerProfile::isFrozen)
                .toList();
    }
}
