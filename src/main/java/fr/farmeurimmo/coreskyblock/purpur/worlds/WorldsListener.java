package fr.farmeurimmo.coreskyblock.purpur.worlds;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldsListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        World world = e.getWorld();

        if (IslandsManager.INSTANCE.isAnIsland(world)) {
            CoreSkyblock.INSTANCE.optimizeWorld(world, 2);
        } else if (world.getName().contains("pvp")) {
            CoreSkyblock.INSTANCE.optimizeWorld(world, 3);
        }
    }
}
