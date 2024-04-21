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
    private final LinkedHashMap<Pair<UUID, String>, Double> topBankMoney = new LinkedHashMap<>();
    private final LinkedHashMap<Pair<UUID, String>, Double> topWarpRate = new LinkedHashMap<>();
    private final long time_between_updates = 5 * 60 * 1000;
    private long lastUpdate = System.currentTimeMillis();
    private long nextUpdate = -1;

    public IslandsTopManager() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, this::updateTops, 0, 20 * 60 * 5 - 20);
    }

    public LinkedHashMap<Pair<UUID, String>, Double> getTopIslands(int type) {
        return switch (type) {
            case 0 -> topIslands;
            case 1 -> topBankMoney;
            case 2 -> topWarpRate;
            default -> null;
        };
    }

    private void updateTops() {
        topIslands.clear();
        topBankMoney.clear();
        topWarpRate.clear();

        CompletableFuture.supplyAsync(() -> IslandsDataManager.INSTANCE.getIslandTop()).thenAccept(islands -> {
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                topIslands.putAll(islands);
                return null;
            });
        }).thenRunAsync(() -> {
            CompletableFuture.supplyAsync(() -> IslandsDataManager.INSTANCE.getIslandTopBankMoney()).thenAccept(bankMoney -> {
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    topBankMoney.putAll(bankMoney);
                    return null;
                });
            }).thenRunAsync(() -> {
                CompletableFuture.supplyAsync(() -> IslandsDataManager.INSTANCE.getIslandTopWarpRate()).thenAccept(warpRate -> {
                    Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                        topWarpRate.putAll(warpRate);

                        lastUpdate = System.currentTimeMillis();
                        nextUpdate = lastUpdate + time_between_updates;
                        return null;
                    });
                });
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

    public int getPosition(UUID islandUUID, int topType) {
        int pos = 1;
        if (topType == 0) {
            for (Pair<UUID, String> pair : topIslands.keySet()) {
                if (pair.left().equals(islandUUID)) {
                    return pos;
                }
                pos++;
            }
        }
        return -1;
    }
}
