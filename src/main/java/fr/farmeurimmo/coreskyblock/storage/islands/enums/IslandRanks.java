package fr.farmeurimmo.coreskyblock.storage.islands.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

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

    public static LinkedList<IslandRanks> getRanksReverse() {
        LinkedList<IslandRanks> ranks = new LinkedList<>(Arrays.asList(IslandRanks.values()));
        Collections.reverse(ranks);
        return ranks;
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

    public static LinkedList<IslandRanks> getAvailableRankForMembers() {
        return new LinkedList<>(Arrays.asList(COCHEF, MODERATEUR, MEMBRE));
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean isExternal() {
        return this == COOP || this == VISITEUR;
    }
}

