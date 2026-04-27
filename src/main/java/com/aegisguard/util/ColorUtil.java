package com.aegisguard.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Adventure API color and component utilities.
 */
public final class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private ColorUtil() {}

    /**
     * Parse a MiniMessage string to a Component.
     */
    public static Component parse(String miniMessage) {
        if (miniMessage == null) return Component.empty();
        return MINI_MESSAGE.deserialize(miniMessage);
    }

    /**
     * Parse a legacy (&-code) string to a Component.
     */
    public static Component parseLegacy(String legacyText) {
        if (legacyText == null) return Component.empty();
        return LEGACY.deserialize(legacyText);
    }

    /**
     * Parse with placeholders replaced.
     */
    public static Component parse(String miniMessage, String... placeholders) {
        if (miniMessage == null) return Component.empty();
        String result = miniMessage;
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            result = result.replace(placeholders[i], placeholders[i + 1]);
        }
        return MINI_MESSAGE.deserialize(result);
    }

    /**
     * Serialize a Component to a MiniMessage string.
     */
    public static String serialize(Component component) {
        if (component == null) return "";
        return MINI_MESSAGE.serialize(component);
    }

    /**
     * Create a simple colored text component.
     */
    public static Component text(String text, NamedTextColor color) {
        return Component.text(text, color);
    }

    /**
     * Create a bold colored text component.
     */
    public static Component bold(String text, NamedTextColor color) {
        return Component.text(text, color, TextDecoration.BOLD);
    }

    /**
     * Create an error message component.
     */
    public static Component error(String message) {
        return Component.text(message, NamedTextColor.RED);
    }

    /**
     * Create a success message component.
     */
    public static Component success(String message) {
        return Component.text(message, NamedTextColor.GREEN);
    }

    /**
     * Create a warning message component.
     */
    public static Component warning(String message) {
        return Component.text(message, NamedTextColor.YELLOW);
    }

    /**
     * Create an info message component.
     */
    public static Component info(String message) {
        return Component.text(message, NamedTextColor.GRAY);
    }

    /**
     * Strip all formatting from a string.
     */
    public static String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                .replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }
}
