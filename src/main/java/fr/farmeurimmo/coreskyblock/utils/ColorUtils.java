package fr.farmeurimmo.coreskyblock.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    public static String translate(String messageWithColor) {
        Matcher matcher = Pattern.compile("#[a-fA-F0-9]{6}").matcher(messageWithColor);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String color = messageWithColor.substring(matcher.start(), matcher.end());
            matcher.appendReplacement(sb, ChatColor.of(color).toString());
        }
        return matcher.appendTail(sb).toString();
    }
}
