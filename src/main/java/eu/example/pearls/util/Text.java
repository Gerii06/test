package eu.example.pearls.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small helper for turning config strings into Adventure {@link Component}s.
 * Supports legacy '&' color codes plus hex colors written as {@code <#RRGGBB>}.
 */
public final class Text {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()
                    .build();

    private Text() {
    }

    /**
     * Parse a config string into a component, translating hex tokens first so the
     * legacy serializer can pick them up.
     */
    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            // Convert <#RRGGBB> into the '&x&R&R&G&G&B&B' form the legacy serializer understands.
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(sb);
        return LEGACY.deserialize(sb.toString()).colorIfAbsent(NamedTextColor.WHITE);
    }

    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }
}
