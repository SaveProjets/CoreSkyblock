package fr.farmeurimmo.coreskyblock.purpur.islands;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandWarp;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandWarpCategories;
import fr.farmeurimmo.coreskyblock.utils.DateUtils;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class IslandsWarpManager {

    public static IslandsWarpManager INSTANCE;

    private final Map<IslandWarp, Long> warps = new HashMap<>(); // warp -> last fetch
    private final Map<UUID, String> userInputAwaiting = new HashMap<>(); // player -> input

    public IslandsWarpManager() {
        INSTANCE = this;

        CompletableFuture.runAsync(() -> {
            List<IslandWarp> warps = IslandsDataManager.INSTANCE.loadIslandsWarps();

            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                this.warps.clear();
                for (IslandWarp warp : warps) {
                    this.warps.put(warp, System.currentTimeMillis());
                }

                CoreSkyblock.INSTANCE.console.sendMessage("§7Loaded §6" + warps.size() + "§7 warps.");
                return null;
            });
        });
    }

    public void updateWarpWithId(UUID uuid, IslandWarp islandWarp) {
        removeWarpWithId(uuid);
        warps.put(islandWarp, System.currentTimeMillis());
    }

    public void removeWarpWithId(UUID uuid) {
        IslandWarp warp = getWarpWithId(uuid);
        if (warp != null) {
            warps.remove(warp);
        }
    }

    public IslandWarp getWarpWithId(UUID uuid) {
        for (IslandWarp warp : warps.keySet()) {
            if (warp.getUuid().equals(uuid)) {
                return warp;
            }
        }
        return null;
    }

    public IslandWarp getByIslandUUID(UUID islandUUID) {
        for (IslandWarp warp : warps.keySet()) {
            if (warp.getIslandUUID().equals(islandUUID)) {
                return warp;
            }
        }
        return null;
    }

    public boolean isAwaitingInput(UUID uuid) {
        return userInputAwaiting.containsKey(uuid);
    }

    public void removeAwaitingInput(UUID uuid) {
        userInputAwaiting.remove(uuid);
    }

    public boolean isAwaitingLongString(UUID uuid) {
        return userInputAwaiting.containsKey(uuid) && userInputAwaiting.get(uuid).equals("description");
    }

    public void processInput(Player p, String input) {
        if (!isAwaitingInput(p.getUniqueId())) {
            return;
        }
        if (userInputAwaiting.get(p.getUniqueId()).equals("name")) {
            IslandWarp warp = getByIslandUUID(IslandsManager.INSTANCE.getIslandOf(p.getUniqueId()).getIslandUUID());
            if (warp == null) {
                p.sendMessage(Component.text("§cErreur interne, annulation."));
                userInputAwaiting.remove(p.getUniqueId());
                return;
            }
            warp.setName(input);
            p.sendMessage(Component.text("§aNom du warp modifié."));
        } else if (userInputAwaiting.get(p.getUniqueId()).equals("description")) {
            IslandWarp warp = getByIslandUUID(IslandsManager.INSTANCE.getIslandOf(p.getUniqueId()).getIslandUUID());
            if (warp == null) {
                p.sendMessage(Component.text("§cErreur interne, annulation."));
                userInputAwaiting.remove(p.getUniqueId());
                return;
            }
            warp.setDescription(input);
            p.sendMessage(Component.text("§aDescription du warp modifiée."));
        }
        userInputAwaiting.remove(p.getUniqueId());
    }

    public void addProcessInput(Player p, String input) {
        userInputAwaiting.put(p.getUniqueId(), input);
    }

    public ArrayList<IslandWarp> getActiveWarps() {
        ArrayList<IslandWarp> activeWarps = new ArrayList<>();
        for (IslandWarp warp : warps.keySet()) {
            if (warp.isActivated()) {
                activeWarps.add(warp);
            }
        }
        return activeWarps;
    }

    public ArrayList<IslandWarp> getActiveWarps(IslandWarpCategories category) {
        ArrayList<IslandWarp> activeWarps = new ArrayList<>();
        for (IslandWarp warp : warps.keySet()) {
            if (warp.isActivated() && warp.getCategories().contains(category)) {
                activeWarps.add(warp);
            }
        }
        return activeWarps;
    }

    private ArrayList<IslandWarp> getAllWarps() {
        return new ArrayList<>(warps.keySet());
    }

    public ArrayList<String> getLore(IslandWarp islandWarp) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§aDescription:");
        boolean first = true;
        for (String descLine : islandWarp.getDescription().replace("\\n", "\n").split("\n")) {
            if (first) {
                lore.add("§f▶  §7" + descLine);
                first = false;
            } else {
                lore.add("    §7" + descLine);
            }
        }
        lore.add("");
        lore.add("§dInformation:");
        lore.add("§f▶ §7Note: §e" + NumberFormat.getInstance().format(islandWarp.getRate()));
        lore.add("§f▶ §7Catégories: §e" + islandWarp.getCategories().stream().map(IslandWarpCategories::getName).reduce((a, b) -> a + "§8, §e" + b).orElse("Aucune"));
        if (islandWarp.isStillForwarded()) lore.add("§f▶ §7Mise en avant restante: " + DateUtils.getFormattedTimeLeft(
                (int) ((islandWarp.getForwardedWarp() - System.currentTimeMillis()) / 1000)));
        return lore;
    }

    public ArrayList<IslandWarp> getForwardedWarps() {
        return new ArrayList<>(getAllWarps().stream().filter(IslandWarp::isStillForwarded).toList());
    }

    public void teleportToWarp(Player p, IslandWarp islandWarp) {
        if (islandWarp == null) {
            p.sendMessage(Component.text("§cCe warp n'existe pas."));
            return;
        }
        p.sendMessage(Component.text("§7Téléportation en cours..."));

        IslandsManager.INSTANCE.checkForDataIntegrity(islandWarp.getIslandUUID().toString(), null, false);

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
            Island island = IslandsManager.INSTANCE.getIslandOf(p.getUniqueId());
            if (island == null) {
                return;
            }
            if (island.isLoaded()) {
                if (islandWarp.getLocation() == null) return;
                if (islandWarp.getLocation().getBlock().getType() != Material.AIR) {
                    p.sendMessage(Component.text("§cErreur téléportation impossible: Le warp n'est pas " +
                            "sécurisé."));
                    island.sendMessageToAll("§cAttention: §e" + p.getName() + " §ca tenté de se téléporter " +
                            "au warp de l'île mais le warp n'est pas sécurisé.");
                    task.cancel();
                    return;
                }
                p.teleportAsync(islandWarp.getLocation()).thenAccept(result ->
                        p.sendMessage(Component.text("§aVous avez été téléporté au warp de l'île.")));
                task.cancel();
                return;
            }
            String serverWhereIslandIsLoaded = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:" + island.getIslandUUID() + ":loaded");
            if (serverWhereIslandIsLoaded == null) {
                return;
            }
            if (serverWhereIslandIsLoaded.equalsIgnoreCase(CoreSkyblock.SERVER_NAME)) {
                p.teleportAsync(islandWarp.getLocation()).thenAccept(result ->
                        p.sendMessage(Component.text("§aVous avez été téléporté au warp de l'île.")));
                task.cancel();
                return;
            }
            JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:teleport_warp:" + p.getUniqueId() + ":"
                    + island.getIslandUUID() + ":" + serverWhereIslandIsLoaded);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

            try {
                dataOutputStream.writeUTF("Connect");
                dataOutputStream.writeUTF(serverWhereIslandIsLoaded);
            } catch (Exception e) {
                e.printStackTrace();
            }

            p.sendPluginMessage(CoreSkyblock.INSTANCE, "BungeeCord", byteArrayOutputStream.toByteArray());
            task.cancel();
        }, 0, 10);
    }

    public List<String> getLastRates(IslandWarp islandWarp) {
        List<String> rates = new ArrayList<>();
        for (Map.Entry<UUID, Pair<Integer, Long>> entry : islandWarp.getRaters().entrySet()) {
            rates.add("§e" + Bukkit.getOfflinePlayer(entry.getKey()).getName() + " §7note §6" +
                    getRateName(entry.getValue().left()) + " §7le §6" + DateUtils.getFormattedDate(entry.getValue().right()));
        }
        return rates;
    }

    public Material getMaterialFromRate(int rate) {
        if (rate == -2) return Material.RED_WOOL;
        if (rate == -1) return Material.ORANGE_WOOL;
        if (rate == 0) return Material.YELLOW_WOOL;
        if (rate == 1) return Material.LIME_WOOL;
        if (rate == 2) return Material.GREEN_WOOL;
        return Material.GRAY_WOOL;
    }

    public String getRateName(int rate) {
        if (rate == -2) return "§4Très mauvais";
        if (rate == -1) return "§cMauvais";
        if (rate == 0) return "§eMoyen";
        if (rate == 1) return "§aBon";
        if (rate == 2) return "§2Très bon";
        return "§4§lErreur";
    }
}
