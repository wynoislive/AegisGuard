package com.aegisguard.util;

/**
 * Mathematical utility methods for anticheat calculations.
 */
public final class MathUtil {

    private MathUtil() {}

    /**
     * Clamp value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Calculate the horizontal distance between two points.
     */
    public static double horizontalDistance(double x1, double z1, double x2, double z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Calculate 3D distance between two points.
     */
    public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculate the horizontal distance squared.
     */
    public static double horizontalDistanceSq(double x1, double z1, double x2, double z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        return dx * dx + dz * dz;
    }

    /**
     * Calculate the angle difference normalized to [-180, 180].
     */
    public static float angleDifference(float a, float b) {
        float diff = ((a - b) % 360.0f + 540.0f) % 360.0f - 180.0f;
        return diff;
    }

    /**
     * Calculate standard deviation from an array of values.
     */
    public static double standardDeviation(double[] values) {
        if (values.length == 0) return 0.0;
        double mean = 0;
        for (double v : values) mean += v;
        mean /= values.length;
        double variance = 0;
        for (double v : values) {
            double diff = v - mean;
            variance += diff * diff;
        }
        return Math.sqrt(variance / values.length);
    }

    /**
     * Calculate mean from an array.
     */
    public static double mean(double[] values) {
        if (values.length == 0) return 0.0;
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    /**
     * Calculate the GCD of two values with epsilon tolerance.
     */
    public static double gcd(double a, double b) {
        if (Math.abs(b) < 1E-6) return a;
        return gcd(b, a % b);
    }

    /**
     * Check if a value is within a given tolerance of a target.
     */
    public static boolean isNear(double value, double target, double tolerance) {
        return Math.abs(value - target) <= tolerance;
    }

    /**
     * Linear interpolation.
     */
    public static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }

    /**
     * Get the coefficient of variation from an array.
     */
    public static double coefficientOfVariation(double[] values) {
        double m = mean(values);
        if (m == 0) return 0;
        return standardDeviation(values) / Math.abs(m);
    }
}
