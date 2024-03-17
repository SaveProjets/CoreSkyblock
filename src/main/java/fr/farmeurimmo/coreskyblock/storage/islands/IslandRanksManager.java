package fr.farmeurimmo.coreskyblock.storage.islands;

import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;

import java.util.HashMap;

public class IslandRanksManager {

    public static IslandRanksManager INSTANCE;

    private final HashMap<IslandRanks, Integer> islandRankPos = new HashMap<>();

    public IslandRanksManager() {
        INSTANCE = this;
        for (IslandRanks rank : IslandRanks.values()) {
            if (rank == IslandRanks.CHEF) {
                islandRankPos.put(rank, 0);
            }
            if (rank == IslandRanks.COCHEF) {
                islandRankPos.put(rank, 1);
            }
            if (rank == IslandRanks.MODERATEUR) {
                islandRankPos.put(rank, 2);
            }
            if (rank == IslandRanks.MEMBRE) {
                islandRankPos.put(rank, 3);
            }
            if (rank == IslandRanks.COOP) {
                islandRankPos.put(rank, 4);
            }
            if (rank == IslandRanks.VISITEUR) {
                islandRankPos.put(rank, 5);
            }
        }
    }

    public IslandRanks getNextRank(IslandRanks rank) {
        return switch (rank) {
            case MODERATEUR -> IslandRanks.COCHEF;
            case MEMBRE -> IslandRanks.MODERATEUR;
            default -> rank;
        };
    }

    public IslandRanks getPreviousRank(IslandRanks rank) {
        return switch (rank) {
            case COCHEF -> IslandRanks.MODERATEUR;
            case MODERATEUR -> IslandRanks.MEMBRE;
            default -> rank;
        };
    }

    public HashMap<IslandRanks, Integer> getIslandRankPos() {
        return islandRankPos;
    }

    public IslandRanks getNextRankForPerm(IslandPerms perms, Island playerIsland) {
        if (!playerIsland.hasPerms(IslandRanks.COCHEF, perms, null)) {
            return IslandRanks.COCHEF;
        } else if (!playerIsland.hasPerms(IslandRanks.MODERATEUR, perms, null)) {
            return IslandRanks.MODERATEUR;
        } else if (!playerIsland.hasPerms(IslandRanks.MEMBRE, perms, null)) {
            return IslandRanks.MEMBRE;
        } else if (!playerIsland.hasPerms(IslandRanks.COOP, perms, null) && perms.getDescription().startsWith("§f")) {
            return IslandRanks.COOP;
        } else if (!playerIsland.hasPerms(IslandRanks.VISITEUR, perms, null) && perms.getDescription().startsWith("§f")) {
            return IslandRanks.VISITEUR;
        } else {
            if (perms.getDescription().startsWith("§f")) {
                return IslandRanks.VISITEUR;
            } else {
                return IslandRanks.MEMBRE;
            }
        }
    }

    public IslandRanks getPreviousRankForPerm(IslandPerms perms, Island playerIsland) {
        if (playerIsland.hasPerms(IslandRanks.VISITEUR, perms, null) && perms.getDescription().startsWith("§f")) {
            return IslandRanks.VISITEUR;
        } else if (playerIsland.hasPerms(IslandRanks.COOP, perms, null) && perms.getDescription().startsWith("§f")) {
            return IslandRanks.COOP;
        } else if (playerIsland.hasPerms(IslandRanks.MEMBRE, perms, null)) {
            return IslandRanks.MEMBRE;
        } else if (playerIsland.hasPerms(IslandRanks.MODERATEUR, perms, null)) {
            return IslandRanks.MODERATEUR;
        } else if (playerIsland.hasPerms(IslandRanks.COCHEF, perms, null)) {
            return IslandRanks.COCHEF;
        } else {
            return IslandRanks.COCHEF;
        }
    }
}
