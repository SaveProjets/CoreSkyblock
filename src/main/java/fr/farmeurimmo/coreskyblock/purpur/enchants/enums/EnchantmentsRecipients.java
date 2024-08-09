package fr.farmeurimmo.coreskyblock.purpur.enchants.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum EnchantmentsRecipients {

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

    public static EnchantmentsRecipients getFromItemStack(ItemStack itemStack) {
        String name = itemStack.getType().name();
        if (name.contains("HELMET")) return HELMET;
        if (name.contains("CHESTPLATE")) return CHESTPLATE;
        if (name.contains("LEGGINGS")) return LEGGINGS;
        if (name.contains("BOOTS")) return BOOTS;

        if (name.contains("SWORD")) return SWORD;
        if (name.contains("BOW") || name.contains("CROSSBOW")) return BOW_AND_CROSSBOW;

        if (name.contains("PICKAXE")) return PICKAXE;
        if (name.contains("AXE")) return AXE;
        if (name.contains("HOE")) return HOE;
        if (name.contains("ROD")) return ROD;
        if (name.contains("SHOVEL")) return SHOVEL;

        return null;
    }

    public Material getMaterialForDisplay() {
        return switch (this) {
            case HELMET -> Material.DIAMOND_HELMET;
            case CHESTPLATE -> Material.DIAMOND_CHESTPLATE;
            case LEGGINGS -> Material.DIAMOND_LEGGINGS;
            case BOOTS -> Material.DIAMOND_BOOTS;
            case SWORD -> Material.DIAMOND_SWORD;
            case BOW_AND_CROSSBOW -> Material.BOW;
            case PICKAXE -> Material.DIAMOND_PICKAXE;
            case AXE -> Material.DIAMOND_AXE;
            case HOE -> Material.DIAMOND_HOE;
            case ROD -> Material.FISHING_ROD;
            case SHOVEL -> Material.DIAMOND_SHOVEL;
            default -> Material.BARRIER;
        };
    }

    public String getName() {
        return name;
    }
}
