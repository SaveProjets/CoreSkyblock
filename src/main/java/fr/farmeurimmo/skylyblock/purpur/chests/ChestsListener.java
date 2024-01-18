package fr.farmeurimmo.skylyblock.purpur.chests;

import fr.farmeurimmo.skylyblock.purpur.IslandsManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ChestsListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (item.getItemMeta() == null) return;
        if (!item.hasDisplayName()) return;
        if (!item.isUnbreakable()) return;
        if (ChestType.containsChestTypeInIt(item.getItemMeta().getDisplayName())) {
            e.setCancelled(true);

            Player p = e.getPlayer();

            if (!IslandsManager.INSTANCE.isAnIsland(p.getWorld())) {
                p.sendMessage("§b[SkylyBlock] §cVous ne pouvez placer ce coffre que sur votre île !");
                return;
            }

            p.sendMessage("§b[SkylyBlock] §cIndisponible pour le moment !");

            //FIXME: CHESTS
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockBreakEvent e) {
        Block b = e.getBlock();
    }

    @EventHandler
    public void onPistonPush(BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks()) {
            if (ChestType.getMaterials().contains(b.getType())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks()) {
            if (ChestType.getMaterials().contains(b.getType())) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
