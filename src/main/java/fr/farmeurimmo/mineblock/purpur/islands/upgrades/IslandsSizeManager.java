package fr.farmeurimmo.mineblock.purpur.islands.upgrades;

import fr.farmeurimmo.mineblock.common.islands.Island;
import fr.farmeurimmo.mineblock.purpur.MineBlock;
import fr.farmeurimmo.mineblock.purpur.islands.IslandsManager;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;

public class IslandsSizeManager {

    public static IslandsSizeManager INSTANCE;

    public IslandsSizeManager() {
        INSTANCE = this;
    }

    public int getSizeFromLevel(int level) {
        return switch (level) {
            case 2 -> 50;
            case 3 -> 100;
            case 4 -> 200;
            case 5 -> 250;
            default -> 25;
        };
    }

    public double getSizePriceFromLevel(int level) {
        return switch (level) {
            case 2 -> 200;
            case 3 -> 500;
            case 4 -> 1000;
            case 5 -> 2000;
            default -> 0;
        };
    }

    public void updateWorldBorder(Island island) {
        if (!island.isLoaded()) return;
        Bukkit.getScheduler().runTaskLater(MineBlock.INSTANCE, () -> {
            World w = Bukkit.getWorld(IslandsManager.INSTANCE.getIslandWorldName(island.getIslandUUID()));
            if (w != null) {
                w.getWorldBorder().setCenter(0, 0);
                w.getWorldBorder().setSize(getSizeFromLevel(island.getMaxSize()));
            }
        }, 5);
    }

    public List<String> getLore(int level) {
        List<String> lore = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            lore.add("§7" + i + ": §6" + IslandsSizeManager.INSTANCE.getSizeFromLevel(i) + "§fx§6" +
                    IslandsSizeManager.INSTANCE.getSizeFromLevel(i) + " §8| " + (level >= i ? "§aDéjà achetée" :
                    "§7Prix: §e" + IslandsSizeManager.INSTANCE.getSizePriceFromLevel(i) + "§6§lexp"));
        }
        return lore;
    }
}
