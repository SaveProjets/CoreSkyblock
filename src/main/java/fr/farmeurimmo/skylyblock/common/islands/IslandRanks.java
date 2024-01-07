package fr.farmeurimmo.skylyblock.common.islands;

public enum IslandRanks {

    CHEF("Chef"),
    COCHEF("Cochef"),
    MODERATEUR("Modérateur"),
    MEMBRE("Membre"),
    COOP("Membre temporaire"),
    VISITEUR("Visiteur");

    private String name;

    IslandRanks(String name) {
        this.name = name;
    }

    public static IslandRanks match(String str) {
        for (IslandRanks islandRanks : values()) {
            if (str.contains(islandRanks.name())) {
                return islandRanks;
            }
        }
        return valueOf(str);
    }

    public String getName() {
        return name;
    }
}

