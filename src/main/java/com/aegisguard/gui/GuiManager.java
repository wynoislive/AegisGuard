package com.aegisguard.gui;

import com.aegisguard.core.AegisGuard;
import com.aegisguard.config.ConfigManager;
import com.aegisguard.config.GuiConfig;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.playerdata.PlayerProfileManager;
import com.aegisguard.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * GUI manager for chest-based admin panels.
 */
public final class GuiManager implements Listener {

    private final Plugin plugin;
    private final ConfigManager config;
    private final PlayerProfileManager profileManager;
    private final Set<UUID> openGuis = Collections.synchronizedSet(new HashSet<>());
    private final Map<UUID, String> guiType = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, String> playerPanelTarget = Collections.synchronizedMap(new HashMap<>());

    public GuiManager(Plugin plugin, ConfigManager config, PlayerProfileManager profileManager) {
        this.plugin = plugin;
        this.config = config;
        this.profileManager = profileManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Open the main dashboard panel for a staff member.
     */
    public void openMainPanel(Player player) {
        GuiConfig gc = config.getGuiConfig();
        Inventory inv = Bukkit.createInventory(null, gc.getMainSize(),
                ColorUtil.parseLegacy(gc.getMainTitle()));

        // Fill border with glass panes
        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gc.getMainSize(); i++) inv.setItem(i, filler);

        // Suspicious players
        long suspiciousCount = profileManager.getSuspiciousProfiles(20).size();
        inv.setItem(gc.getItemSlot("main-panel", "suspicious-players"),
                createItem(Material.PLAYER_HEAD, "&c&lSuspicious Players",
                        "&7Online flagged: &f" + suspiciousCount));

        // Server risk
        double avgVl = profileManager.getAllProfiles().stream()
                .mapToDouble(PlayerProfile::getTotalVL).average().orElse(0);
        String risk = avgVl > 50 ? "&cHigh" : avgVl > 20 ? "&eModerate" : "&aLow";
        inv.setItem(gc.getItemSlot("main-panel", "server-risk"),
                createItem(Material.REDSTONE_TORCH, "&e&lServer Risk", "&7Level: " + risk));

        // Active freezes
        long frozenCount = profileManager.getFrozenProfiles().size();
        inv.setItem(gc.getItemSlot("main-panel", "active-freezes"),
                createItem(Material.BLUE_ICE, "&b&lActive Freezes", "&7Frozen: &f" + frozenCount));

        // Toggle alerts
        inv.setItem(gc.getItemSlot("main-panel", "toggle-alerts"),
                createItem(Material.COMPARATOR, "&a&lToggle Alerts", "&7Click to toggle"));

        // Close
        inv.setItem(gc.getItemSlot("main-panel", "close"),
                createItem(Material.BARRIER, "&c&lClose", "&7Close this menu"));

        player.openInventory(inv);
        openGuis.add(player.getUniqueId());
        guiType.put(player.getUniqueId(), "main");
    }

    /**
     * Open a player inspection panel.
     */
    public void openPlayerPanel(Player staff, PlayerProfile target) {
        GuiConfig gc = config.getGuiConfig();
        String title = gc.getPlayerTitle().replace("{player}", target.getUsername());
        Inventory inv = Bukkit.createInventory(null, gc.getPlayerSize(),
                ColorUtil.parseLegacy(title));

        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gc.getPlayerSize(); i++) inv.setItem(i, filler);

        // Player info head
        inv.setItem(4, createItem(Material.PLAYER_HEAD, "&e&l" + target.getUsername(),
                "&7Platform: &f" + target.getPlatform(),
                "&7Trust: &f" + String.format("%.0f", target.getTrustScore().getScore()),
                "&7Ping: &f" + target.getPing() + "ms"));

        // Violations
        inv.setItem(19, createItem(Material.PAPER, "&c&lViolations",
                "&7Total: &f" + String.format("%.0f", target.getTotalVL())));

        // Trust
        inv.setItem(21, createItem(Material.EMERALD, "&a&lTrust Score",
                "&7Score: &f" + String.format("%.0f", target.getTrustScore().getScore()) + "/100",
                "&7Level: &f" + target.getTrustScore().getTrustLevel()));

        // Evidence
        inv.setItem(23, createItem(Material.ENDER_EYE, "&d&lEvidence",
                "&7Records: &f" + target.getEvidenceRecords().size()));

        // Freeze
        inv.setItem(37, createItem(target.isFrozen() ? Material.PACKED_ICE : Material.ICE,
                target.isFrozen() ? "&b&lUnfreeze" : "&b&lFreeze",
                "&7Click to " + (target.isFrozen() ? "unfreeze" : "freeze")));

        // Back
        inv.setItem(49, createItem(Material.ARROW, "&7&lBack", "&7Return to dashboard"));

        staff.openInventory(inv);
        openGuis.add(staff.getUniqueId());
        guiType.put(staff.getUniqueId(), "player");
        playerPanelTarget.put(staff.getUniqueId(), target.getUsername());
    }

    /**
     * Open the OreHider control panel.
     */
    public void openOreHiderPanel(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ColorUtil.parseLegacy("&6&lOreHider Control Panel"));

        // Mode Switcher
        int currentMode = config.getChecksConfig().getConfig().getInt("ore-hider.global.engine-mode", 2);
        inv.setItem(11, createItem(Material.REDSTONE_TORCH, "&eEngine Mode: &f" + currentMode, 
                "&7Click to switch between Mode 1 (Hide) and 2 (Obfuscate)"));

        // Reload
        inv.setItem(13, createItem(Material.EMERALD_BLOCK, "&aForce Sync & Reload", 
                "&7Apply changes to Paper config"));

        // Status
        boolean enabled = config.getChecksConfig().getConfig().getBoolean("ore-hider.global.enabled", true);
        inv.setItem(15, createItem(Material.ENDER_EYE, "&dStatus: " + (enabled ? "&aENABLED" : "&cDISABLED"), 
                "&7Click to toggle OreHider state"));

        player.openInventory(inv);
        openGuis.add(player.getUniqueId());
        guiType.put(player.getUniqueId(), "orehider");
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!openGuis.contains(player.getUniqueId())) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        String type = guiType.get(player.getUniqueId());
        if ("main".equals(type)) {
            handleMainClick(player, event.getSlot(), clicked);
        } else if ("player".equals(type)) {
            handlePlayerClick(player, event.getSlot(), clicked);
        } else if ("orehider".equals(type)) {
            handleOreHiderClick(player, event.getSlot(), clicked);
        }
    }

    private void handleMainClick(Player player, int slot, ItemStack item) {
        GuiConfig gc = config.getGuiConfig();
        if (slot == gc.getItemSlot("main-panel", "close")) {
            player.closeInventory();
        } else if (slot == gc.getItemSlot("main-panel", "suspicious-players")) {
            // Show first suspicious player panel
            var suspicious = profileManager.getSuspiciousProfiles(10);
            if (!suspicious.isEmpty()) {
                openPlayerPanel(player, suspicious.iterator().next());
            }
        } else if (slot == gc.getItemSlot("main-panel", "toggle-alerts")) {
            PlayerProfile profile = profileManager.getProfile(player.getUniqueId());
            if (profile != null) {
                profile.setAlertsEnabled(!profile.isAlertsEnabled());
                player.sendMessage(ColorUtil.parse(profile.isAlertsEnabled()
                        ? "<green>Alerts enabled</green>" : "<yellow>Alerts disabled</yellow>"));
            }
        }
    }

    private void handlePlayerClick(Player player, int slot, ItemStack item) {
        if (slot == 49) {
            openMainPanel(player);
        } else if (slot == 37) {
            String targetName = playerPanelTarget.get(player.getUniqueId());
            if (targetName != null) {
                PlayerProfile target = profileManager.getProfile(targetName);
                if (target != null) {
                    target.setFrozen(!target.isFrozen());
                    openPlayerPanel(player, target); // Refresh
                }
            }
        }
    }

    private void handleOreHiderClick(Player player, int slot, ItemStack item) {
        if (slot == 11) {
            int current = config.getChecksConfig().getConfig().getInt("ore-hider.global.engine-mode", 2);
            int next = (current == 1) ? 2 : 1;
            config.getChecksConfig().getConfig().set("ore-hider.global.engine-mode", next);
            player.sendMessage(ColorUtil.success("Set Engine Mode to " + next));
            openOreHiderPanel(player); // Refresh
        } else if (slot == 13) {
            player.sendMessage(ColorUtil.parse("<yellow>Syncing...</yellow>"));
            AegisGuard.get().getPaperSyncService().syncAll();
            player.sendMessage(ColorUtil.success("Configs synced! Restart required."));
            player.closeInventory();
        } else if (slot == 15) {
            boolean current = config.getChecksConfig().getConfig().getBoolean("ore-hider.global.enabled", true);
            config.getChecksConfig().getConfig().set("ore-hider.global.enabled", !current);
            openOreHiderPanel(player);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        openGuis.remove(uuid);
        guiType.remove(uuid);
        playerPanelTarget.remove(uuid);
    }

    /**
     * Close all open GUIs (for reload/shutdown).
     */
    public void closeAll() {
        for (UUID uuid : new HashSet<>(openGuis)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.closeInventory();
        }
        openGuis.clear();
        guiType.clear();
        playerPanelTarget.clear();
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ColorUtil.parseLegacy(name));
        if (lore.length > 0) {
            meta.lore(Arrays.stream(lore).map(ColorUtil::parseLegacy).toList());
        }
        item.setItemMeta(meta);
        return item;
    }
}
