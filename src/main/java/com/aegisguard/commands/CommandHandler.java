package com.aegisguard.commands;

import com.aegisguard.core.AegisGuard;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.util.ColorUtil;
import com.aegisguard.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Root command handler for /ac (/aegis, /anticheat, /aegisguard).
 * Dispatches to subcommands.
 */
public final class CommandHandler implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private final AegisGuard core;
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "help", "gui", "alerts", "logs", "profile", "info", "trust",
            "freeze", "unfreeze", "exempt", "punish", "verbose", "debug", "reload",
            "webhook", "evidence"
    );
    private static final List<String> WEBHOOK_SUBS = Arrays.asList("test", "status", "flush", "reload");

    public CommandHandler(Plugin plugin, AegisGuard core) {
        this.plugin = plugin;
        this.core = core;
    }

    public void register() {
        var cmd = plugin.getServer().getPluginCommand("ac");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        return switch (sub) {
            case "help" -> { sendHelp(sender); yield true; }
            case "gui" -> handleGui(sender);
            case "alerts" -> handleAlerts(sender);
            case "logs", "evidence" -> handleLogs(sender, args);
            case "profile", "info" -> handleProfile(sender, args);
            case "trust" -> handleTrust(sender, args);
            case "freeze" -> handleFreeze(sender, args);
            case "unfreeze" -> handleUnfreeze(sender, args);
            case "exempt" -> handleExempt(sender, args);
            case "punish" -> handlePunish(sender, args);
            case "verbose" -> handleVerbose(sender, args);
            case "debug" -> handleDebug(sender);
            case "reload" -> handleReload(sender);
            case "webhook" -> handleWebhook(sender, args);
            default -> { sendHelp(sender); yield true; }
        };
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.parse("<dark_gray>═══════════════════════════════════════</dark_gray>"));
        sender.sendMessage(ColorUtil.parse("<aqua><bold>AegisGuard</bold></aqua> <gray>- Enterprise AntiCheat</gray>"));
        sender.sendMessage(ColorUtil.parse("<dark_gray>═══════════════════════════════════════</dark_gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac gui</yellow> <gray>- Open dashboard</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac alerts</yellow> <gray>- Toggle alerts</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac profile <player></yellow> <gray>- View player</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac trust <player></yellow> <gray>- View trust score</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac freeze <player></yellow> <gray>- Freeze player</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac unfreeze <player></yellow> <gray>- Unfreeze player</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac exempt <player> <time></yellow> <gray>- Exempt player</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac verbose <player></yellow> <gray>- Verbose mode</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac evidence <player></yellow> <gray>- View evidence</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac logs <player></yellow> <gray>- View violations</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac webhook <sub></yellow> <gray>- Webhook commands</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac debug</yellow> <gray>- Toggle debug mode</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/ac reload</yellow> <gray>- Reload config</gray>"));
    }

    private boolean handleGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.error("Only players can use the GUI."));
            return true;
        }
        if (!player.hasPermission("aegis.gui")) {
            sender.sendMessage(core.getConfigManager().getMessagesConfig().get("commands.no-permission"));
            return true;
        }
        core.getGuiManager().openMainPanel(player);
        return true;
    }

    private boolean handleAlerts(CommandSender sender) {
        if (!(sender instanceof Player player)) return true;
        PlayerProfile profile = core.getProfileManager().getProfile(player.getUniqueId());
        if (profile != null) {
            profile.setAlertsEnabled(!profile.isAlertsEnabled());
            sender.sendMessage(ColorUtil.parse(profile.isAlertsEnabled()
                    ? "<green>Alerts enabled.</green>" : "<yellow>Alerts disabled.</yellow>"));
        }
        return true;
    }

    private boolean handleProfile(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(ColorUtil.error("Usage: /ac profile <player>")); return true; }
        PlayerProfile profile = core.getProfileManager().getProfile(args[1]);
        if (profile == null) { sender.sendMessage(ColorUtil.error("Player not found: " + args[1])); return true; }
        sender.sendMessage(ColorUtil.parse("<dark_gray>═══════════════════════════════════════</dark_gray>"));
        sender.sendMessage(ColorUtil.parse("<aqua><bold>Profile: " + profile.getUsername() + "</bold></aqua>"));
        sender.sendMessage(ColorUtil.parse("<gray>Platform:</gray> <white>" + profile.getPlatform() + "</white>"));
        sender.sendMessage(ColorUtil.parse("<gray>Trust:</gray> <white>" + String.format("%.0f", profile.getTrustScore().getScore()) + "/100 (" + profile.getTrustScore().getTrustLevel() + ")</white>"));
        sender.sendMessage(ColorUtil.parse("<gray>Ping:</gray> <white>" + profile.getPing() + "ms</white>"));
        sender.sendMessage(ColorUtil.parse("<gray>Playtime:</gray> <white>" + TimeUtil.formatPlaytime(profile.getTotalPlaytime()) + "</white>"));
        sender.sendMessage(ColorUtil.parse("<gray>Total VL:</gray> <white>" + String.format("%.0f", profile.getTotalVL()) + "</white>"));
        sender.sendMessage(ColorUtil.parse("<gray>Frozen:</gray> <white>" + profile.isFrozen() + "</white>"));
        sender.sendMessage(ColorUtil.parse("<gray>Evidence:</gray> <white>" + profile.getEvidenceRecords().size() + " records</white>"));
        sender.sendMessage(ColorUtil.parse("<dark_gray>═══════════════════════════════════════</dark_gray>"));
        return true;
    }

    private boolean handleTrust(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(ColorUtil.error("Usage: /ac trust <player>")); return true; }
        PlayerProfile profile = core.getProfileManager().getProfile(args[1]);
        if (profile == null) { sender.sendMessage(ColorUtil.error("Player not found.")); return true; }
        sender.sendMessage(ColorUtil.parse("<gray>Trust for <yellow>" + profile.getUsername() + "</yellow>: <white>"
                + String.format("%.0f", profile.getTrustScore().getScore()) + "/100</white> <gray>(" + profile.getTrustScore().getTrustLevel() + ")</gray>"));
        return true;
    }

    private boolean handleFreeze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aegis.freeze")) { sender.sendMessage(ColorUtil.error("No permission.")); return true; }
        if (args.length < 2) { sender.sendMessage(ColorUtil.error("Usage: /ac freeze <player>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage(ColorUtil.error("Player not online.")); return true; }
        PlayerProfile profile = core.getProfileManager().getProfile(target.getUniqueId());
        if (profile == null) return true;
        if (profile.isFrozen()) { sender.sendMessage(ColorUtil.warning(target.getName() + " is already frozen.")); return true; }
        profile.setFrozen(true);
        target.sendMessage(core.getConfigManager().getMessagesConfig().get("freeze.frozen"));
        sender.sendMessage(ColorUtil.success("Froze " + target.getName()));
        return true;
    }

    private boolean handleUnfreeze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aegis.freeze")) { sender.sendMessage(ColorUtil.error("No permission.")); return true; }
        if (args.length < 2) { sender.sendMessage(ColorUtil.error("Usage: /ac unfreeze <player>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage(ColorUtil.error("Player not online.")); return true; }
        PlayerProfile profile = core.getProfileManager().getProfile(target.getUniqueId());
        if (profile == null) return true;
        profile.setFrozen(false);
        sender.sendMessage(ColorUtil.success("Unfroze " + target.getName()));
        return true;
    }

    private boolean handleExempt(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aegis.exempt")) { sender.sendMessage(ColorUtil.error("No permission.")); return true; }
        if (args.length < 3) { sender.sendMessage(ColorUtil.error("Usage: /ac exempt <player> <time>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage(ColorUtil.error("Player not online.")); return true; }
        long seconds = TimeUtil.parseDuration(args[2]);
        if (seconds <= 0) { sender.sendMessage(ColorUtil.error("Invalid duration.")); return true; }
        PlayerProfile profile = core.getProfileManager().getProfile(target.getUniqueId());
        if (profile == null) return true;
        profile.setExemptUntil(System.currentTimeMillis() + seconds * 1000);
        sender.sendMessage(ColorUtil.success("Exempted " + target.getName() + " for " + TimeUtil.formatDuration(seconds)));
        return true;
    }

    private boolean handlePunish(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aegis.staff")) { sender.sendMessage(ColorUtil.error("No permission.")); return true; }
        if (args.length < 2) { sender.sendMessage(ColorUtil.error("Usage: /ac punish <player>")); return true; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage(ColorUtil.error("Player not online.")); return true; }
        target.kick(core.getConfigManager().getMessagesConfig().get("punishments.kick-message",
                "{reason}", "Staff action"));
        sender.sendMessage(ColorUtil.success("Punished " + target.getName()));
        return true;
    }

    private boolean handleVerbose(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(ColorUtil.error("Usage: /ac verbose <player>")); return true; }
        PlayerProfile profile = core.getProfileManager().getProfile(args[1]);
        if (profile == null) { sender.sendMessage(ColorUtil.error("Player not found.")); return true; }
        profile.setVerboseTarget(!profile.isVerboseTarget());
        sender.sendMessage(ColorUtil.parse(profile.isVerboseTarget()
                ? "<green>Verbose enabled for " + profile.getUsername() + "</green>"
                : "<yellow>Verbose disabled for " + profile.getUsername() + "</yellow>"));
        return true;
    }

    private boolean handleDebug(CommandSender sender) {
        if (!sender.hasPermission("aegis.debug")) { sender.sendMessage(ColorUtil.error("No permission.")); return true; }
        boolean debug = !core.getConfigManager().getMainConfig().getBoolean("general.debug", false);
        core.getConfigManager().getMainConfig().set("general.debug", debug);
        sender.sendMessage(debug ? ColorUtil.success("Debug mode enabled.") : ColorUtil.warning("Debug mode disabled."));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("aegis.reload")) { sender.sendMessage(ColorUtil.error("No permission.")); return true; }
        try {
            core.getConfigManager().reloadAll();
            sender.sendMessage(ColorUtil.success("Configuration reloaded."));
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.error("Reload failed: " + e.getMessage()));
        }
        return true;
    }

    private boolean handleLogs(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(ColorUtil.error("Usage: /ac logs <player>")); return true; }
        PlayerProfile profile = core.getProfileManager().getProfile(args[1]);
        if (profile == null) { sender.sendMessage(ColorUtil.error("Player not found.")); return true; }
        sender.sendMessage(ColorUtil.parse("<aqua>Evidence for " + profile.getUsername() + ":</aqua>"));
        var records = profile.getEvidenceRecords();
        if (records.isEmpty()) { sender.sendMessage(ColorUtil.info("No evidence found.")); return true; }
        for (var record : records.subList(Math.max(0, records.size() - 10), records.size())) {
            sender.sendMessage(ColorUtil.parse("<gray>[" + TimeUtil.toDisplay(record.timestamp()) + "] "
                    + record.triggeredChecks() + " @ " + record.world() + " "
                    + String.format("%.0f,%.0f,%.0f", record.x(), record.y(), record.z()) + "</gray>"));
        }
        return true;
    }

    private boolean handleWebhook(CommandSender sender, String[] args) {
        if (!sender.hasPermission("aegis.webhook")) { sender.sendMessage(ColorUtil.error("No permission.")); return true; }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.parse("<yellow>Webhook commands: test, status, flush, reload</yellow>"));
            return true;
        }
        return switch (args[1].toLowerCase()) {
            case "test" -> {
                boolean ok = core.getWebhookService().testWebhook("alerts");
                sender.sendMessage(ok ? ColorUtil.success("Test webhook sent.") : ColorUtil.error("Test failed."));
                yield true;
            }
            case "status" -> {
                sender.sendMessage(ColorUtil.parse("<aqua>Webhook Status:</aqua>"));
                sender.sendMessage(ColorUtil.parse("<gray>Queue size: <white>" + core.getWebhookService().getQueueSize() + "</white></gray>"));
                sender.sendMessage(ColorUtil.parse("<gray>Rate limited: <white>" + core.getWebhookService().isRateLimited() + "</white></gray>"));
                yield true;
            }
            case "flush" -> {
                core.getWebhookService().flush();
                sender.sendMessage(ColorUtil.success("Queue flushed."));
                yield true;
            }
            case "reload" -> {
                core.getConfigManager().reloadAll();
                sender.sendMessage(ColorUtil.success("Webhook config reloaded."));
                yield true;
            }
            default -> { sender.sendMessage(ColorUtil.error("Unknown webhook subcommand.")); yield true; }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("webhook")) {
                return WEBHOOK_SUBS.stream().filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
            // Return online player names for commands that need a player argument
            if (List.of("profile", "info", "trust", "freeze", "unfreeze", "exempt", "punish", "verbose", "logs", "evidence").contains(sub)) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
