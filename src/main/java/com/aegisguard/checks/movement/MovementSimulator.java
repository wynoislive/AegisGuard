package com.aegisguard.checks.movement;

import com.aegisguard.checks.*;
import com.aegisguard.playerdata.MovementData;
import com.aegisguard.playerdata.PlayerProfile;
import com.aegisguard.util.LocationUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * Simulates legal movement physics to determine if a player's
 * position is achievable through vanilla mechanics.
 * Accounts for TPS, ping, environment, and game mechanics.
 */
public final class MovementSimulator {

    private static final double GRAVITY = 0.08;
    private static final double DRAG = 0.98;
    private static final double BASE_WALK_SPEED = 0.2873;
    private static final double SPRINT_MULTIPLIER = 1.3;
    private static final double SNEAK_MULTIPLIER = 0.3;
    private static final double SWIM_MULTIPLIER = 0.5;
    private static final double ICE_MULTIPLIER = 1.6;
    private static final double SOUL_SAND_MULTIPLIER = 0.4;
    private static final double JUMP_VELOCITY = 0.42;
    private static final double WATER_DRAG = 0.8;
    private static final double LAVA_DRAG = 0.5;

    /**
     * Calculate the maximum allowed horizontal speed for a player.
     */
    public static double getMaxHorizontalSpeed(Player player, PlayerProfile profile) {
        double base = BASE_WALK_SPEED;
        MovementData data = profile.getMovementData();

        // Flight modes
        if (player.isFlying() || player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR) {
            return player.getFlySpeed() * 20.0 + 0.5;
        }

        // Sprint modifier
        if (data.isSprinting()) {
            base *= SPRINT_MULTIPLIER;
        }

        // Sneak modifier
        if (data.isSneaking()) {
            base *= SNEAK_MULTIPLIER;
        }

        // Swimming
        if (data.isSwimming()) {
            base *= SWIM_MULTIPLIER;
        }

        // Speed potion
        var speed = player.getPotionEffect(PotionEffectType.SPEED);
        if (speed != null) {
            base *= 1.0 + (speed.getAmplifier() + 1) * 0.2;
        }

        // Slowness potion
        var slow = player.getPotionEffect(PotionEffectType.SLOWNESS);
        if (slow != null) {
            base *= 1.0 - (slow.getAmplifier() + 1) * 0.15;
        }

        // Ice
        Location loc = player.getLocation();
        if (LocationUtil.isOnIce(loc)) {
            base *= ICE_MULTIPLIER;
        }

        // Honey
        if (LocationUtil.isOnHoney(loc)) {
            base *= 0.4;
        }

        // Water drag
        if (LocationUtil.isInWater(loc)) {
            base *= WATER_DRAG;
        }

        // Lava drag
        if (LocationUtil.isInLava(loc)) {
            base *= LAVA_DRAG;
        }

        // Elytra gliding
        if (data.isGliding()) {
            base = 3.0; // elytra can reach very high speeds
        }

        // Vehicle
        if (data.isInVehicle()) {
            base = 1.5; // vehicles have their own speed
        }

        // TPS compensation
        double tps = profile.getServerTps();
        if (tps < 20.0 && tps > 0) {
            base *= (20.0 / tps);
        }

        // Ping compensation
        int ping = profile.getPing();
        if (ping > 100) {
            base += (ping - 100) * 0.001;
        }

        // Knockback allowance
        if (profile.getCombatData().hasReceivedKnockback()) {
            long kbAge = System.currentTimeMillis() - profile.getCombatData().getLastKnockbackTime();
            if (kbAge < 500) {
                base += 1.0;
            }
        }

        return base;
    }

    /**
     * Calculate the maximum allowed vertical velocity for a player.
     */
    public static double getMaxVerticalVelocity(Player player, PlayerProfile profile) {
        double maxUp = JUMP_VELOCITY;

        // Jump boost
        var jumpBoost = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (jumpBoost != null) {
            maxUp += (jumpBoost.getAmplifier() + 1) * 0.1;
        }

        // Slime block bounce
        if (LocationUtil.isOnSlime(player.getLocation())) {
            maxUp = 1.5;
        }

        // Bubble column
        if (LocationUtil.isInBubbleColumn(player.getLocation())) {
            maxUp = 1.8;
        }

        // Climbing
        if (LocationUtil.isOnClimbable(player.getLocation())) {
            maxUp = 0.2;
        }

        // Wind charge / riptide
        if (profile.getMovementData().isGliding()) {
            maxUp = 5.0;
        }

        // TPS compensation
        double tps = profile.getServerTps();
        if (tps < 20.0 && tps > 0) {
            maxUp *= (20.0 / tps);
        }

        return maxUp;
    }

    /**
     * Check if a movement is physically possible.
     */
    public static boolean isLegalMovement(Player player, PlayerProfile profile,
                                          double horizontalSpeed, double verticalSpeed) {
        double maxH = getMaxHorizontalSpeed(player, profile);
        double maxV = getMaxVerticalVelocity(player, profile);

        // Allow 15% tolerance for edge cases
        return horizontalSpeed <= maxH * 1.15 && verticalSpeed <= maxV * 1.15;
    }
}
