package fr.farmeurimmo.mineblock.purpur.islands.upgrades;

import fr.farmeurimmo.mineblock.common.islands.Island;
import fr.farmeurimmo.mineblock.purpur.MineBlock;
import fr.farmeurimmo.mineblock.purpur.islands.IslandsManager;
import org.bukkit.Bukkit;
import org.bukkit.World;

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
}
