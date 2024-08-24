package fr.farmeurimmo.coreskyblock.storage.islands.enums;

import org.bukkit.Material;

import java.util.ArrayList;

public enum IslandWarpCategories {

    NOTHING(0, "[Sans filtre]", Material.BARRIER),
    PASSIVE_MOBS(1, "[Mobs passifs]", Material.COW_SPAWN_EGG),
    HOSTILE_MOBS(2, "[Mobs hostiles]", Material.ZOMBIE_HEAD),
    EXP_FARM(3, "[Ferme à XP]", Material.EXPERIENCE_BOTTLE),
    SHOPS(4, "[Boutiques]", Material.CHEST),
    VISIT(5, "[Visite]", Material.COMPASS),
    OTHER(6, "[Autre]", Material.STONE);

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

    public static ArrayList<IslandWarpCategories> getCategories() {
        ArrayList<IslandWarpCategories> categories = new ArrayList<>();
        for (IslandWarpCategories category : values()) {
            if (category == NOTHING) continue;
            categories.add(category);
        }
        return categories;
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
