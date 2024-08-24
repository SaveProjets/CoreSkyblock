package fr.farmeurimmo.coreskyblock.purpur.islands.listeners;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsGeneratorManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

public class GeneratorListener implements Listener {

    @EventHandler
    public void onGenerate(BlockFormEvent e) {
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getBlock().getWorld());
        if (island != null) {
            if (e.getNewState().getType() == Material.COBBLESTONE) {
                e.getNewState().setType(IslandsGeneratorManager.INSTANCE.getMaterialRandom(island.getGeneratorLevel()));
            }
        }
    }
}
