package fr.farmeurimmo.coreskyblock.purpur.enchants.enums;

public enum EnchantmentRarity {

    UNCOMMON(0, "§a§l"),
    RARE(1, "§9§l"),
    EPIC(2, "§d§l");

    private final int id;
    private final String color;

    EnchantmentRarity(int id, String color) {
        this.id = id;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }
}
