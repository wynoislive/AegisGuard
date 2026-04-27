package com.aegisguard.config;

import com.aegisguard.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

/**
 * Typed wrapper for messages.yml.
 */
public final class MessagesConfig {

    private final FileConfiguration config;

    public MessagesConfig(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Get a raw message string by path.
     */
    public String getRaw(String path) {
        return config.getString(path, "<red>Missing message: " + path + "</red>");
    }

    /**
     * Get a parsed Component by path.
     */
    public Component get(String path) {
        return ColorUtil.parse(getRaw(path));
    }

    /**
     * Get a parsed Component with placeholders replaced.
     */
    public Component get(String path, String... placeholders) {
        String raw = getRaw(path);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            raw = raw.replace(placeholders[i], placeholders[i + 1]);
        }
        // Replace {prefix}
        String prefix = config.getString("prefix", "");
        raw = raw.replace("{prefix}", prefix);
        return ColorUtil.parse(raw);
    }

    /**
     * Get a message with map-based placeholders.
     */
    public Component get(String path, Map<String, String> placeholders) {
        String raw = getRaw(path);
        String prefix = config.getString("prefix", "");
        raw = raw.replace("{prefix}", prefix);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return ColorUtil.parse(raw);
    }

    /**
     * Get the prefix.
     */
    public String getPrefix() {
        return config.getString("prefix", "");
    }
}
