package fr.farmeurimmo.mineblock.purpur.islands.listeners;

import fr.farmeurimmo.mineblock.purpur.islands.IslandsManager;
import org.bukkit.WorldBorder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class IslandsProtectionListener implements Listener {

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getBlock().getWorld())) return;
        WorldBorder border = e.getBlock().getWorld().getWorldBorder();
        if (e.getToBlock().getX() > border.getSize() / 2 || e.getToBlock().getZ() > border.getSize() / 2) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        WorldBorder border = e.getPlayer().getWorld().getWorldBorder();
        if (e.getTo().getX() > border.getSize() / 2 || e.getTo().getZ() > border.getSize() / 2) {
            e.setCancelled(true);
        }
    }
}
