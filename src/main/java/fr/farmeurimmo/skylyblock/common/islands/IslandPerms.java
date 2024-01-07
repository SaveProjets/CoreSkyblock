package fr.farmeurimmo.skylyblock.common.islands;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public enum IslandPerms {

    PROMOTE("§6Promouvoir un membre inférieur à lui"),
    DEMOTE("§6Rétrograder un membre inférieur à lui"),
    INVITE("§6Inviter un membre"),
    CANCEL_INVITE("§6Annuler les invitations d'un membre"),
    KICK("§6Expulser un membre inférieur à lui"),
    BAN("§6Bannir un visiteur"),
    UNBAN("§6Débannir un visiteur"),
    EXPEL("§6Expulser un visiteur"),
    SET_ISLAND_NAME("§6Définir le nom de l'île"),
    PRIVATE("§6Définir la visibilité de l'île sur privée"),
    PUBLIC("§6Définir la visibilité de l'île sur publique"),
    CHANGE_ISLAND_BIOME("§6Changer le biome de l'île"),
    SET_HOME("§6Définir le home de l'île"),
    CHANGE_PERMS("§6Définir les permissions jusqu'à son niveau de grade"),
    ALL_PERMS("§6Toutes les permissions (Bypass les autres permissions)"),
    ADD_COOP("§6Ajouter un membre temporaire"),
    REMOVE_COOP("§6Retirer un membre temporaire"),
    BANK_ADD("§6Ajouter de l'argent/crystaux/exp à la banque"),
    BANK_REMOVE("§6Retirer de l'argent/crystaux/exp à la banque"),
    BUILD("§fConstruire"),
    BREAK("§fCasser"),
    INTERACT("§fIntéragir avec les blocs"),
    CONTAINER("§fOuvrir les conteneurs"),
    MINIONS_ADD("§6Ajouter un minion"),
    MINIONS_REMOVE("§6Retirer un minion"),
    MINIONS_INTERACT("§6Intéragir avec un minion");

    private final String description;

    IslandPerms(String description) {
        this.description = description;
    }

    public static IslandPerms match(String str) {
        for (IslandPerms islandPerms : values()) {
            if (str.contains(islandPerms.name())) {
                return islandPerms;
            }
        }
        return valueOf(str);
    }

    public static ArrayList<IslandPerms> getAllPerms() {
        return new ArrayList<>(Arrays.asList(IslandPerms.values()));
    }

    public static ItemStack getItemStackForPerm(IslandPerms perm) {
        switch (perm) {
            case BAN -> {
                return new ItemStack(Material.ANVIL);
            }
            case CANCEL_INVITE -> {
                return new ItemStack(Material.KNOWLEDGE_BOOK);
            }
            case CHANGE_ISLAND_BIOME -> {
                return new ItemStack(Material.CLAY);
            }
            case DEMOTE -> {
                return new ItemStack(Material.DIAMOND_SWORD);
            }
            case UNBAN -> {
                return new ItemStack(Material.DAMAGED_ANVIL);
            }
            case PRIVATE -> {
                return new ItemStack(Material.RED_BED);
            }
            case PUBLIC -> {
                return new ItemStack(Material.BLUE_BED);
            }
            case PROMOTE -> {
                return new ItemStack(Material.NETHERITE_SWORD);
            }
            case INVITE -> {
                return new ItemStack(Material.BOOK);
            }
            case KICK -> {
                return new ItemStack(Material.LEATHER_BOOTS);
            }
            case SET_HOME -> {
                return new ItemStack(Material.GRASS_BLOCK);
            }
            case SET_ISLAND_NAME -> {
                return new ItemStack(Material.NAME_TAG);
            }
            case CHANGE_PERMS -> {
                return new ItemStack(Material.EMERALD_BLOCK);
            }
            case BUILD -> {
                return new ItemStack(Material.DIAMOND_PICKAXE);
            }
            case BREAK -> {
                return new ItemStack(Material.DIAMOND_AXE);
            }
            case INTERACT -> {
                return new ItemStack(Material.IRON_HOE);
            }
            case CONTAINER -> {
                return new ItemStack(Material.CHEST);
            }
            case ALL_PERMS -> {
                return new ItemStack(Material.BEDROCK);
            }
            case ADD_COOP -> {
                return new ItemStack(Material.BONE_BLOCK);
            }
            case REMOVE_COOP -> {
                return new ItemStack(Material.BONE_MEAL);
            }
            case BANK_ADD -> {
                return new ItemStack(Material.GOLD_BLOCK);
            }
            case BANK_REMOVE -> {
                return new ItemStack(Material.GOLD_NUGGET);
            }
            case EXPEL -> {
                return new ItemStack(Material.DIAMOND_HOE);
            }
            case MINIONS_ADD -> {
                return new ItemStack(Material.DRAGON_BREATH);
            }
            case MINIONS_REMOVE -> {
                return new ItemStack(Material.DRAGON_EGG);
            }
            case MINIONS_INTERACT -> {
                return new ItemStack(Material.DRAGON_HEAD);
            }
            default -> {
                return new ItemStack(Material.AIR);
            }
        }
    }

    public String getDescription() {
        return description;
    }
}
