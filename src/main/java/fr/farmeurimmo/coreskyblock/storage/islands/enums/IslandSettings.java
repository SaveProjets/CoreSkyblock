package fr.farmeurimmo.coreskyblock.storage.islands.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum IslandSettings {

    MOB_GRIEFING("§7Grief/Explosion des mobs", 0),
    MOB_SPAWNING("§7Spawn des mobs", 1),
    BLOCK_BURNING("§7Combustion des blocs", 2),
    LIGHTNING_STRIKE("§7Foudre", 3),
    BLOCK_EXPLOSION("§7Explosions des blocs", 4),
    TIME_DEFAULT("§7Temps par défaut", 5),
    TIME_DAY("§7Jour permanant", 6),
    TIME_CREPUSCULE("§7Crépuscule permanant", 7),
    TIME_NIGHT("§7Nuit permanante", 8),
    WEATHER_DEFAULT("§7Météo par défaut", 9),
    WEATHER_RAIN("§7Météo pluvieuse/neigeuse", 10),
    WEATHER_CLEAR("§7Météo claire", 11);

    private final String desc;
    private final int id;

    IslandSettings(String desc, int id) {
        this.desc = desc;
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

    public int getTime() {
        return switch (this) {
            case TIME_DAY -> 1000;
            case TIME_CREPUSCULE -> 12500;
            case TIME_NIGHT -> 13000;
            default -> 0;
        };
    }

    public String getDesc() {
        return desc;
    }

    public int getId() {
        return id;
    }
}
