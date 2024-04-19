package fr.farmeurimmo.coreskyblock.purpur.islands;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandWarp;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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

            this.warps.clear();
            for (IslandWarp warp : warps) {
                this.warps.put(warp, System.currentTimeMillis());
            }

            CoreSkyblock.INSTANCE.console.sendMessage("§7Loaded §6" + warps.size() + "§7 warps.");
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

    private ArrayList<IslandWarp> getAllWarps() {
        return new ArrayList<>(warps.keySet());
    }

    public ArrayList<String> getLore(IslandWarp islandWarp) {
        ArrayList<String> lore = new ArrayList<>();
        for (String descLine : islandWarp.getDescription().replace("\\n", "\n").split("\n")) {
            lore.add("§7" + descLine);
        }
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
}
