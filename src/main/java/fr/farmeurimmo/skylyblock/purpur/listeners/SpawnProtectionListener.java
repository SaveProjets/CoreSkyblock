package fr.farmeurimmo.skylyblock.purpur.listeners;

import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SpawnProtectionListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!SkylyBlock.INSTANCE.isAHub(e.getLocation().getWorld())) return;
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (!SkylyBlock.INSTANCE.isAHub(e.getBlock().getWorld())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!SkylyBlock.INSTANCE.isAHub(e.getLocation().getWorld())) return;
        e.setCancelled(true);
    }

}
