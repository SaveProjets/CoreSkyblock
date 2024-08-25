package fr.farmeurimmo.coreskyblock.purpur.islands.upgrades;

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
        lore.add("");
        lore.add("§aDescription:");
        lore.add("§f▶  §7Augmenter la limite");
        lore.add("    §7de membre de votre île.");
        lore.add("");
        lore.add("§dInformation:");
        for (int i = 1; i <= 5; i++) {
            lore.add("§f▶ §7" + getMaxMembersFromLevel(i) + " membres §8| " + (level >= i ? "§aPossédée" :
                    "§7Prix: §d§l" + getMembersPriceFromLevel(i) + "§dexp"));
        }
        lore.add("");
        lore.add("§8➡ §fCliquez pour améliorer.");
        return lore;
    }
}
