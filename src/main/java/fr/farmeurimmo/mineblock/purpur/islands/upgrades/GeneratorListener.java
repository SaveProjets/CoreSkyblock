package fr.farmeurimmo.mineblock.purpur.islands.upgrades;

import fr.farmeurimmo.mineblock.common.islands.Island;
import fr.farmeurimmo.mineblock.purpur.islands.IslandsManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

public class GeneratorListener implements Listener {

    @EventHandler
    public void onGenerate(BlockFormEvent e) {
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getBlock().getLocation());
        ;
        if (island != null) {
            if (e.getNewState().getType() == Material.COBBLESTONE) {
                e.getNewState().setType(IslandsGeneratorManager.INSTANCE.getMaterialRandom(island.getGeneratorLevel()));
            }
        }
    }
}
