package fr.farmeurimmo.skylyblock.purpur.hub.listeners;

import fr.farmeurimmo.skylyblock.purpur.hub.HubManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class HubPlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        p.teleport(HubManager.SPAWN);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (HubManager.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (HubManager.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (HubManager.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
    }


}
