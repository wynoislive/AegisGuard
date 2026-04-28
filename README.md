# AegisGuard ✨

An enterprise-grade, high-performance AntiCheat plugin for Minecraft Paper 1.21.1. Developed from the ground up for massive concurrency, stable TPS, and deep intelligence, AegisGuard offers comprehensive protection against world exploitation, combat advantages, movement modifications, and an array of packet-level attacks.

**Developer:** wyno
**Target:** Paper 1.21.1 (Java 21)

---

## 🔥 Features
- **Dual-Database Support:** Out-of-the-box zero-setup SQLite, with enterprise MySQL + HikariCP connection pooling available for heavily populated networks.
- **Bedrock / Floodgate Awareness:** Automatically calibrates strictness and precision thresholds when dealing with Bedrock players, controllers, and mobile touch inputs.
- **Thread-safe Player Profiling:** Each player is given an asynchronous `PlayerProfile` to gracefully decouple check evaluations from memory congestion. Allows for rapid cache invalidation and rollback. 
- **Modular Checking Engine:** Employs a sophisticated annotation-driven architecture (`@CheckInfo`). Evaluates actions efficiently utilizing `Caffeine` caches to manage real-time rolling-windows of block states, combat interactions, and packet traffic.
- **Deep Ore Tracking (Bait & Vein Analyze):** Built specifically for custom WorldGen (e.g., WynoWorldGen). Tracks direct-path deviations, vein flood-fills, and places invisible bait ores for automated honeypots. 
- **Physics-based Movement Simulation:** Models vanilla movement algorithms — calculating friction scalars, liquid viscosity offsets, attribute modifiers, potion effects, and jump physics to precisely discern human limits from hacked clients.
- **Discord Webhook Alert System:** Provides rate-limited, asynchronous payload dispatch for suspicious behavior, staff punishments, errors, and system lifecycles.

## 🛠️ Included Checks
AegisGuard spans **34 individual enterprise-grade checks** encompassing multiple dimensions of play.

- **Combat:** _Reach, KillAura, AutoClicker, Velocity, Critical, AimAssist_
- **Movement:** _Fly, Speed, NoFall, Phase, Jesus, Step, Blink, InventoryWalk_
- **World:** _Scaffold, Nuker, AutoMine_
- **Ore (Xray):** _Xray (Dynamic thresholds), OreTracker (Hidden/Exposed validation)_
- **Interaction:** _ChestStealer, AutoArmor, AutoTotem, AutoEat_
- **Packet & Exploits:** _InvalidPacket, PacketOrder, PacketFlood, CrashExploit, BookExploit, SignExploit, ChatFlood_
- **Economy:** _EconomyAbuse (Trade mapping and inflation protection)_
- **Freecam:** _Freecam, BaseHunter, StorageESP_

## 💻 Commands
- `/ac help` - Show command help menu.
- `/ac gui` - Open the AegisGuard centralized Staff Dashboard (Chest GUI).
- `/ac profile <player>` - View ping, TPS, trust score, and violations for a specific player.
- `/ac trust <player>` - View a player's trust ranking and risk level.
- `/ac alerts` - Toggle real-time staff alerts for yourself.
- `/ac freeze/unfreeze <player>` - Immobilize a suspected cheater.
- `/ac punish <player>` - Automatically apply configured enforcement.
- `/ac exempt <player> <time>` - Force bypass checks temporarily for a player.
- `/ac evidence <player>` - Fetch raw data logs associated with flagged interactions.
- `/ac webhook <test|status|flush|reload>` - Manage webhook queues.

*(Permission `aegis.staff` required for most staff-level interactions, `aegis.gui` for dashboard).*

## 🔌 Configuration & Datasets
The configuration is broken down into structured systems, located in `plugins/AegisGuard/`:
- `config.yml` - Toggles TPS and ping compensations, platform multipliers, and system performance thread allocation.
- `checks.yml` - Tune individual cooldowns, VL decay points, and alert thresholds per-check.
- `database.yml` - Configure SQLite parameters or MySQL / MariaDB connection tuning.
- `discord.yml` - Bind and style integrated channels, specify retry-delay for exponential back-off limiters.
- `gui.yml` - Alter chest panel sizes, titles, layout indices, and iconography.
- `messages.yml` - Modify chat response texts (Adventure API MiniMessage syntax supported via `<color>`).
- `punishments.yml` - Create scaled violation escalations extending into temporary mutes, kicks, IP/Hardware bans. 

## 🚀 Building from source

We use `Gradle` to compile and shade our dependencies. Java 21 or higher (JDK 23 recommended for this environment) must be utilized.
```bash
./gradlew clean shadowJar
```
This produces an artifact mapping to `build/libs/AegisGuard-1.1.0.jar`. Drop it directly into your `plugins` directory. Note that AegisGuard gracefully identifies the presence of `ProtocolLib` and `Floodgate` at runtime, hooking into them automatically if they are populated.
