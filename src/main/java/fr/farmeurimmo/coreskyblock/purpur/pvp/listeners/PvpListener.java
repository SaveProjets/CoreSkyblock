package fr.farmeurimmo.coreskyblock.purpur.pvp.listeners;

import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PvpListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
        if (user != null) {
            user.incrementPvpDeaths();
        }

        if (p.getKiller() != null) {
            SkyblockUser killer = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getKiller().getUniqueId());
            if (killer != null) {
                killer.incrementPvpKills();
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
    }
}
