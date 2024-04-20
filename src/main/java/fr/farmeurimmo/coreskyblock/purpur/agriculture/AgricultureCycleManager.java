package fr.farmeurimmo.coreskyblock.purpur.agriculture;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.agriculture.AgricultureCycleDataManager;
import fr.farmeurimmo.coreskyblock.storage.agriculture.AgricultureCycleSeason;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;

public class AgricultureCycleManager {

    public static AgricultureCycleManager INSTANCE;
    private AgricultureCycleSeason currentSeason;

    public AgricultureCycleManager() {
        INSTANCE = this;

        new AgricultureCycleDataManager();

        CompletableFuture.supplyAsync(() -> AgricultureCycleDataManager.INSTANCE.getCurrentSeason()).thenAccept(season -> {
            if (season == null) throw new NullPointerException();
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                currentSeason = season;
                return null;
            });
        }).exceptionally(e -> {
            AgricultureCycleSeason season = new AgricultureCycleSeason(0, System.currentTimeMillis(),
                    System.currentTimeMillis() + 5L * 5 * 7 * 24 * 60 * 60 * 1000);
            AgricultureCycleDataManager.INSTANCE.updateSeason(season);
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                currentSeason = season;
                return null;
            });
            return null;
        });
    }

    public final AgricultureCycleSeason getCurrentSeason() {
        return currentSeason;
    }
}
