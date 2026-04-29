package com.aegisguard.core;

import com.aegisguard.alerts.AlertManager;
import com.aegisguard.alerts.discord.DiscordWebhookService;
import com.aegisguard.antixray.AntiXrayManager;
import com.aegisguard.checks.CheckManager;
import com.aegisguard.commands.CommandHandler;
import com.aegisguard.compat.CompatManager;
import com.aegisguard.config.ConfigManager;
import com.aegisguard.evidence.EvidenceManager;
import com.aegisguard.gui.GuiManager;
import com.aegisguard.metrics.MetricsManager;
import com.aegisguard.playerdata.PlayerProfileManager;
import com.aegisguard.scheduler.TaskScheduler;
import com.aegisguard.storage.DatabaseManager;
import com.aegisguard.world.WorldManager;
import com.aegisguard.antixray.PaperSyncService;
import com.aegisguard.checks.ore.OreAnalysisListener;
import com.aegisguard.checks.ore.OreThresholdCheck;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Core singleton managing all AegisGuard subsystems.
 * Handles orderly initialization and shutdown of all services.
 */
public final class AegisGuard {

    private static AegisGuard instance;

    private final Plugin plugin;
    private final Logger logger;
    private final ServiceRegistry registry;

    private ConfigManager configManager;
    private TaskScheduler scheduler;
    private DatabaseManager databaseManager;
    private PlayerProfileManager profileManager;
    private CheckManager checkManager;
    private AlertManager alertManager;
    private DiscordWebhookService webhookService;
    private EvidenceManager evidenceManager;
    private CommandHandler commandHandler;
    private GuiManager guiManager;
    private CompatManager compatManager;
    private MetricsManager metricsManager;
    private WorldManager worldManager;
    private PaperSyncService paperSyncService;

    private volatile boolean enabled = false;
    private long startTime;

    private AegisGuard(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.registry = new ServiceRegistry();
    }

    /**
     * Initialize the AegisGuard core instance.
     */
    public static AegisGuard init(Plugin plugin) {
        if (instance != null) {
            throw new IllegalStateException("AegisGuard is already initialized!");
        }
        instance = new AegisGuard(plugin);
        return instance;
    }

    /**
     * Get the singleton instance.
     */
    public static AegisGuard get() {
        if (instance == null) {
            throw new IllegalStateException("AegisGuard is not initialized!");
        }
        return instance;
    }

    /**
     * Start all subsystems in the correct order.
     */
    public void enable() {
        startTime = System.currentTimeMillis();
        logger.info("═══════════════════════════════════════");
        logger.info("  AegisGuard - Enterprise AntiCheat");
        logger.info("  Author: wyno");
        logger.info("  Version: " + plugin.getPluginMeta().getVersion());
        logger.info("═══════════════════════════════════════");

        try {
            // Phase 1: Configuration
            logger.info("[1/11] Loading configuration...");
            configManager = new ConfigManager(plugin);
            configManager.loadAll();
            registry.register(ConfigManager.class, configManager);

            worldManager = new WorldManager(plugin);
            worldManager.load();
            registry.register(WorldManager.class, worldManager);

            if (!configManager.isEnabled()) {
                logger.warning("AegisGuard is disabled in config.yml!");
                return;
            }

            // Phase 2: Scheduler
            logger.info("[2/11] Initializing scheduler...");
            scheduler = new TaskScheduler(plugin, configManager.getAsyncThreads());
            registry.register(TaskScheduler.class, scheduler);

            // Phase 3: Database
            logger.info("[3/11] Connecting to database...");
            databaseManager = new DatabaseManager(plugin, configManager.getDatabaseConfig());
            databaseManager.initialize();
            registry.register(DatabaseManager.class, databaseManager);

            // Phase 4: Compatibility
            logger.info("[4/11] Loading compatibility modules...");
            compatManager = new CompatManager(plugin, configManager);
            compatManager.initialize();
            registry.register(CompatManager.class, compatManager);

            // Phase 5: Player Data
            logger.info("[5/11] Initializing player profile engine...");
            profileManager = new PlayerProfileManager(plugin, databaseManager, configManager, compatManager);
            registry.register(PlayerProfileManager.class, profileManager);

            // Phase 6: Evidence
            logger.info("[6/11] Initializing evidence system...");
            evidenceManager = new EvidenceManager(databaseManager, scheduler);
            registry.register(EvidenceManager.class, evidenceManager);

            // Phase 7: Alerts & Webhooks
            logger.info("[7/11] Initializing alert and webhook systems...");
            webhookService = new DiscordWebhookService(configManager.getDiscordConfig(), scheduler, plugin.getLogger());
            webhookService.initialize();
            registry.register(DiscordWebhookService.class, webhookService);

            alertManager = new AlertManager(plugin, configManager, webhookService, evidenceManager, scheduler);
            registry.register(AlertManager.class, alertManager);

            // Phase 8: Checks
            logger.info("[8/11] Registering check systems...");
            checkManager = new CheckManager(plugin, configManager, profileManager, alertManager, compatManager);
            checkManager.registerAll();
            registry.register(CheckManager.class, checkManager);

            // Phase 9: GUI
            logger.info("[9/11] Initializing GUI system...");
            guiManager = new GuiManager(plugin, configManager, profileManager);
            registry.register(GuiManager.class, guiManager);

            // Phase 10: Commands & Metrics
            logger.info("[10/11] Registering commands and metrics...");
            commandHandler = new CommandHandler(plugin, this);
            commandHandler.register();
            registry.register(CommandHandler.class, commandHandler);

            metricsManager = new MetricsManager(plugin, this);
            metricsManager.start();
            registry.register(MetricsManager.class, metricsManager);

            // Phase 11: Prevention Engines
            logger.info("[11/11] Starting prevention engines...");
            AntiXrayManager antiXrayManager = new AntiXrayManager(
                    plugin, 
                    worldManager,
                    configManager.isAntiXrayEnabled(), 
                    configManager.getAntiXrayMode()
            );
            registry.register(AntiXrayManager.class, antiXrayManager);

            // Phase 12: OreHider Integration
            logger.info("[12/12] Initializing OreHider integration...");
            paperSyncService = new PaperSyncService(plugin, configManager);
            paperSyncService.syncAll();
            registry.register(PaperSyncService.class, paperSyncService);

            Bukkit.getPluginManager().registerEvents(new OreAnalysisListener(plugin), plugin);
            Bukkit.getPluginManager().registerEvents(new OreThresholdCheck(plugin), plugin);

            // Discord Startup Message (OreHider parity)
            if (configManager.getChecksConfig().getConfig().getBoolean("ore-hider.discord.enabled", true)) {
                String startupMsg = configManager.getChecksConfig().getConfig().getString("ore-hider.discord.messages.startup", 
                        "🟢 **OreHider** is now active. Anti-Xray protocols engaged.");
                webhookService.sendDirect(startupMsg);
            }

            // Start scheduled tasks
            startScheduledTasks();

            enabled = true;
            long elapsed = System.currentTimeMillis() - startTime;
            logger.info("═══════════════════════════════════════");
            logger.info("  AegisGuard enabled successfully!");
            logger.info("  Checks loaded: " + checkManager.getCheckCount());
            logger.info("  Database: " + configManager.getDatabaseConfig().getType());
            logger.info("  Platform: Paper " + Bukkit.getMinecraftVersion());
            logger.info("  Startup time: " + elapsed + "ms");
            logger.info("═══════════════════════════════════════");

            // Send lifecycle webhook
            webhookService.sendLifecycleStartup(
                    plugin.getPluginMeta().getVersion(),
                    configManager.getDatabaseConfig().getType(),
                    checkManager.getCheckCount(),
                    Bukkit.getMinecraftVersion(),
                    elapsed
            );

        } catch (Exception e) {
            logger.severe("Failed to enable AegisGuard: " + e.getMessage());
            e.printStackTrace();
            disable();
        }
    }

    /**
     * Shut down all subsystems in reverse order.
     */
    public void disable() {
        logger.info("Shutting down AegisGuard...");

        if (webhookService != null) {
            // Discord Shutdown Message (OreHider parity)
            if (configManager != null && configManager.getChecksConfig().getConfig().getBoolean("ore-hider.discord.enabled", true)) {
                String shutdownMsg = configManager.getChecksConfig().getConfig().getString("ore-hider.discord.messages.shutdown", 
                        "🔴 **OreHider** has been disabled.");
                webhookService.sendDirect(shutdownMsg);
            }
            webhookService.sendLifecycleShutdown();
            webhookService.shutdown();
        }

        if (metricsManager != null) metricsManager.stop();
        if (guiManager != null) guiManager.closeAll();
        if (checkManager != null) checkManager.unregisterAll();
        if (profileManager != null) profileManager.saveAll();
        if (webhookService != null) webhookService.shutdown();
        if (databaseManager != null) databaseManager.shutdown();
        if (scheduler != null) scheduler.shutdown();

        registry.clear();
        enabled = false;
        instance = null;

        logger.info("AegisGuard disabled.");
    }

    /**
     * Start periodic scheduled tasks.
     */
    private void startScheduledTasks() {
        int interval = configManager.getTaskInterval();

        // VL decay task
        scheduler.runSyncRepeating(() -> {
            if (profileManager != null) {
                profileManager.decayAllViolations(configManager.getDecayInterval());
            }
        }, interval * 20L, interval * 20L);

        // Metrics collection
        scheduler.runAsyncRepeating(() -> {
            if (metricsManager != null) {
                metricsManager.collect();
            }
        }, 100L, 1200L);

        // Database batch flush
        int batchInterval = configManager.getDatabaseConfig().getBatchFlushInterval();
        scheduler.runAsyncRepeating(() -> {
            if (databaseManager != null) {
                databaseManager.flushBatch();
            }
        }, batchInterval * 20L, batchInterval * 20L);
    }

    // --- Accessors ---

    public Plugin getPlugin() { return plugin; }
    public Logger getLogger() { return logger; }
    public ServiceRegistry getRegistry() { return registry; }
    public ConfigManager getConfigManager() { return configManager; }
    public TaskScheduler getScheduler() { return scheduler; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public PlayerProfileManager getProfileManager() { return profileManager; }
    public CheckManager getCheckManager() { return checkManager; }
    public AlertManager getAlertManager() { return alertManager; }
    public DiscordWebhookService getWebhookService() { return webhookService; }
    public EvidenceManager getEvidenceManager() { return evidenceManager; }
    public CommandHandler getCommandHandler() { return commandHandler; }
    public GuiManager getGuiManager() { return guiManager; }
    public CompatManager getCompatManager() { return compatManager; }
    public MetricsManager getMetricsManager() { return metricsManager; }
    public WorldManager getWorldManager() { return worldManager; }
    public PaperSyncService getPaperSyncService() { return paperSyncService; }
    public boolean isEnabled() { return enabled; }
    public long getStartTime() { return startTime; }
}
