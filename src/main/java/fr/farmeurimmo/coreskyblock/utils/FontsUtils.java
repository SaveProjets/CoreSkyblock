package fr.farmeurimmo.coreskyblock.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FontsUtils {

    private static final Key FONT_KEY = Key.key("mono");
    private static final List<String> SPECIAL_CHARS = List.of("➡");
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§([0-9a-fA-Fk-oK-OrR])");

    public static Component applyFont(String text) {
        return Component.text(text).font(FONT_KEY);
    }

    public static List<Component> applyFont(List<String> text) {
        return text.stream().map(FontsUtils::applyFontToLine).toList();
    }

    // TEMPORARY: This method is used to apply a custom font to a specific character, it will be removed in the future
    @Deprecated
    private static Component applyFontToLine(String line) {
        List<Component> components = new ArrayList<>();

        for (String specialChar : SPECIAL_CHARS) {
            if (line.contains(specialChar)) {
                String[] parts = line.split(specialChar, 2);
                String colorCode = "";
                if (parts.length > 1) {
                    Matcher matcher = COLOR_CODE_PATTERN.matcher(parts[0]);
                    while (matcher.find()) {
                        colorCode = "§" + matcher.group(1);
                    }
                }
                components.add(Component.text(parts[0])
                        .append(Component.text(colorCode + specialChar))
                        .append(Component.text(parts[1]).font(FONT_KEY)));
            } else {
                components.add(applyFont(line));
            }
        }
        return components.stream().reduce(Component.empty(), Component::append);
    }
}
