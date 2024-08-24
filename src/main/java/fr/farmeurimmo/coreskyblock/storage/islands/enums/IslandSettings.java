package fr.farmeurimmo.coreskyblock.storage.islands.enums;

import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public enum IslandSettings {

    MOB_GRIEFING("§aGrief/Explosion des monstres", List.of("", "§aDescription:", "§f▶ §7Activez ou désactivez",
            "   §7la possibilité aux montres", "   §7d'affecter votre île."), 0),
    MOB_SPAWNING("§aSpawn des mobs", List.of("", "§aDescription:", "§f▶ §7Activez ou désactivez",
            "   §7la possibilité aux montres", "   §7d'apparaître sur votre île."), 1),
    BLOCK_BURNING("§aCombustion des blocs", List.of("", "§aDescription:", "§f▶ §7Activez ou désactivez",
            "   §7la possibilité aux bloc", "   §7de propager et prendre feu."), 2),
    LIGHTNING_STRIKE("§aFoudre", List.of("", "§aDescription:", "§f▶ §7Activez ou désactivez",
            "   §7la possibilité à la foudre", "   §7de s'abattre sur votre île."), 3),
    BLOCK_EXPLOSION("§aExplosions des blocs", List.of("", "§aDescription:", "§f▶ §7Activez ou désactivez",
            "   §7la possibilité à l'explosion", "   §7d'affecter votre île."), 4),

    TIME_DEFAULT("[Temps par défaut]", List.of(), 5),
    TIME_DAY("[Jour permanent]", List.of(), 6),
    TIME_CREPUSCULE("[Crépuscule permanent]", List.of(), 7),
    TIME_NIGHT("[Nuit permanente]", List.of(), 8),
    WEATHER_DEFAULT("[Météo par défaut]", List.of(), 9),
    WEATHER_RAIN("[Météo pluvieuse/neigeuse]", List.of(), 10),
    WEATHER_CLEAR("[Météo claire]", List.of(), 11);

    private final String displayName;
    private final List<String> description;
    private final int id;

    IslandSettings(String displayName, List<String> description, int id) {
        this.displayName = displayName;
        this.description = description;
        this.id = id;
    }

    public static ItemStack getItemForSetting(IslandSettings setting) {
        return switch (setting) {
            case MOB_GRIEFING -> new ItemStack(Material.CREEPER_SPAWN_EGG);
            case MOB_SPAWNING -> new ItemStack(Material.SPAWNER);
            case BLOCK_BURNING -> new ItemStack(Material.FLINT_AND_STEEL);
            case LIGHTNING_STRIKE -> new ItemStack(Material.ANVIL);
            case BLOCK_EXPLOSION -> new ItemStack(Material.TNT);
            case TIME_DEFAULT, TIME_CREPUSCULE, TIME_DAY, TIME_NIGHT -> new ItemStack(Material.DAYLIGHT_DETECTOR);
            case WEATHER_CLEAR, WEATHER_DEFAULT, WEATHER_RAIN -> new ItemStack(Material.CLOCK);
        };
    }

    public static IslandSettings getNext(IslandSettings setting) {
        if (setting.toString().contains("TIME")) {
            return switch (setting) {
                case TIME_DEFAULT -> TIME_DAY;
                case TIME_DAY -> TIME_CREPUSCULE;
                case TIME_CREPUSCULE -> TIME_NIGHT;
                default -> TIME_DEFAULT;
            };
        } else if (setting.toString().contains("WEATHER")) {
            return switch (setting) {
                case WEATHER_DEFAULT -> WEATHER_RAIN;
                case WEATHER_RAIN -> WEATHER_CLEAR;
                default -> WEATHER_DEFAULT;
            };
        }
        return null;
    }

    public static IslandSettings getById(int id) {
        for (IslandSettings setting : values()) {
            if (setting.getId() == id) {
                return setting;
            }
        }
        return null;
    }

    public static ArrayList<IslandSettings> getSettingsOfTheSameType(IslandSettings s) {
        ArrayList<IslandSettings> settings = new ArrayList<>();
        String type = s.name().split("_")[0];
        for (IslandSettings setting : IslandSettings.values()) {
            if (setting.toString().contains(type)) {
                settings.add(setting);
            }
        }
        return settings;
    }

    public int getTime() {
        return switch (this) {
            case TIME_DAY -> 1000;
            case TIME_CREPUSCULE -> 12500;
            case TIME_NIGHT -> 13000;
            default -> 0;
        };
    }

    public String getDisplayName() {
        return displayName;
    }

    public ArrayList<String> getDescription(boolean activated, IslandSettings activatedSetting) {
        ArrayList<String> description = new ArrayList<>(this.description);
        if (this.id <= 4) {
            description.add("");
            description.add("§dInformation:");
            description.add(CommonItemStacks.getArrowWithColors(activated) + "[Activé]");
            description.add(CommonItemStacks.getArrowWithColors(!activated) + "[Désactivé]");
            description.add("");
            description.add("§8➡ §fCliquez pour changer.");
        } else if (this.id <= 11) {
            description.add("");
            description.add("§dInformation:");
            ArrayList<IslandSettings> settings = getSettingsOfTheSameType(this);
            for (IslandSettings setting : settings) {
                description.add(CommonItemStacks.getArrowWithColors(setting == activatedSetting) + setting.getDisplayName());
            }
            description.add("");
            description.add("§8➡ §fCliquez pour changer.");
        }
        return description;
    }

    public int getId() {
        return id;
    }
}
