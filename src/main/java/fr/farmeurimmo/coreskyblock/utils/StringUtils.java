package fr.farmeurimmo.coreskyblock.utils;

import java.util.Set;
import java.util.stream.Collectors;

public class StringUtils {

    private static final Set<Character> ALLOWED_UNICODE_CHARS = Set.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ',
            ',', '.', '!', '?', ';', ':', '_', '-', '\'', '\"', '(', ')', '[', ']', '{', '}', '<', '>', '/', '\\',
            '|', '@', '#', '$', '%', '^', '&', '*', '~', '`', '=', '+', '§', '°', 'µ', '£', '€', '¥', '¢', '¤', 'é',
            'è', 'à', 'ù', 'â', 'ê', 'î', 'ô', 'û', 'ä', 'ë', 'ï', 'ö', 'ü', 'ÿ', 'ç', 'Ç', 'É', 'È', 'À', 'Ù', 'Â',
            'Ê', 'Î', 'Ô', 'Û', 'Ä', 'Ë', 'Ï', 'Ö', 'Ü', 'Ÿ', 'æ', 'Æ', 'œ', 'Œ', 'ß', '¿', '¡', 'ñ'
    );

    public static String getTheMessageWithOnlyAllowedCharacters(String message) {
        return message.chars()
                .filter(c -> ALLOWED_UNICODE_CHARS.contains((char) c))
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());
    }
}
