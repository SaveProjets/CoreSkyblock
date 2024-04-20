package fr.farmeurimmo.coreskyblock.purpur.islands;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import fr.farmeurimmo.coreskyblock.utils.DateUtils;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandsTopManager {

    public static IslandsTopManager INSTANCE;
    private final LinkedHashMap<Pair<UUID, String>, Double> topIslands = new LinkedHashMap<>();
    private final long time_between_updates = 5 * 60 * 1000;
    private long lastUpdate = System.currentTimeMillis();
    private long nextUpdate = -1;

    public IslandsTopManager() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, this::updateTopIslands, 0, 20 * 60 * 5);
    }

    public LinkedHashMap<Pair<UUID, String>, Double> getTopIslands() {
        return topIslands;
    }

    private void updateTopIslands() {
        topIslands.clear();

        CompletableFuture.supplyAsync(() -> IslandsDataManager.INSTANCE.getIslandTop()).thenAccept(islands -> {
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                topIslands.putAll(islands);

                lastUpdate = System.currentTimeMillis();
                nextUpdate = System.currentTimeMillis() + time_between_updates;
                return null;
            });
        });
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public String getTimeUntilRefresh() {
        return DateUtils.getFormattedTimeLeft((int) ((nextUpdate - System.currentTimeMillis()) / 1000));
    }

    public String getTimeAfterRefresh() {
        return DateUtils.getFormattedTimeLeft((int) ((System.currentTimeMillis() - lastUpdate) / 1000));
    }

    public int getPosition(UUID islandUUID) {
        int pos = 1;
        for (Pair<UUID, String> pair : topIslands.keySet()) {
            if (pair.left().equals(islandUUID)) {
                return pos;
            }
            pos++;
        }
        return -1;
    }
}
