package fr.farmeurimmo.coreskyblock.purpur.enchants.enums;

public enum EnchantmentsRecipients {

    ARMOR("Armure"),
    HELMET("Casque"),
    CHESTPLATE("Plastron"),
    LEGGINGS("Jambières"),
    BOOTS("Bottes"),
    SWORD("Epée"),
    BOW_AND_CROSSBOW("Arc et Arbalète"),
    PICKAXE("Pioche"),
    AXE("Hache"),
    HOE("Houe"),
    ROD("Canne à pêche"),
    SHOVEL("Pelle");

    private final String name;

    EnchantmentsRecipients(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
