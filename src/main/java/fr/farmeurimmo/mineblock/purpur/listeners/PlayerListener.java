package fr.farmeurimmo.mineblock.purpur.listeners;

import fr.farmeurimmo.mineblock.common.skyblockusers.SkyblockUser;
import fr.farmeurimmo.mineblock.common.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.mineblock.purpur.MineBlock;
import fr.farmeurimmo.mineblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.mineblock.purpur.scoreboard.ScoreboardManager;
import fr.farmeurimmo.mineblock.purpur.trade.TradesManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.concurrent.CompletableFuture;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        SkyblockUsersManager.INSTANCE.loadUser(p.getUniqueId(), p.getName()).exceptionally(ex -> {
            p.kick(Component.text("§cErreur lors de la connexion au serveur, veuillez réessayer plus tard !"));
            return null;
        });

        ScoreboardManager.INSTANCE.addPlayer(p);

        e.joinMessage(null);

        p.teleportAsync(MineBlock.SPAWN);

        IslandsManager.INSTANCE.checkLoadedIsland(p);
    }

    @EventHandler
    public void onPlayerPickupEvent(PlayerAttemptPickupItemEvent e) {
        if (!TradesManager.INSTANCE.isPlayerInATrade(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        e.quitMessage(null);

        SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
        if (user != null) {
            CompletableFuture.runAsync(() -> SkyblockUsersManager.INSTANCE.updateUserSync(user)).thenRun(() -> {
                Bukkit.getScheduler().callSyncMethod(MineBlock.INSTANCE, () -> {
                    SkyblockUsersManager.INSTANCE.getCachedUsers().remove(p.getUniqueId());
                    return null;
                });
            });
        }

        IslandsManager.INSTANCE.checkUnloadIsland(p);
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("MineBlock.admin")) return;
        if (e.getTo().getWorld().getName().equals("world")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!MineBlock.INSTANCE.isASpawn(e.getPlayer().getWorld())) return;
        if (MineBlock.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
    }
}
