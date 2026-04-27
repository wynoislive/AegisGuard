package com.aegisguard.checks.ore;

/**
 * Configurable ore generation rate model for custom worldgen (WynoWorldGen).
 * Stores expected ore rates per biome/dimension for anomaly detection.
 */
public final class OreRateModel {
    private double diamondRate;
    private double debrisRate;
    private double emeraldRate;
    private double goldRate;
    private double ironRate;
    private double lapisRate;
    private double redstoneRate;

    public OreRateModel() {
        // Vanilla defaults (ores per chunk)
        this.diamondRate = 3.7;
        this.debrisRate = 1.7;
        this.emeraldRate = 1.0;
        this.goldRate = 8.2;
        this.ironRate = 20.0;
        this.lapisRate = 3.4;
        this.redstoneRate = 8.0;
    }

    public double getDiamondRate() { return diamondRate; }
    public void setDiamondRate(double r) { this.diamondRate = r; }
    public double getDebrisRate() { return debrisRate; }
    public void setDebrisRate(double r) { this.debrisRate = r; }
    public double getEmeraldRate() { return emeraldRate; }
    public void setEmeraldRate(double r) { this.emeraldRate = r; }
    public double getGoldRate() { return goldRate; }
    public void setGoldRate(double r) { this.goldRate = r; }
    public double getIronRate() { return ironRate; }
    public void setIronRate(double r) { this.ironRate = r; }
    public double getLapisRate() { return lapisRate; }
    public void setLapisRate(double r) { this.lapisRate = r; }
    public double getRedstoneRate() { return redstoneRate; }
    public void setRedstoneRate(double r) { this.redstoneRate = r; }
}
