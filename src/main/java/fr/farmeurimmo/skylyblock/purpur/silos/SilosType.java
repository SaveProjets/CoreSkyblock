package fr.farmeurimmo.skylyblock.purpur.silos;

import org.bukkit.Material;

public enum SilosType {

    CARROT(0, "Carotte", Material.CARROT, false, null),
    POTATO(1, "Pomme de terre", Material.POTATO, false, null),
    WHEAT(2, "Blé", Material.WHEAT, false, null),
    BEETROOT(3, "Betterave", Material.BEETROOT, false, null),
    SWEET_BERRIES(4, "Baies sucrées", Material.SWEET_BERRIES, false, null),
    PUMPKIN(5, "Citrouille", Material.PUMPKIN, false, null),
    MELON(6, "Melon", Material.MELON, false, Material.MELON_SLICE),
    COCOA_BEANS(7, "Fèves de cacao", Material.COCOA_BEANS, false, null),
    BAMBOO(8, "Bambou", Material.BAMBOO, true, null),
    SUGAR_CANE(9, "Canne à sucre", Material.SUGAR_CANE, true, null),
    NETHER_WART(10, "Verrues du nether", Material.NETHER_WART, false, null),
    CACTUS(11, "Cactus", Material.CACTUS, true, null),
    KELP(12, "Kelp", Material.KELP, true, null);


    private final int id;
    private final String name;
    private final Material material;
    private final boolean isHigh;
    private final Material alternativeMaterial;

    private SilosType(int id, String name, Material material, boolean isHigh, Material alternativeMaterial) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.isHigh = isHigh;
        this.alternativeMaterial = alternativeMaterial;
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

    public static SilosType getByAlternativeMaterial(Material material) {
        for (SilosType silosType : values()) {
            if (silosType.getAlternativeMaterial() != null && silosType.getAlternativeMaterial() == material) {
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

    public boolean isHigh() {
        return isHigh;
    }

    public Material getAlternativeMaterial() {
        return alternativeMaterial;
    }
}
