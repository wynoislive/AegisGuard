package com.aegisguard.checks;

/**
 * Violation severity levels with point values.
 */
public enum ViolationLevel {
    MINOR(2),
    MEDIUM(5),
    STRONG(15),
    CRITICAL(40);

    private final int points;

    ViolationLevel(int points) {
        this.points = points;
    }

    public int getPoints() { return points; }

    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
