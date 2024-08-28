package fr.farmeurimmo.coreskyblock.purpur.islands;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import fr.farmeurimmo.coreskyblock.ServerType;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.dependencies.holograms.DecentHologramAPI;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import fr.farmeurimmo.coreskyblock.utils.DateUtils;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.text.NumberFormat;
import java.util.ArrayList;
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

        if (CoreSkyblock.SERVER_TYPE == ServerType.SPAWN) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () -> updateHolograms(false), 0, 20 * 5);
        }
    }

    private void updateHolograms(boolean full) {
        updateHologram("ISLANDS_Ranking_value", 0, "§6§lTop îles (Valeur)",
                CoreSkyblock.SPAWN.clone().add(-5, 4, -5), full);
        updateHologram("ISLANDS_Ranking_money", 1, "§6§lTop îles (Argent)",
                CoreSkyblock.SPAWN.clone().add(-5, 4, 5), full);
        updateHologram("ISLANDS_Ranking_warp", 2, "§6§lTop îles (Warp)",
                CoreSkyblock.SPAWN.clone().add(5, 4, -5), full);
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

                        Bukkit.getScheduler().runTaskLaterAsynchronously(CoreSkyblock.INSTANCE, () -> updateHolograms(true), 20L);
                        return null;
                    });
                });
            });
        });
    }

    private void updateHologram(String hologramName, int topType, String title, Location location, boolean full) {
        ArrayList<String> defaultLines = new ArrayList<>();
        defaultLines.add(title);
        for (int i = 0; i < 12; i++) {
            defaultLines.add("");
        }

        Hologram hologram = DecentHologramAPI.INSTANCE.getHologram(hologramName);
        if (hologram == null) {
            hologram = DecentHologramAPI.INSTANCE.spawnHologram(hologramName, location, defaultLines);
        }

        HologramPage page = hologram.getPage(0);


        if (full) {
            LinkedHashMap<Pair<UUID, String>, Double> topIslands = IslandsTopManager.INSTANCE.getTopIslands(topType);

            int i = 0;
            for (Pair<UUID, String> island : topIslands.keySet()) {
                if (i >= 10) {
                    break;
                }
                page.setLine(i + 2, "§6#" + (i + 1) + " §6" + island.right().replace("&", "§") + " §7- §6" +
                        NumberFormat.getInstance().format(topIslands.get(island)));
                i++;
            }

            for (int j = i + 2; j < 12; j++) {
                page.setLine(j, "");
            }
        }
        page.setLine(page.size() - 1, "§7Mise à jour dans: §6" + getTimeUntilRefresh());
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
