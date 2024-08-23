package fr.farmeurimmo.coreskyblock.purpur.tp.warps;

import fr.farmeurimmo.coreskyblock.ServerType;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.mrmicky.fastinv.ItemBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WarpsManager {

    public static final long WARP_REQUEST_EXPIRE_TIME = 25_000L;
    public static WarpsManager INSTANCE;
    public final Map<UUID, Pair<String, Long>> awaitingToTeleport = new HashMap<>();
    public final LinkedHashMap<String, Pair<ServerType, Location>> warps = new LinkedHashMap<>();

    public WarpsManager() {
        INSTANCE = this;

        warps.put("pvp-01", Pair.of(ServerType.PVP, new Location(Bukkit.getWorld(CoreSkyblock.SPAWN_WORLD_NAME), 0.5, 84.5, 13.5, 180, 0)));
        warps.put("pvp-02", Pair.of(ServerType.PVP, new Location(Bukkit.getWorld(CoreSkyblock.SPAWN_WORLD_NAME), 11.5, 84.5, -10.5, 45, 0)));
        warps.put("pvp-03", Pair.of(ServerType.PVP, new Location(Bukkit.getWorld(CoreSkyblock.SPAWN_WORLD_NAME), -11.5, 84.5, -10.5, -45, 0)));
    }

    public String awaitingToTeleport(UUID player) {
        Pair<String, Long> pair = awaitingToTeleport.get(player);
        if (pair == null) {
            return null;
        }
        if (System.currentTimeMillis() - pair.right() >= WARP_REQUEST_EXPIRE_TIME) {
            awaitingToTeleport.remove(player);
            return null;
        }
        return pair.left();
    }

    public boolean isAwaitingToTeleport(UUID player) {
        return awaitingToTeleport(player) != null;
    }

    public Location getWarp(String warpName) {
        return warps.get(warpName).right();
    }

    public String getARandomPVPWarp() {
        ArrayList<String> pvpWarps = new ArrayList<>();
        for (String warp : warps.keySet()) {
            if (warp.startsWith("pvp-")) {
                pvpWarps.add(warp);
            }
        }
        return pvpWarps.get((int) (Math.random() * pvpWarps.size()));
    }

    public void removeAwaitingToTeleport(UUID player) {
        awaitingToTeleport.remove(player);
    }

    public ItemStack getItemStackForWarp(String warpName) {
        if (warpName.contains("pvp")) {
            return new ItemBuilder(new ItemStack(Material.DIAMOND_SWORD)).name("§6" + warpName).build();
        }
        return new ItemBuilder(new ItemStack(Material.COMPASS)).name("§6" + warpName).build();
    }

    public void teleportToWarp(Player p, String warp) {
        Pair<ServerType, Location> location = warps.get(warp);
        if (location == null) {
            p.sendMessage("§cLe warp demandé n'existe pas.");
            return;
        }

        if (location.left() == CoreSkyblock.SERVER_TYPE) {
            p.teleportAsync(location.right())
                    .thenRun(() -> p.sendActionBar(Component.text("§aVous avez été téléporté au warp §e" + warp + "§a.")))
                    .exceptionally(e -> {
                        p.sendMessage(Component.text("§cUne erreur est survenue lors de votre téléportation."));
                        return null;
                    });
            removeAwaitingToTeleport(p.getUniqueId());
            return;
        }

        final String warpWithoutAnyNumber = warp.replaceAll("[0-9]", "").replaceAll("-", "");

        for (String s : CoreSkyblock.INSTANCE.getServersLoad().keySet()) {
            if (s.contains(warpWithoutAnyNumber)) {

                CompletableFuture.runAsync(() -> JedisManager.INSTANCE.publishToRedis("coreskyblock", "server_warp_teleport:" + p.getUniqueId()
                        + ":" + warp + ":" + CoreSkyblock.SERVER_NAME + ":" + s));
                CoreSkyblock.INSTANCE.sendToServer(p, s);
                return;
            }
        }

        p.sendMessage(Component.text("§cUne erreur est survenue lors de votre téléportation."));
        removeAwaitingToTeleport(p.getUniqueId());
    }
}
