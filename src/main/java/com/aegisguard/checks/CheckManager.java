package com.aegisguard.checks;

import com.aegisguard.alerts.AlertManager;
import com.aegisguard.checks.bedrock.BedrockExemptions;
import com.aegisguard.checks.combat.*;
import com.aegisguard.checks.economy.EconomyAbuseCheck;
import com.aegisguard.checks.exploit.*;
import com.aegisguard.checks.freecam.*;
import com.aegisguard.checks.interaction.*;
import com.aegisguard.checks.movement.*;
import com.aegisguard.checks.ore.*;
import com.aegisguard.checks.packet.*;
import com.aegisguard.checks.world.*;
import com.aegisguard.compat.CompatManager;
import com.aegisguard.config.ConfigManager;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.playerdata.PlayerProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Central check manager — registers, dispatches, and executes all check implementations.
 * Listens to relevant events and delegates to the appropriate checks.
 */
public final class CheckManager implements Listener {

    private final Plugin plugin;
    private final Logger logger;
    private final ConfigManager config;
    private final PlayerProfileManager profileManager;
    private final AlertManager alertManager;
    private final CompatManager compatManager;
    private final BedrockExemptions bedrockExemptions;

    private final List<Check> movementChecks = new ArrayList<>();
    private final List<Check> combatChecks = new ArrayList<>();
    private final List<Check> worldChecks = new ArrayList<>();
    private final List<Check> oreChecks = new ArrayList<>();
    private final List<Check> interactionChecks = new ArrayList<>();
    private final List<Check> exploitChecks = new ArrayList<>();
    private final List<Check> packetChecks = new ArrayList<>();
    private final List<Check> freecamChecks = new ArrayList<>();
    private final List<Check> economyChecks = new ArrayList<>();
    private final List<Check> allChecks = new ArrayList<>();

    public CheckManager(Plugin plugin, ConfigManager config, PlayerProfileManager profileManager,
                        AlertManager alertManager, CompatManager compatManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = config;
        this.profileManager = profileManager;
        this.alertManager = alertManager;
        this.compatManager = compatManager;
        this.bedrockExemptions = new BedrockExemptions(config);
    }

    /**
     * Register all check implementations.
     */
    public void registerAll() {
        // Movement checks
        registerCheck(movementChecks, new FlyCheck());
        registerCheck(movementChecks, new SpeedCheck());
        registerCheck(movementChecks, new NoFallCheck());
        registerCheck(movementChecks, new PhaseCheck());
        registerCheck(movementChecks, new JesusCheck());
        registerCheck(movementChecks, new StepCheck());
        registerCheck(movementChecks, new BlinkCheck());
        registerCheck(movementChecks, new InventoryWalkCheck());

        // Combat checks
        registerCheck(combatChecks, new ReachCheck());
        registerCheck(combatChecks, new KillAuraCheck());
        registerCheck(combatChecks, new AutoClickerCheck());
        registerCheck(combatChecks, new VelocityCheck());
        registerCheck(combatChecks, new CriticalCheck());
        registerCheck(combatChecks, new AimAssistCheck());

        // World checks
        registerCheck(worldChecks, new ScaffoldCheck());
        registerCheck(worldChecks, new NukerCheck());
        registerCheck(worldChecks, new AutoMineCheck());

        // Ore checks
        registerCheck(oreChecks, new XrayCheck(config));

        // Interaction checks
        registerCheck(interactionChecks, new ChestStealerCheck());
        registerCheck(interactionChecks, new AutoArmorCheck());
        registerCheck(interactionChecks, new AutoTotemCheck());
        registerCheck(interactionChecks, new AutoEatCheck());

        // Exploit checks
        registerCheck(exploitChecks, new PacketFloodCheck());
        registerCheck(exploitChecks, new CrashExploitCheck());
        registerCheck(exploitChecks, new BookExploitCheck());
        registerCheck(exploitChecks, new SignExploitCheck());
        registerCheck(exploitChecks, new ChatFloodCheck());

        // Packet checks
        registerCheck(packetChecks, new InvalidPacketCheck());
        registerCheck(packetChecks, new PacketOrderCheck());

        // Freecam checks
        registerCheck(freecamChecks, new FreecamCheck());
        registerCheck(freecamChecks, new BaseHunterCheck());
        registerCheck(freecamChecks, new StorageESPCheck());

        // Economy checks
        registerCheck(economyChecks, new EconomyAbuseCheck());

        Bukkit.getPluginManager().registerEvents(this, plugin);
        logger.info("Registered " + allChecks.size() + " checks across " + CheckCategory.values().length + " categories.");
    }

    private void registerCheck(List<Check> category, Check check) {
        check.loadConfig(config.getChecksConfig());
        if (check.isEnabled()) {
            category.add(check);
            allChecks.add(check);
        }
    }

    /**
     * Unregister all checks and listeners.
     */
    public void unregisterAll() {
        allChecks.clear();
        movementChecks.clear();
        combatChecks.clear();
        worldChecks.clear();
        oreChecks.clear();
        interactionChecks.clear();
        exploitChecks.clear();
        packetChecks.clear();
        freecamChecks.clear();
        economyChecks.clear();
    }

    /**
     * Run a set of checks against a player profile.
     */
    private void runChecks(Player player, PlayerProfile profile, List<Check> checks) {
        if (profile == null || profile.isExempt() || profile.isFrozen()) return;
        if (player.hasPermission("aegis.bypass")) return;

        for (Check check : checks) {
            if (!check.isEnabled() || check.isOnCooldown(profile)) continue;

            // Skip precision checks for bedrock players
            if (profile.getPlatform().isBedrock() && bedrockExemptions.isExempt(check)) continue;

            try {
                CheckResult result = check.check(player, profile);
                if (result.failed()) {
                    handleViolation(player, profile, check, result);
                }
            } catch (Exception e) {
                logger.warning("Check " + check.getName() + " threw exception for " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Handle a detected violation.
     */
    private void handleViolation(Player player, PlayerProfile profile, Check check, CheckResult result) {
        double vlAdded = check.calculateVL(result.severity());
        double newVl = profile.addViolation(check.getName(), vlAdded);
        profile.setAlertCooldown(check.getName());

        // Reduce trust
        profile.getTrustScore().removeTrust(vlAdded * 0.1);

        // Setback if enabled and threshold reached
        if (check.isSetbackEnabled() && newVl >= check.getThreshold()) {
            Location safe = profile.getMovementData().getSafeLocation();
            if (safe != null) {
                Bukkit.getScheduler().runTask(plugin, () -> player.teleport(safe));
            }
        }

        // Dispatch alert
        alertManager.handleViolation(player, profile, check, result, newVl);
    }

    // --- Event Handlers ---

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!hasMovement(event)) return;
        Player player = event.getPlayer();
        PlayerProfile profile = profileManager.getProfile(player.getUniqueId());
        if (profile == null) return;

        // Update movement data
        profile.getMovementData().update(event.getTo(), player.isOnGround());
        profile.updatePing(player.getPing());
        profile.setServerTps(Bukkit.getTPS()[0]);

        // Run movement and freecam checks
        runChecks(player, profile, movementChecks);
        runChecks(player, profile, freecamChecks);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        PlayerProfile profile = profileManager.getProfile(player.getUniqueId());
        if (profile == null) return;

        // Update combat data
        double reach = player.getLocation().distance(event.getEntity().getLocation());
        if (event.getEntity() instanceof Player target) {
            profile.getCombatData().recordHit(target.getUniqueId(), reach);
        } else {
            profile.getCombatData().recordHit(event.getEntity().getUniqueId(), reach);
        }

        runChecks(player, profile, combatChecks);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = profileManager.getProfile(player.getUniqueId());
        if (profile == null) return;

        profile.getMiningData().recordBlockMined(event.getBlock().getType(), event.getBlock().getY());

        if (com.aegisguard.util.BlockUtil.isOre(event.getBlock().getType())) {
            String oreType = com.aegisguard.util.BlockUtil.getOreType(event.getBlock().getType());
            profile.getMiningData().recordOreFound(oreType, event.getBlock().getY());
            runChecks(player, profile, oreChecks);
        }

        runChecks(player, profile, worldChecks);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = profileManager.getProfile(player.getUniqueId());
        if (profile == null) return;

        profile.getPacketData().recordInteraction();
        runChecks(player, profile, exploitChecks);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        PlayerProfile profile = profileManager.getProfile(player.getUniqueId());
        if (profile == null) return;

        runChecks(player, profile, interactionChecks);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = profileManager.getProfile(player.getUniqueId());
        if (profile == null) return;

        profile.getPacketData().recordChat();
        // Chat flood check runs from exploit checks
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = profileManager.getProfile(player.getUniqueId());
        if (profile == null) return;

        profile.getPacketData().recordCommand();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = profileManager.getProfile(player.getUniqueId());
        if (profile == null) return;

        profile.getCombatData().recordClick();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        PlayerProfile profile = profileManager.getProfile(event.getPlayer().getUniqueId());
        if (profile != null) {
            profile.getMovementData().setSprinting(event.isSprinting());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        PlayerProfile profile = profileManager.getProfile(event.getPlayer().getUniqueId());
        if (profile != null) {
            profile.getMovementData().setSneaking(event.isSneaking());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        PlayerProfile profile = profileManager.getProfile(event.getPlayer().getUniqueId());
        if (profile != null) {
            profile.getMovementData().setFlying(event.isFlying());
        }
    }

    private boolean hasMovement(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
    }

    /**
     * Get the total number of registered checks.
     */
    public int getCheckCount() {
        return allChecks.size();
    }

    public List<Check> getAllChecks() { return allChecks; }
}
