package fr.farmeurimmo.skylyblock.purpur.listeners;

import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SpawnProtectionListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!SkylyBlock.INSTANCE.isASpawn(e.getLocation().getWorld())) return;
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (!SkylyBlock.INSTANCE.isASpawn(e.getBlock().getWorld())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!SkylyBlock.INSTANCE.isASpawn(e.getLocation().getWorld())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!SkylyBlock.INSTANCE.isASpawn(e.getBlock().getWorld())) return;
        if (SkylyBlock.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!SkylyBlock.INSTANCE.isASpawn(e.getBlock().getWorld())) return;
        if (SkylyBlock.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
    }

}
