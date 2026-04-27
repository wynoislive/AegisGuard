package com.aegisguard.playerdata;

import org.bukkit.Location;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Movement tracking data for a player session.
 */
public final class MovementData {

    private static final int MAX_HISTORY = 40;

    private Location lastLocation;
    private Location safeLocation;
    private long lastMoveTime;
    private double lastHorizontalSpeed;
    private double lastVerticalSpeed;
    private double maxHorizontalSpeed;
    private int airTicks;
    private int groundTicks;
    private int waterTicks;
    private int lavaTicks;
    private int climbTicks;
    private boolean onGround;
    private boolean lastOnGround;
    private boolean inVehicle;
    private boolean gliding;
    private boolean swimming;
    private boolean sprinting;
    private boolean sneaking;
    private boolean flying;
    private final Deque<Location> locationHistory = new ArrayDeque<>();
    private final Deque<Double> speedHistory = new ArrayDeque<>();

    /**
     * Record a new position update.
     */
    public void update(Location to, boolean onGround) {
        this.lastOnGround = this.onGround;
        this.onGround = onGround;
        this.lastMoveTime = System.currentTimeMillis();

        if (lastLocation != null && to.getWorld() == lastLocation.getWorld()) {
            double dx = to.getX() - lastLocation.getX();
            double dz = to.getZ() - lastLocation.getZ();
            double dy = to.getY() - lastLocation.getY();
            this.lastHorizontalSpeed = Math.sqrt(dx * dx + dz * dz);
            this.lastVerticalSpeed = dy;

            if (lastHorizontalSpeed > maxHorizontalSpeed) {
                maxHorizontalSpeed = lastHorizontalSpeed;
            }

            // Speed history
            speedHistory.addLast(lastHorizontalSpeed);
            if (speedHistory.size() > MAX_HISTORY) speedHistory.pollFirst();
        }

        // Air/ground tick tracking
        if (onGround) {
            groundTicks++;
            airTicks = 0;
        } else {
            airTicks++;
            groundTicks = 0;
        }

        // Location history
        locationHistory.addLast(to.clone());
        if (locationHistory.size() > MAX_HISTORY) locationHistory.pollFirst();

        // Safe location (last known ground position)
        if (onGround && to.getBlock().getType().isSolid() || isOnGround(to)) {
            this.safeLocation = to.clone();
        }

        this.lastLocation = to.clone();
    }

    private boolean isOnGround(Location loc) {
        return loc.clone().subtract(0, 0.1, 0).getBlock().getType().isSolid();
    }

    /**
     * Reset all movement tracking data.
     */
    public void reset() {
        lastLocation = null;
        safeLocation = null;
        lastHorizontalSpeed = 0;
        lastVerticalSpeed = 0;
        maxHorizontalSpeed = 0;
        airTicks = 0;
        groundTicks = 0;
        waterTicks = 0;
        lavaTicks = 0;
        climbTicks = 0;
        onGround = true;
        lastOnGround = true;
        inVehicle = false;
        gliding = false;
        swimming = false;
        sprinting = false;
        sneaking = false;
        flying = false;
        locationHistory.clear();
        speedHistory.clear();
    }

    // --- Getters and Setters ---

    public Location getLastLocation() { return lastLocation; }
    public Location getSafeLocation() { return safeLocation; }
    public void setSafeLocation(Location loc) { this.safeLocation = loc; }
    public long getLastMoveTime() { return lastMoveTime; }
    public double getLastHorizontalSpeed() { return lastHorizontalSpeed; }
    public double getLastVerticalSpeed() { return lastVerticalSpeed; }
    public double getMaxHorizontalSpeed() { return maxHorizontalSpeed; }
    public int getAirTicks() { return airTicks; }
    public int getGroundTicks() { return groundTicks; }
    public int getWaterTicks() { return waterTicks; }
    public void setWaterTicks(int t) { this.waterTicks = t; }
    public int getLavaTicks() { return lavaTicks; }
    public void setLavaTicks(int t) { this.lavaTicks = t; }
    public int getClimbTicks() { return climbTicks; }
    public void setClimbTicks(int t) { this.climbTicks = t; }
    public boolean isOnGround() { return onGround; }
    public boolean wasOnGround() { return lastOnGround; }
    public boolean isInVehicle() { return inVehicle; }
    public void setInVehicle(boolean v) { this.inVehicle = v; }
    public boolean isGliding() { return gliding; }
    public void setGliding(boolean g) { this.gliding = g; }
    public boolean isSwimming() { return swimming; }
    public void setSwimming(boolean s) { this.swimming = s; }
    public boolean isSprinting() { return sprinting; }
    public void setSprinting(boolean s) { this.sprinting = s; }
    public boolean isSneaking() { return sneaking; }
    public void setSneaking(boolean s) { this.sneaking = s; }
    public boolean isFlying() { return flying; }
    public void setFlying(boolean f) { this.flying = f; }
    public Deque<Location> getLocationHistory() { return locationHistory; }
    public Deque<Double> getSpeedHistory() { return speedHistory; }
}
