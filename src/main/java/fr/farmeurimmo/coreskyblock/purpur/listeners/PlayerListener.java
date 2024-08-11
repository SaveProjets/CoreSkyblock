package fr.farmeurimmo.coreskyblock.purpur.listeners;

import fr.farmeurimmo.coreskyblock.ServerType;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.scoreboard.ScoreboardManager;
import fr.farmeurimmo.coreskyblock.purpur.sync.SyncUsersManager;
import fr.farmeurimmo.coreskyblock.purpur.tpa.TpasManager;
import fr.farmeurimmo.coreskyblock.purpur.trade.TradesManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.coreskyblock.utils.BossBarBackgroundUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Beacon;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        ScoreboardManager.INSTANCE.addPlayer(p);

        e.joinMessage(null);

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
            CoreSkyblock.INSTANCE.clockSendPlayerConnectedToRedis();
        });

        Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> {
            BossBar bossBar = BossBarBackgroundUtils.getBossBarWithText("OoO aaa eoo e e e|Salut " + p.getName());
            p.showBossBar(bossBar);
            /*BossBar bossBar2 = BossBarBackgroundUtils.getBossBarWithText("CoreSkyblock 1234 1334");
            p.showBossBar(bossBar2);
            BossBar bossBar3 = BossBarBackgroundUtils.getBossBarWithText("CoreSkyblock 1 1 1 11 1 11 1 11 1 11 11 1 1 11 1");
            p.showBossBar(bossBar3);*/
        }, 20L);
    }

    @EventHandler
    public void onPlayerSpawn(PlayerSpawnLocationEvent e) {
        Player p = e.getPlayer();
        Location tpaLocation = TpasManager.INSTANCE.onJoin(p);
        if (tpaLocation != null) {
            e.setSpawnLocation(tpaLocation);
            return;
        }
        if (CoreSkyblock.SERVER_TYPE == ServerType.SPAWN) {
            e.setSpawnLocation(CoreSkyblock.SPAWN);
            return;
        }
        if (CoreSkyblock.SERVER_TYPE == ServerType.GAME) {
            if (IslandsManager.INSTANCE.teleportToIsland.containsKey(p.getUniqueId())) {
                Island island = IslandsManager.INSTANCE.getIslandByUUID(IslandsManager.INSTANCE.teleportToIsland.get(p.getUniqueId()));
                if (island != null) {
                    e.setSpawnLocation(island.getSpawn());
                    p.sendMessage(Component.text("§aVous avez été téléporté sur votre île."));
                }
            }
        }
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent e) {
        e.message(null);
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
                SkyblockUsersManager.INSTANCE.upsertUser(user);
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    SkyblockUsersManager.INSTANCE.getCachedUsers().remove(p.getUniqueId());
                    return null;
                });
            }
        });

        Bukkit.getScheduler().runTaskLaterAsynchronously(CoreSkyblock.INSTANCE, () ->
                CoreSkyblock.INSTANCE.clockSendPlayerConnectedToRedis(), 20L);
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("coreskyblock.admin")) return;
        if (e.getTo().getWorld().getName().equals("world")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!CoreSkyblock.INSTANCE.isASpawn(e.getPlayer().getWorld())) return;
        if (CoreSkyblock.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setUseItemInHand(Event.Result.ALLOW);
        if (e.getClickedBlock() != null) {
            if (e.getClickedBlock().getState() instanceof Sign) {
                e.setUseInteractedBlock(Event.Result.DENY);
            }
            if (e.getClickedBlock().getState() instanceof Container) {
                e.setUseInteractedBlock(Event.Result.DENY);
            }
            if (e.getClickedBlock().getState() instanceof Beacon) {
                e.setUseInteractedBlock(Event.Result.DENY);
            }
            return;
        }
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
