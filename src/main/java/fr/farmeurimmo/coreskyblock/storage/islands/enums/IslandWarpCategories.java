package fr.farmeurimmo.coreskyblock.storage.islands.enums;

import org.bukkit.Material;

public enum IslandWarpCategories {

    PASSIVE_MOBS(0, "Mobs passifs", Material.COW_SPAWN_EGG),
    HOSTILE_MOBS(1, "Mobs hostiles", Material.ZOMBIE_HEAD),
    EXP_FARM(2, "Ferme à XP", Material.EXPERIENCE_BOTTLE),
    SHOPS(3, "Boutiques", Material.CHEST),
    VISIT(4, "Visit", Material.COMPASS),
    OTHER(5, "Autre", Material.STONE);

    private final int id;
    private final String name;
    private final Material material;

    IslandWarpCategories(int id, String name, Material material) {
        this.id = id;
        this.name = name;
        this.material = material;
    }

    public static IslandWarpCategories getById(int id) {
        for (IslandWarpCategories category : values()) {
            if (category.getId() == id) {
                return category;
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
