package fr.farmeurimmo.coreskyblock.common.islands;

import org.bukkit.Material;

public enum IslandPerms {

    PROMOTE("§6Promouvoir un membre inférieur à lui", 0, Material.NETHERITE_SWORD), // implemented
    DEMOTE("§6Rétrograder un membre inférieur à lui", 1, Material.DIAMOND_SWORD), // implemented
    INVITE("§6Inviter un membre", 2, Material.BOOK), // implemented
    CANCEL_INVITE("§6Annuler les invitations d'un membre", 3, Material.KNOWLEDGE_BOOK), // implemented
    KICK("§6Expulser un membre inférieur à lui", 4, Material.LEATHER_BOOTS), // implemented
    BAN("§6Bannir un visiteur", 5, Material.ANVIL), // not
    UNBAN("§6Débannir un visiteur", 6, Material.DAMAGED_ANVIL), // not
    EXPEL("§6Expulser un visiteur", 7, Material.DIAMOND_HOE),  // implemented
    SET_ISLAND_NAME("§6Définir le nom de l'île", 8, Material.NAME_TAG), // not
    PRIVATE("§6Définir la visibilité de l'île sur privée", 9, Material.RED_BED), // implemented
    PUBLIC("§6Définir la visibilité de l'île sur publique", 10, Material.BLUE_BED), // implemented
    CHANGE_ISLAND_BIOME("§6Changer le biome de l'île", 11, Material.CLAY), // not
    SET_HOME("§6Définir le home de l'île", 12, Material.GRASS_BLOCK), // not
    CHANGE_PERMS("§6Définir les permissions depuis son niveau de grade", 13, Material.EMERALD_BLOCK), // implemented
    ALL_PERMS("§6Toutes les permissions (Bypass les autres permissions)", 14, Material.BEDROCK), // implemented
    ADD_COOP("§6Ajouter un membre temporaire", 15, Material.BONE_BLOCK), // not
    REMOVE_COOP("§6Retirer un membre temporaire", 16, Material.BONE_MEAL), // not
    BANK_ADD("§6Ajouter de l'argent/crystaux/exp à la banque", 17, Material.GOLD_BLOCK), // implemented
    BANK_REMOVE("§6Retirer de l'argent/crystaux/exp à la banque", 18, Material.GOLD_NUGGET), // implemented
    BUILD("§fConstruire", 19, Material.DIAMOND_PICKAXE), // implemented
    BREAK("§fCasser", 20, Material.DIAMOND_AXE), // implemented
    INTERACT("§fIntéragir avec les blocs", 21, Material.IRON_HOE), // implemented
    CONTAINER("§fOuvrir les conteneurs", 22, Material.CHEST), // implemented
    MINIONS_ADD("§6Ajouter un minion", 23, Material.DRAGON_BREATH), // not
    MINIONS_REMOVE("§6Retirer un minion", 24, Material.DRAGON_EGG), // not
    MINIONS_INTERACT("§6Intéragir avec un minion", 25, Material.DRAGON_HEAD), // not
    SECURED_CHEST("§6Utiliser un coffre sécurisé", 26, Material.IRON_CHESTPLATE), // implemented
    CALCULATE_ISLAND_LEVEL("§6Calculer le niveau de l'île", 27, Material.CALCITE); // implemented

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
