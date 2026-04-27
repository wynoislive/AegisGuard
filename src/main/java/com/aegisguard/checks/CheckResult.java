package com.aegisguard.checks;

/**
 * Result of a check execution.
 */
public record CheckResult(
        boolean failed,
        ViolationLevel severity,
        double confidence,
        String details
) {
    /**
     * Passing result — no violation detected.
     */
    public static CheckResult pass() {
        return new CheckResult(false, ViolationLevel.MINOR, 0.0, "");
    }

    /**
     * Failing result — violation detected.
     */
    public static CheckResult fail(ViolationLevel severity, double confidence, String details) {
        return new CheckResult(true, severity, confidence, details);
    }
}
