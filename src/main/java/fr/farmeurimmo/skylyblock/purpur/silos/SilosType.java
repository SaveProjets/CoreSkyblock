package fr.farmeurimmo.skylyblock.purpur.silos;

import org.bukkit.Material;

public enum SilosType {

    CARROT(0, "Carotte", Material.CARROT),
    POTATO(1, "Pomme de terre", Material.POTATO),
    WHEAT(2, "Blé", Material.WHEAT),
    BEETROOT(3, "Betterave", Material.BEETROOT);

    private final int id;
    private final String name;
    private final Material material;

    private SilosType(int id, String name, Material material) {
        this.id = id;
        this.name = name;
        this.material = material;
    }

    public static SilosType getById(int id) {
        for (SilosType silosType : values()) {
            if (silosType.getId() == id) {
                return silosType;
            }
        }
        return null;
    }

    public static SilosType getByName(String name) {
        for (SilosType silosType : values()) {
            if (silosType.getName().equalsIgnoreCase(name)) {
                return silosType;
            }
        }
        return null;
    }

    public static SilosType getByMaterial(Material material) {
        for (SilosType silosType : values()) {
            if (silosType.getMaterial() == material) {
                return silosType;
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
