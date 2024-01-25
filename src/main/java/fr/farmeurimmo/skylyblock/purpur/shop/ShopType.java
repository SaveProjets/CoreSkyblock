package fr.farmeurimmo.skylyblock.purpur.shop;

public enum ShopType {

    BLOCKS("§6Blocs"),
    DYES("§6Colorants"),
    AGRICULTURE("§6Agriculture"),
    FOOD("§6Nourritures"),
    ORES("§6Minerais"),
    OTHERS("§6Autres"),
    MOBS("§6Loots de mobs"),
    REDSTONE("§6Redstone"),
    SPAWNERS("§6Spawneurs");

    private final String name;

    ShopType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
