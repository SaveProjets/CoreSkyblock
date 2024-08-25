package fr.farmeurimmo.coreskyblock.purpur.islands.upgrades;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;

public class IslandsSizeManager {

    public static IslandsSizeManager INSTANCE;

    public IslandsSizeManager() {
        INSTANCE = this;
    }

    public double getSizeFromLevel(int level) {
        return switch (level) {
            case 2 -> 50.5;
            case 3 -> 100.5;
            case 4 -> 200.5;
            case 5 -> 250.5;
            default -> 25.5;
        };
    }

    public int getSizeFromLevelRounded(int level) {
        return (int) getSizeFromLevel(level);
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
        Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> {
            World w = Bukkit.getWorld(IslandsManager.INSTANCE.getIslandWorldName(island.getIslandUUID()));
            if (w != null) {
                w.getWorldBorder().setCenter(0.5, 0.5);
                w.getWorldBorder().setSize(getSizeFromLevel(island.getMaxSize()) * 2);
            }
        }, 5);
    }

    public List<String> getLore(int level) {
        List<String> lore = new java.util.ArrayList<>();
        lore.add("");
        lore.add("§aDescription:");
        lore.add("§f▶  §7Améliorer la taille");
        lore.add("    §7de votre île afin d'avoir");
        lore.add("    §7un terrain plus élevé.");
        lore.add("");
        lore.add("§dInformation:");
        for (int i = 1; i <= 5; i++) {
            lore.add("§f▶ §7" + IslandsSizeManager.INSTANCE.getSizeFromLevelRounded(i) + "§8x§7" +
                    IslandsSizeManager.INSTANCE.getSizeFromLevelRounded(i) + " §8| " + (level >= i ? "§aPossédée" :
                    "§7Prix: §d§l" + IslandsSizeManager.INSTANCE.getSizePriceFromLevel(i) + "§dexp"));
        }
        lore.add("");
        lore.add("§8➡ §fCliquez pour améliorer.");
        return lore;
    }
}
