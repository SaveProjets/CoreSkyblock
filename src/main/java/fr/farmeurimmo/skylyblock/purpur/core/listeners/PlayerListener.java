package fr.farmeurimmo.skylyblock.purpur.core.listeners;

import fr.farmeurimmo.skylyblock.common.SkyblockUserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        SkyblockUserManager.INSTANCE.checkForAccountOrCreate(p.getUniqueId(), p.getName());

        e.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        e.setQuitMessage(null);
    }
}
