package fr.farmeurimmo.coreskyblock.storage.islands.enums;

public enum IslandWarpCategories {

    PASSIVE_MOBS(0, "Mobs passifs"),
    HOSTILE_MOBS(1, "Mobs hostiles"),
    EXP_FARM(2, "Ferme à XP"),
    SHOPS(3, "Boutiques"),
    VISIT(4, "Visit"),
    OTHER(5, "Autre");

    private final int id;
    private final String name;

    IslandWarpCategories(int id, String name) {
        this.id = id;
        this.name = name;
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
}
