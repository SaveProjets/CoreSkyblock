package fr.farmeurimmo.skylyblock.purpur.minions;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public enum MinionType {

    PIOCHEUR(0, "Piocheur"),
    AUTRE(99, "Autre");

    private final int id;
    private final String name;

    MinionType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static MinionType getById(int id) {
        for (MinionType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    public static MinionType getByName(String name) {
        for (MinionType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public static ItemStack getMinionHelmet(MinionType type) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        applyMeta(type, helmet);
        return helmet;
    }

    public static void applyMeta(MinionType type, ItemStack item) {
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        switch (type) {
            case PIOCHEUR:
                meta.setColor(Color.fromBGR(249, 128, 29));
                break;
            case AUTRE:
                meta.setColor(Color.fromBGR(1, 1, 1));
                break;
        }
        item.setItemMeta(meta);

    }

    public static ItemStack getMinionChestplate(MinionType type) {
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        applyMeta(type, chestplate);
        return chestplate;
    }

    public static ItemStack getMinionLeggings(MinionType type) {
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        applyMeta(type, leggings);
        return leggings;
    }

    public static ItemStack getMinionBoots(MinionType type) {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        applyMeta(type, boots);
        return boots;
    }

    public static ItemStack getTool(MinionType type) {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);

        //If the type is not a piocheur, return something else
        if (type == AUTRE) {
            tool.setType(Material.DIAMOND_AXE);
        }

        ItemMeta meta = tool.getItemMeta();
        meta.setUnbreakable(true);
        tool.setItemMeta(meta);
        return tool;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }
}
