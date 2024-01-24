package fr.farmeurimmo.skylyblock.purpur.listeners;

import fr.farmeurimmo.skylyblock.common.SkyblockUsersManager;
import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import fr.farmeurimmo.skylyblock.purpur.scoreboard.ScoreboardManager;
import fr.farmeurimmo.skylyblock.purpur.trade.TradesManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        SkyblockUsersManager.INSTANCE.checkForAccountOrCreate(p.getUniqueId(), p.getName());

        ScoreboardManager.INSTANCE.addPlayer(p);

        e.setJoinMessage(null);

        p.teleportAsync(SkylyBlock.SPAWN);

        /*JedisManager.INSTANCE.publishToRedis("skylyblock", "sync:inv:" + p.getUniqueId() + ":" +
                InventorySyncUtils.INSTANCE.toBase64(inv));*/
    }

    @EventHandler
    public void onPlayerPickupEvent(PlayerAttemptPickupItemEvent e) {
        if (!TradesManager.INSTANCE.isPlayerInATrade(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        e.setQuitMessage(null);
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("skylyblock.admin")) return;
        if (e.getTo().getWorld().getName().equals("world")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!SkylyBlock.INSTANCE.isASpawn(e.getPlayer().getWorld())) return;
        if (SkylyBlock.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
    }
}
