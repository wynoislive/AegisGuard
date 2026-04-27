package com.aegisguard.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Centralized task scheduling wrapper for async and sync task management.
 */
public final class TaskScheduler {

    private final Plugin plugin;
    private final ExecutorService asyncPool;

    public TaskScheduler(Plugin plugin, int asyncThreads) {
        this.plugin = plugin;
        this.asyncPool = Executors.newFixedThreadPool(asyncThreads, r -> {
            Thread t = new Thread(r, "AegisGuard-Async");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Run a task on the main server thread.
     */
    public void runSync(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Run a task on the main thread after a delay.
     */
    public void runSyncLater(Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    /**
     * Run a repeating task on the main thread.
     */
    public int runSyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks).getTaskId();
    }

    /**
     * Run a task asynchronously using the plugin's thread pool.
     */
    public void runAsync(Runnable task) {
        asyncPool.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                plugin.getLogger().severe("Async task error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Run a task asynchronously using Bukkit's scheduler.
     */
    public void runBukkitAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * Run a repeating async task using Bukkit's scheduler.
     */
    public int runAsyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks).getTaskId();
    }

    /**
     * Shutdown the async thread pool gracefully.
     */
    public void shutdown() {
        asyncPool.shutdown();
        try {
            if (!asyncPool.awaitTermination(10, TimeUnit.SECONDS)) {
                asyncPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
