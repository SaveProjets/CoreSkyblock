package fr.farmeurimmo.coreskyblock.storage.islands.enums;

import org.bukkit.Material;

import java.util.ArrayList;

public enum IslandPerms {

    CHANGE_RANK("§6Modifier le rang d'un membre inférieur à lui", 0, Material.NETHERITE_SWORD), // implemented
    INVITES("§6Gérer les invitations", 1, Material.BOOK), // implemented
    KICK("§6Expulser un membre inférieur à lui", 2, Material.LEATHER_BOOTS), // implemented
    EDIT_BANS("§6Gérer les bannissements de l'île", 3, Material.ANVIL), // implemented
    EXPEL("§6Expulser un visiteur", 4, Material.DIAMOND_HOE),  // implemented
    SET_ISLAND_NAME("§6Définir le nom de l'île", 5, Material.NAME_TAG), // implemented
    EDIT_PUBLIC("§6Ouvrir et Fermer l'île aux visiteurs", 6, Material.RED_BED), // implemented
    CHANGE_ISLAND_BIOME("§6Changer le biome de l'île", 7, Material.CLAY), // not
    SET_HOME("§6Définir le home de l'île", 8, Material.GRASS_BLOCK), // implemented
    CHANGE_PERMS("§6Pouvoir changer les permissions d'un rang", 9, Material.EMERALD_BLOCK), // implemented
    ALL_PERMS("§6Permissions admin (Attention ne pas mettre sur un coup de tête)", 10, Material.BEDROCK), // implemented
    EDIT_COOP("§6Gérer les coops", 11, Material.BONE_BLOCK), // implemented
    BANK_ADD("§6Ajouter de l'argent/crystaux/exp à la banque", 12, Material.GOLD_BLOCK), // implemented
    BANK_REMOVE("§6Retirer de l'argent/crystaux/exp à la banque", 13, Material.GOLD_NUGGET), // implemented
    BUILD("§fPoser des blocs", 14, Material.DIAMOND_PICKAXE), // implemented
    BREAK("§fCasser des blocs", 15, Material.DIAMOND_AXE), // implemented
    INTERACT("§fIntéragir avec les blocs", 16, Material.IRON_HOE), // implemented
    CONTAINER("§fOuvrir les conteneurs", 17, Material.CHEST), // implemented
    EDIT_MINIONS("§6Gérer les minions", 18, Material.DRAGON_BREATH), // not
    SECURED_CHEST("§6Utiliser un coffre sécurisé", 19, Material.IRON_CHESTPLATE), // implemented
    CALCULATE_ISLAND_LEVEL("§6Calculer le niveau de l'île", 20, Material.CALCITE), // implemented
    FEED_ANIMALS("§fNourrir les animaux", 21, Material.COOKED_BEEF), // implemented
    KILL_ANIMALS("§fTuer les animaux", 22, Material.COOKED_CHICKEN), // implemented
    KILL_MOBS("§fTuer les monstres", 23, Material.ROTTEN_FLESH), // implemented
    EDIT_ISLAND_WARP("§6Gérer le warp de l'île", 24, Material.ENDER_PEARL), // implemented
    EDIT_ISLAND_CHARACTERISTICS("§6Gérer les caractéristiques de l'île", 25, Material.BOOK), // not
    DROP_ITEMS("§fJeter des items", 26, Material.DIRT), // implemented
    FISH("§fPêcher", 27, Material.FISHING_ROD), // implemented
    FLY("§fVoler sur l'île", 28, Material.ELYTRA), // implemented
    INTERACT_WITH_MOUNTS("§fIntéragir avec les montures", 29, Material.SADDLE), // implemented
    USE_SPAWN_EGG("§fUtiliser des spawn eggs", 30, Material.PIG_SPAWN_EGG), // implemented
    PICKUP_ITEMS("§fRamasser des items", 31, Material.IRON_SHOVEL), // implemented
    UPGRADE_ISLAND("§6Améliorer l'île", 32, Material.DIAMOND_BLOCK), // implemented
    EDIT_SETTINGS("§6Modifier les paramètres de l'île", 33, Material.COMPARATOR), // implemented
    BREAK_SPAWNERS("§6Casser les spawners", 34, Material.SPAWNER), // implemented
    BREAK_ISLAND_LEVEL_BLOCKS("§6Casser les blocs du niveau de l'île", 35, Material.CALCITE), // implemented
    INTERACT_WITH_VILLAGERS("§fIntéragir avec les villageois", 36, Material.VILLAGER_SPAWN_EGG), // implemented
    INTERACT_WITH_ITEM_FRAMES("§fIntéragir avec les cadres d'items", 37, Material.ITEM_FRAME); // implemented


    private final String description;
    private final int id;
    private final Material material;

    IslandPerms(String description, int id, Material material) {
        this.description = description;
        this.id = id;
        this.material = material;
    }

    public static IslandPerms match(String str) {
        for (IslandPerms islandPerms : values()) {
            if (str.contains(islandPerms.name())) {
                return islandPerms;
            }
        }
        return valueOf(str);
    }

    public static IslandPerms getById(int id) {
        for (IslandPerms islandPerms : values()) {
            if (islandPerms.getId() == id) {
                return islandPerms;
            }
        }
        return null;
    }

    public static ArrayList<IslandPerms> getPerms(int startingAt, int maxAmount) {
        if (startingAt >= values().length) return new ArrayList<>();
        ArrayList<IslandPerms> perms = new ArrayList<>();
        int i = 0;
        for (IslandPerms perm : values()) {
            if (i >= startingAt && i < startingAt + maxAmount) {
                perms.add(perm);
            }
            i++;
        }
        return perms;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }
}
