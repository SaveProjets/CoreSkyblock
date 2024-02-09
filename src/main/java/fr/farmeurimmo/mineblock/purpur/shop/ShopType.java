package fr.farmeurimmo.mineblock.purpur.shop;

public enum ShopType {

    BLOCKS("§8Blocs"),
    DYES("§8Colorants"),
    AGRICULTURE("§8Agriculture"),
    FOOD("§8Nourritures"),
    ORES("§8Minerais"),
    OTHERS("§8Autres"),
    MOBS("§8Loots de mobs"),
    REDSTONE("§8Redstone"),
    SPAWNERS("§8Spawneurs");

    private final String name;

    ShopType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
