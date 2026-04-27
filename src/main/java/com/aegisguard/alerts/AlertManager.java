package com.aegisguard.alerts;

import com.aegisguard.alerts.discord.DiscordWebhookService;
import com.aegisguard.checks.Check;
import com.aegisguard.checks.CheckResult;
import com.aegisguard.config.ConfigManager;
import com.aegisguard.evidence.EvidenceManager;
import com.aegisguard.evidence.EvidenceRecord;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.scheduler.TaskScheduler;
import com.aegisguard.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Alert dispatch and cooldown management.
 * Sends alerts to staff, logs them, and dispatches webhooks.
 */
public final class AlertManager {

    private final Plugin plugin;
    private final Logger logger;
    private final ConfigManager config;
    private final DiscordWebhookService webhookService;
    private final EvidenceManager evidenceManager;
    private final TaskScheduler scheduler;
    private final File alertLogFile;
    private final File punishLogFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AlertManager(Plugin plugin, ConfigManager config, DiscordWebhookService webhookService,
                        EvidenceManager evidenceManager, TaskScheduler scheduler) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = config;
        this.webhookService = webhookService;
        this.evidenceManager = evidenceManager;
        this.scheduler = scheduler;

        // Initialize log files
        File logDir = new File(plugin.getDataFolder(), "logs");
        if (!logDir.exists()) logDir.mkdirs();
        this.alertLogFile = new File(logDir, "alerts.log");
        this.punishLogFile = new File(logDir, "punishments.log");
    }

    /**
     * Handle a violation from the check system.
     */
    public void handleViolation(Player player, PlayerProfile profile, Check check,
                                CheckResult result, double newVl) {
        String checkName = check.getName();
        String category = check.getCategory().name();
        String details = result.details();

        // Determine alert level
        AlertLevel level;
        if (newVl >= check.getThreshold() * 3) level = AlertLevel.CRITICAL;
        else if (newVl >= check.getThreshold() * 2) level = AlertLevel.HIGH;
        else if (newVl >= check.getThreshold()) level = AlertLevel.MEDIUM;
        else level = AlertLevel.LOW;

        // Build alert message
        Component alertComponent = Component.text()
                .append(ColorUtil.parse(config.getMessagesConfig().getPrefix()))
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" flagged ", NamedTextColor.GRAY))
                .append(Component.text(checkName, NamedTextColor.RED))
                .append(Component.text(" x" + String.format("%.0f", newVl), NamedTextColor.GRAY))
                .append(Component.text(" (" + category + ")", NamedTextColor.DARK_GRAY))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Details: " + details + "\nPlatform: " + profile.getPlatform()
                                + "\nPing: " + profile.getPing() + "ms\nTrust: " + String.format("%.0f", profile.getTrustScore().getScore()), NamedTextColor.GRAY)
                ))
                .build();

        // Send to staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("aegis.alerts")) {
                PlayerProfile staffProfile = null; // Could lookup but not needed for alerts
                staff.sendMessage(alertComponent);
            }
        }

        // Log to file
        if (config.getMainConfig().getBoolean("logging.log-alerts", true)) {
            logToFile(alertLogFile, String.format("[%s] %s flagged %s x%.0f (%s) - %s",
                    dateFormat.format(new Date()), player.getName(), checkName, newVl, category, details));
        }

        // Collect evidence if above threshold
        if (newVl >= check.getThreshold()) {
            EvidenceRecord evidence = evidenceManager.collectEvidence(player, profile, checkName, details);

            // Send webhook
            scheduler.runAsync(() -> webhookService.sendAlert(
                    player.getName(), player.getUniqueId().toString(),
                    profile.getPlatform().name(),
                    player.getWorld().getName(),
                    String.format("%.1f, %.1f, %.1f", player.getLocation().getX(),
                            player.getLocation().getY(), player.getLocation().getZ()),
                    profile.getPing(), profile.getServerTps(),
                    checkName, category, newVl,
                    profile.getTrustScore().getScore(),
                    details, level
            ));
        }

        // Log to database
        scheduler.runAsync(() -> {
            var db = com.aegisguard.core.AegisGuard.get().getDatabaseManager();
            db.insertViolation(player.getUniqueId(), checkName, category, newVl,
                    result.severity().name(), details, player.getWorld().getName(),
                    player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                    player.getLocation().getYaw(), player.getLocation().getPitch(),
                    profile.getPing(), profile.getServerTps());
            db.insertAlert(player.getUniqueId(), checkName, category, newVl,
                    player.getName() + " flagged " + checkName);
        });
    }

    /**
     * Log a punishment to file and webhook.
     */
    public void logPunishment(String playerName, String action, String reason, String staffName) {
        if (config.getMainConfig().getBoolean("logging.log-punishments", true)) {
            logToFile(punishLogFile, String.format("[%s] %s punished %s: %s (%s)",
                    dateFormat.format(new Date()), staffName, playerName, action, reason));
        }
    }

    private void logToFile(File file, String message) {
        scheduler.runAsync(() -> {
            try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
                out.println(message);
            } catch (IOException e) {
                logger.warning("Failed to write log: " + e.getMessage());
            }
        });
    }
}
