package fr.farmeurimmo.coreskyblock.purpur.islands;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandWarp;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import net.kyori.adventure.text.Component;
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
                return;
            }
            warp.setName(input);
            p.sendMessage(Component.text("§aNom du warp modifié."));
        } else if (userInputAwaiting.get(p.getUniqueId()).equals("description")) {
            IslandWarp warp = getByIslandUUID(IslandsManager.INSTANCE.getIslandOf(p.getUniqueId()).getIslandUUID());
            if (warp == null) {
                return;
            }
            warp.setDescription(input);
            p.sendMessage(Component.text("§aDescription du warp modifiée."));
        }
    }

    public void addProcessInput(Player p, String input) {
        userInputAwaiting.put(p.getUniqueId(), input);
    }

    public void teleportPlayerToWarp(UUID uuid, UUID islandUUID, Player p) {
        IslandWarp warp = getByIslandUUID(islandUUID);
        if (warp == null) {
            return;
        }
        // teleport player to warp, before that check if island is loaded
        CompletableFuture.runAsync(() -> {
            String server = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:server:" + islandUUID + ":loaded");
            if (server == null) {
                return;
            }

            if (server.equalsIgnoreCase(CoreSkyblock.SERVER_NAME)) {
                p.sendMessage(Component.text("§eTéléportation..."));
                p.teleportAsync(warp.getLocation()).thenAccept(result ->
                        p.sendMessage(Component.text("§aVous avez été téléporté au warp de l'île.")));
                return;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

            try {
                dataOutputStream.writeUTF("Connect");
                dataOutputStream.writeUTF(server);
            } catch (Exception e) {
                e.printStackTrace();
            }

            p.sendPluginMessage(CoreSkyblock.INSTANCE, "BungeeCord", byteArrayOutputStream.toByteArray());

            JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:teleport_warp:" + p.getUniqueId() + ":" + islandUUID);
        });
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

    public ArrayList<String> getLore(IslandWarp islandWarp) {
        ArrayList<String> lore = new ArrayList<>();
        for (String descLine : islandWarp.getDescription().replace("\\n", "\n").split("\n")) {
            lore.add("§7" + descLine);
        }
        return lore;
    }
}
