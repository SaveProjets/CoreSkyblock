package fr.farmeurimmo.mineblock.purpur.islands.upgrades;

import java.util.List;

public class IslandsMaxMembersManager {

    public static IslandsMaxMembersManager INSTANCE;

    public IslandsMaxMembersManager() {
        INSTANCE = this;
    }

    public int getMaxMembersFromLevel(int level) {
        return switch (level) {
            case 2 -> 6;
            case 3 -> 8;
            case 4 -> 10;
            case 5 -> 12;
            default -> 4;
        };
    }

    public double getMembersPriceFromLevel(int level) {
        return switch (level) {
            case 2 -> 200;
            case 3 -> 500;
            case 4 -> 1000;
            case 5 -> 2000;
            default -> 0;
        };
    }

    public boolean isFull(int level, int currentMembers) {
        return currentMembers >= getMaxMembersFromLevel(level);
    }

    public List<String> getLore(int level) {
        List<String> lore = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            lore.add("§7" + i + ": §6" + getMaxMembersFromLevel(i) + " membres §8| " + (level >= i ? "§aDéjà achetée" :
                    "§7Prix: §e" + getMembersPriceFromLevel(i) + "§6§lexp"));
        }
        return lore;
    }
}
