package com.aegisguard.alerts.discord;

import java.util.*;

/**
 * Discord embed builder for webhook messages.
 */
public final class EmbedBuilder {

    private String title;
    private String description;
    private int color;
    private String thumbnail;
    private String footerText;
    private String footerIcon;
    private String timestamp;
    private final List<Map<String, Object>> fields = new ArrayList<>();

    public EmbedBuilder title(String title) { this.title = title; return this; }
    public EmbedBuilder description(String description) { this.description = description; return this; }
    public EmbedBuilder color(int color) { this.color = color; return this; }
    public EmbedBuilder thumbnail(String url) { this.thumbnail = url; return this; }
    public EmbedBuilder footer(String text, String icon) { this.footerText = text; this.footerIcon = icon; return this; }
    public EmbedBuilder timestamp(String iso) { this.timestamp = iso; return this; }

    public EmbedBuilder field(String name, String value, boolean inline) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", name);
        field.put("value", value);
        field.put("inline", inline);
        fields.add(field);
        return this;
    }

    /**
     * Build the embed as a Map for JSON serialization.
     */
    public Map<String, Object> build() {
        Map<String, Object> embed = new LinkedHashMap<>();
        if (title != null) embed.put("title", title);
        if (description != null) embed.put("description", description);
        if (color != 0) embed.put("color", color);
        if (thumbnail != null) {
            Map<String, String> thumb = new LinkedHashMap<>();
            thumb.put("url", thumbnail);
            embed.put("thumbnail", thumb);
        }
        if (!fields.isEmpty()) embed.put("fields", fields);
        if (footerText != null) {
            Map<String, String> footer = new LinkedHashMap<>();
            footer.put("text", footerText);
            if (footerIcon != null && !footerIcon.isEmpty()) footer.put("icon_url", footerIcon);
            embed.put("footer", footer);
        }
        if (timestamp != null) embed.put("timestamp", timestamp);
        return embed;
    }
}
