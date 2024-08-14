package fr.farmeurimmo.coreskyblock.purpur.islands.listeners;

import dev.lone.itemsadder.api.Events.*;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class IslandsProtectionItemsAdderListener implements Listener {

    @EventHandler
    public void onCustomBlockBreak(CustomBlockBreakEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.BREAK, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCustomFurnitureBreak(FurnitureBreakEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.BREAK, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCustomBlockPlace(CustomBlockPlaceEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.BUILD, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCustomFurniturePlace(FurniturePlaceEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.BUILD, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCustomBlockInteract(CustomBlockInteractEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.INTERACT, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCustomBlockInteract(FurnitureInteractEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.INTERACT, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }
}
