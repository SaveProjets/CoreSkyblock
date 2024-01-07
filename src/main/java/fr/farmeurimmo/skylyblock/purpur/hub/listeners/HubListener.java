package fr.farmeurimmo.skylyblock.purpur.hub.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class HubListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        e.setCancelled(true);
    }
}
