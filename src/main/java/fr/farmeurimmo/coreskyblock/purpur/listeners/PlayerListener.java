package fr.farmeurimmo.coreskyblock.purpur.listeners;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.scoreboard.ScoreboardManager;
import fr.farmeurimmo.coreskyblock.purpur.sync.SyncUsersManager;
import fr.farmeurimmo.coreskyblock.purpur.trade.TradesManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.concurrent.CompletableFuture;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        e.joinMessage(null);
        p.teleportAsync(CoreSkyblock.SPAWN);

        ScoreboardManager.INSTANCE.addPlayer(p);

        CompletableFuture.runAsync(() -> {
            SyncUsersManager.INSTANCE.startPlayerSync(p);
            SkyblockUser user = SkyblockUsersManager.INSTANCE.loadUser(p.getUniqueId(), p.getName());
            if (user == null) {
                p.kick(Component.text("§cErreur lors de la connexion au serveur, veuillez réessayer plus tard !"));
                return;
            }
            IslandsManager.INSTANCE.checkForDataIntegrity(null, p.getUniqueId(), false);
            if (p.hasPermission("coreskyblock.mod")) {
                String bypass = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:bypass:" + p.getUniqueId());
                String spying = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:spy:" + p.getUniqueId());
                if (bypass != null) {
                    p.sendMessage(Component.text("§4§l[/!\\] BYPASS ÎLE Actif"));
                    IslandsManager.INSTANCE.setBypass(p.getUniqueId(), true);
                }
                if (spying != null) {
                    p.sendMessage(Component.text("§4§l[/!\\] SPY ÎLE Actif"));
                    IslandsManager.INSTANCE.setSpying(p.getUniqueId(), true);
                }
            }
        });
    }

    @EventHandler
    public void onPlayerPickupEvent(PlayerAttemptPickupItemEvent e) {
        if (SyncUsersManager.INSTANCE.inSync.contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
        if (!TradesManager.INSTANCE.isPlayerInATrade(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        e.quitMessage(null);

        CompletableFuture.runAsync(() -> {
            SyncUsersManager.INSTANCE.stopPlayerSyncInAsync(p);
            SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
            if (user != null) {
                SkyblockUsersManager.INSTANCE.updateUserSync(user);
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    SkyblockUsersManager.INSTANCE.getCachedUsers().remove(p.getUniqueId());
                    return null;
                });
            }
        });
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("CoreSkyblock.admin")) return;
        if (e.getTo().getWorld().getName().equals("world")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!CoreSkyblock.INSTANCE.isASpawn(e.getPlayer().getWorld())) return;
        if (CoreSkyblock.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setUseItemInHand(Event.Result.ALLOW);
        e.setUseInteractedBlock(Event.Result.ALLOW);
        if (e.getItem() != null) {
            if (!e.getItem().getType().isEdible()) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (SyncUsersManager.INSTANCE.inSync.contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (SyncUsersManager.INSTANCE.inSync.contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerAttemptPickupItem(PlayerAttemptPickupItemEvent e) {
        if (SyncUsersManager.INSTANCE.inSync.contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (SyncUsersManager.INSTANCE.inSync.contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }
}
