package com.aegisguard.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Time formatting utilities.
 */
public final class TimeUtil {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_INSTANT;
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private TimeUtil() {}

    /**
     * Format epoch millis to ISO timestamp.
     */
    public static String toISO(long epochMillis) {
        return ISO_FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }

    /**
     * Format epoch millis to display timestamp.
     */
    public static String toDisplay(long epochMillis) {
        return DISPLAY_FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }

    /**
     * Format duration to human-readable string (e.g., "2h 30m 15s").
     */
    public static String formatDuration(long seconds) {
        if (seconds < 0) return "permanent";
        Duration d = Duration.ofSeconds(seconds);
        long days = d.toDays();
        long hours = d.toHours() % 24;
        long mins = d.toMinutes() % 60;
        long secs = d.getSeconds() % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (mins > 0) sb.append(mins).append("m ");
        if (secs > 0 || sb.isEmpty()) sb.append(secs).append("s");
        return sb.toString().trim();
    }

    /**
     * Format playtime from milliseconds to human-readable string.
     */
    public static String formatPlaytime(long millis) {
        return formatDuration(millis / 1000);
    }

    /**
     * Get current epoch millis.
     */
    public static long now() {
        return System.currentTimeMillis();
    }

    /**
     * Get current epoch seconds.
     */
    public static long nowSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Check if a timestamp has expired.
     */
    public static boolean hasExpired(long epochMillis) {
        return epochMillis > 0 && System.currentTimeMillis() > epochMillis;
    }

    /**
     * Parse duration string like "30m", "2h", "7d" to seconds.
     */
    public static long parseDuration(String input) {
        if (input == null || input.isEmpty()) return -1;
        input = input.trim().toLowerCase();
        try {
            char unit = input.charAt(input.length() - 1);
            long value = Long.parseLong(input.substring(0, input.length() - 1));
            return switch (unit) {
                case 's' -> value;
                case 'm' -> value * 60;
                case 'h' -> value * 3600;
                case 'd' -> value * 86400;
                case 'w' -> value * 604800;
                default -> Long.parseLong(input);
            };
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
