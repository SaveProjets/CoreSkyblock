package fr.farmeurimmo.coreskyblock.purpur.chests;

import fr.farmeurimmo.coreskyblock.common.islands.Island;
import fr.farmeurimmo.coreskyblock.common.islands.IslandPerms;
import fr.farmeurimmo.coreskyblock.common.islands.IslandRanks;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.utils.InventoryUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;

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
                p.sendMessage(Component.text("§b§cVous ne pouvez placer ce coffre que sur votre île !"));
                return;
            }

            Island island = IslandsManager.INSTANCE.getIslandByLoc(p.getWorld());

            p.sendMessage(Component.text("§baaaa"));
            ChestType type = ChestType.getByName(item.getItemMeta().getDisplayName());
            if (type == null) return;
            p.sendMessage(Component.text("§baaaaaaaaa"));

            island.addChest(new Chest(UUID.randomUUID(), island.getIslandUUID(), type, e.getBlock().getLocation(),
                    null, 0, false, false, 0));
            if (type == ChestType.CROP_HOPPER) {
                p.sendMessage(Component.text("§b§aVous avez placé un CropHopper sur votre île !"));
            } else if (type == ChestType.SELL_CHEST) {
                p.sendMessage(Component.text("§b§aVous avez placé un SellChest sur votre île !"));
            } else if (type == ChestType.PLAYER_SHOP) {
                p.sendMessage(Component.text("§b§aVous avez placé un PlayerShop sur votre île !"));
            } else if (type == ChestType.BLOCK_STOCKER) {
                p.sendMessage(Component.text("§b§aVous avez placé un BlockStocker sur votre île !"));
            }

            e.setCancelled(false);
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockBreakEvent e) {
        Block b = e.getBlock();

        if (!IslandsManager.INSTANCE.isAnIsland(b.getWorld())) return;

        Island island = IslandsManager.INSTANCE.getIslandByLoc(b.getWorld());
        if (island == null) return;

        Player p = e.getPlayer();
        IslandRanks rank = island.getMembers().get(p.getUniqueId());
        if (rank == null) {
            e.setCancelled(true);
            p.sendMessage(Component.text("§b§cVous n'avez pas la permission de casser ce coffre !"));
            return;
        }

        if (!island.hasPerms(rank, IslandPerms.SECURED_CHEST, p.getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage(Component.text("§b§cVous n'avez pas la permission de casser ce coffre !"));
            return;
        }

        for (Chest chest : island.getChests()) {
            if (chest.getBlock().equals(b.getLocation())) {
                e.setCancelled(true);
                p.sendMessage(Component.text("§b§cVous ne pouvez pas casser ce coffre !"));
                return;
            }
        }
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

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getLocation().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getLocation().getWorld());
        if (island == null) return;
        for (Chest chest : island.getChests()) {
            if (chest.getType() != ChestType.CROP_HOPPER) continue;
            if (chest.getBlock().getChunk().getChunkKey() != e.getLocation().getChunk().getChunkKey()) continue;
            Block b = chest.getBlock().getBlock();
            if (b.getType() != ChestType.CROP_HOPPER.getMaterial()) continue;
            Hopper hopper = (Hopper) b.getState();
            if (hopper.customName() == null) continue;
            if (!Objects.requireNonNull(hopper.customName()).contains(Component.text(ChestType.CROP_HOPPER.getNameWithoutColor())))
                continue;
            int amount = InventoryUtils.INSTANCE.getAmountToFillInInv(e.getEntity().getItemStack(), hopper.getInventory());
            if (amount == 0) continue;
            if (amount >= e.getEntity().getItemStack().getAmount()) {
                e.getEntity().remove();
                hopper.getInventory().addItem(e.getEntity().getItemStack());
                break;
            } else {
                ItemStack item = e.getEntity().getItemStack();
                item.setAmount(amount);
                e.getEntity().getItemStack().setAmount(e.getEntity().getItemStack().getAmount() - amount);
                hopper.getInventory().addItem(item);
            }
        }
    }
}
