package fr.farmeurimmo.mineblock.common.islands;

public enum IslandRanks {

    CHEF("Chef", 0),
    COCHEF("Cochef", 1),
    MODERATEUR("Modérateur", 2),
    MEMBRE("Membre", 3),
    COOP("Membre temporaire", 4),
    VISITEUR("Visiteur", 5);

    private final String name;
    private final int id;

    IslandRanks(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public static IslandRanks match(String str) {
        for (IslandRanks islandRanks : values()) {
            if (str.contains(islandRanks.name())) {
                return islandRanks;
            }
        }
        return valueOf(str);
    }

    public static IslandRanks getById(int id) {
        for (IslandRanks islandRanks : values()) {
            if (islandRanks.getId() == id) {
                return islandRanks;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}

