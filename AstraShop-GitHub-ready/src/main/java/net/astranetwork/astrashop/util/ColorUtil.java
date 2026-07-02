package net.astranetwork.astrashop.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central place for turning config strings (MiniMessage tags and/or legacy
 * '&' codes) into Adventure Components, with simple {placeholder} substitution.
 */
public final class ColorUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    // &#RRGGBB hex format
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private static final Map<Character, String> LEGACY_TAGS = Map.ofEntries(
            Map.entry('0', "<black>"), Map.entry('1', "<dark_blue>"), Map.entry('2', "<dark_green>"),
            Map.entry('3', "<dark_aqua>"), Map.entry('4', "<dark_red>"), Map.entry('5', "<dark_purple>"),
            Map.entry('6', "<gold>"), Map.entry('7', "<gray>"), Map.entry('8', "<dark_gray>"),
            Map.entry('9', "<blue>"), Map.entry('a', "<green>"), Map.entry('b', "<aqua>"),
            Map.entry('c', "<red>"), Map.entry('d', "<light_purple>"), Map.entry('e', "<yellow>"),
            Map.entry('f', "<white>"), Map.entry('k', "<obfuscated>"), Map.entry('l', "<bold>"),
            Map.entry('m', "<strikethrough>"), Map.entry('n', "<underlined>"), Map.entry('o', "<italic>"),
            Map.entry('r', "<reset>")
    );

    private ColorUtil() {
    }

    /**
     * Converts legacy '&' codes (including &#RRGGBB hex) into MiniMessage tags,
     * leaving any existing MiniMessage tags untouched, so a string can freely
     * mix both syntaxes.
     */
    private static String legacyToMiniMessage(String raw) {
        Matcher hexMatcher = HEX_PATTERN.matcher(raw);
        StringBuilder sb = new StringBuilder();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(sb, "<#" + hexMatcher.group(1) + ">");
        }
        hexMatcher.appendTail(sb);
        String withHex = sb.toString();

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < withHex.length(); i++) {
            char c = withHex.charAt(i);
            if (c == '&' && i + 1 < withHex.length()) {
                char next = Character.toLowerCase(withHex.charAt(i + 1));
                String tag = LEGACY_TAGS.get(next);
                if (tag != null) {
                    out.append(tag);
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    /**
     * Parses a raw config string into a Component. Supports MiniMessage tags
     * (e.g. <gradient:#ff6ec7:#a260ff>) as well as legacy '&' color codes
     * mixed in the same string.
     */
    public static Component parse(String raw) {
        if (raw == null) {
            return Component.empty();
        }
        try {
            return MINI.deserialize(legacyToMiniMessage(raw));
        } catch (Exception ex) {
            // Fall back to pure legacy parsing if MiniMessage chokes on the input.
            return LEGACY.deserialize(raw);
        }
    }

    /**
     * Parses with {key} -> value placeholder substitution done before parsing.
     */
    public static Component parse(String raw, Map<String, String> placeholders) {
        if (raw == null) {
            return Component.empty();
        }
        String result = raw;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return parse(result);
    }

    /**
     * Convenience: parse and prepend/replace {prefix} with the given prefix string.
     */
    public static Component parseWithPrefix(String raw, String prefixRaw, Map<String, String> placeholders) {
        String result = raw.replace("{prefix}", prefixRaw);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return parse(result);
    }

    public static String plain(Component component) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component);
    }
}
