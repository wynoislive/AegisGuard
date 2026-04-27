package com.aegisguard.compat.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * ProtocolLib integration for packet-level monitoring.
 */
public final class ProtocolLibHook {

    private final Plugin plugin;
    private final Logger logger;
    private ProtocolManager protocolManager;

    public ProtocolLibHook(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Initialize ProtocolLib packet listeners.
     */
    public void initialize() {
        try {
            protocolManager = ProtocolLibrary.getProtocolManager();

            // Register a packet listener for movement validation
            protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                    PacketType.Play.Client.POSITION,
                    PacketType.Play.Client.POSITION_LOOK,
                    PacketType.Play.Client.LOOK) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    // Update packet data tracking
                    var player = event.getPlayer();
                    if (player == null) return;
                    var core = com.aegisguard.core.AegisGuard.get();
                    if (!core.isEnabled()) return;
                    var profile = core.getProfileManager().getProfile(player.getUniqueId());
                    if (profile != null) {
                        profile.getPacketData().recordPacket(event.getPacketType().name());

                        // Track aim changes for combat analysis
                        if (event.getPacketType() == PacketType.Play.Client.POSITION_LOOK
                                || event.getPacketType() == PacketType.Play.Client.LOOK) {
                            float yaw = player.getLocation().getYaw();
                            float pitch = player.getLocation().getPitch();
                            try {
                                float newYaw = event.getPacket().getFloat().read(0);
                                float newPitch = event.getPacket().getFloat().read(1);
                                profile.getCombatData().recordAim(
                                        newYaw - yaw, newPitch - pitch);
                            } catch (Exception ignored) {
                                // Packet structure variation
                            }
                        }
                    }
                }
            });

            logger.info("ProtocolLib packet listeners registered.");
        } catch (Exception e) {
            logger.warning("Failed to initialize ProtocolLib hook: " + e.getMessage());
        }
    }

    public ProtocolManager getProtocolManager() { return protocolManager; }
}
