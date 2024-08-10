package fr.farmeurimmo.coreskyblock.purpur.items.sacs;

import org.bukkit.Material;

public enum SacsType {

    MELON_SLICE(0, "Tranches de melon", Material.MELON_SLICE),
    SWEET_BERRIES(1, "Baies sucrées", Material.SWEET_BERRIES),
    GLOW_BERRIES(2, "Baies lumineuses", Material.GLOW_BERRIES),
    CHORUS_FRUIT(3, "Fruit de chorus", Material.CHORUS_FRUIT),
    CARROT(4, "Carotte", Material.CARROT),
    POTATO(5, "Pomme de terre", Material.POTATO),
    POISONOUS_POTATO(6, "Pomme de terre empoisonnée", Material.POISONOUS_POTATO),
    BEETROOT(7, "Betterave", Material.BEETROOT),
    BEETROOT_SEEDS(8, "Graines de betterave", Material.BEETROOT_SEEDS),
    NETHER_WART(9, "Verrue du Nether", Material.NETHER_WART),
    WHEAT(10, "Blé", Material.WHEAT),
    WHEAT_SEEDS(11, "Graines de blé", Material.WHEAT_SEEDS),
    KELP(12, "Kelp", Material.KELP),
    COCOA_BEANS(13, "Fèves de cacao", Material.COCOA_BEANS),
    BAMBOO(14, "Bambou", Material.BAMBOO),
    SUGAR_CANE(15, "Canne à sucre", Material.SUGAR_CANE),
    CACTUS(16, "Cactus", Material.CACTUS),
    PUMPKIN(17, "Citrouille", Material.PUMPKIN),
    COBBLESTONE(18, "Pierre", Material.COBBLESTONE),
    COAL(19, "Charbon", Material.COAL),
    IRON_INGOT(20, "Lingot de fer", Material.IRON_INGOT),
    RAW_IRON(21, "Minerai de fer", Material.RAW_IRON),
    COPPER_INGOT(22, "Lingot de cuivre", Material.COPPER_INGOT),
    RAW_COPPER(23, "Minerai de cuivre", Material.RAW_COPPER),
    GOLD_INGOT(24, "Lingot d'or", Material.GOLD_INGOT),
    RAW_GOLD(25, "Minerai d'or", Material.RAW_GOLD),
    LAPIS_LAZULI(26, "Lapis-lazuli", Material.LAPIS_LAZULI),
    REDSTONE(27, "Redstone", Material.REDSTONE),
    DIAMOND(28, "Diamant", Material.DIAMOND),
    EMERALD(29, "Émeraude", Material.EMERALD),
    NETHERITE_SCRAP(30, "Éclat de Netherite", Material.NETHERITE_SCRAP),
    NETHER_QUARTZ(31, "Quartz du Nether", Material.QUARTZ),
    AMETHYST_SHARD(32, "Éclat d'améthyste", Material.AMETHYST_SHARD),
    OAK_LOG(33, "Bûche de chêne", Material.OAK_LOG),
    SPRUCE_LOG(34, "Bûche de sapin", Material.SPRUCE_LOG),
    BIRCH_LOG(35, "Bûche de bouleau", Material.BIRCH_LOG),
    JUNGLE_LOG(36, "Bûche de jungle", Material.JUNGLE_LOG),
    ACACIA_LOG(37, "Bûche d'acacia", Material.ACACIA_LOG),
    DARK_OAK_LOG(38, "Bûche de chêne noir", Material.DARK_OAK_LOG);

    private final int id;
    private final String name;
    private final Material material;

    SacsType(int id, String name, Material material) {
        this.id = id;
        this.name = name;
        this.material = material;
    }

    public static SacsType getByMaterial(Material material) {
        for (SacsType sacsType : values()) {
            if (sacsType.getMaterial() == material) {
                return sacsType;
            }
        }
        return null;
    }

    public static SacsType getById(int id) {
        for (SacsType sacsType : values()) {
            if (sacsType.getId() == id) {
                return sacsType;
            }
        }
        return null;
    }

    public static SacsType getByName(String name) {
        for (SacsType sacsType : values()) {
            if (sacsType.getName().contains(name)) {
                return sacsType;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }
}
