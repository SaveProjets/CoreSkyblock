package fr.farmeurimmo.coreskyblock.purpur.chests;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;
import fr.farmeurimmo.coreskyblock.utils.InventoryUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;

        ItemStack item = e.getItemInHand();
        if (item.getItemMeta() == null) return;
        if (!item.hasDisplayName()) return;
        if (!item.isUnbreakable()) return;
        if (ChestType.containsChestTypeInIt(item.getItemMeta().getDisplayName())) {
            e.setCancelled(true);

            Player p = e.getPlayer();

            if (!IslandsManager.INSTANCE.isAnIsland(p.getWorld())) {
                p.sendMessage(Component.text("§cVous ne pouvez placer ce coffre que sur votre île !"));
                return;
            }

            Island island = IslandsManager.INSTANCE.getIslandByLoc(p.getWorld());

            ChestType type = ChestType.getByName(item.getItemMeta().getDisplayName());
            if (type == null) return;
            int tier = 0;
            if (type == ChestType.SELL_CHEST) {
                tier = ChestsManager.INSTANCE.getTierFromName(item.getItemMeta().getDisplayName());
            }

            island.addChest(new Chest(UUID.randomUUID(), island.getIslandUUID(), type, e.getBlock().getLocation(),
                    null, 0, false, false, tier));
            p.sendMessage(Component.text("§aVous avez placé un " + (type == ChestType.SELL_CHEST ?
                    ChestsManager.INSTANCE.getNameFromTier(tier) : type.getName()) + "§a sur votre île !"));

            e.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDestroy(BlockBreakEvent e) {
        Block b = e.getBlock();

        if (e.isCancelled()) return;
        if (!ChestType.getMaterials().contains(b.getType())) return;
        if (!IslandsManager.INSTANCE.isAnIsland(b.getWorld())) return;

        Island island = IslandsManager.INSTANCE.getIslandByLoc(b.getWorld());
        if (island == null) return;

        Player p = e.getPlayer();
        IslandRanks rank = island.getPlayerRank(p.getUniqueId());

        Chest toRemove = null;
        for (Chest chest : island.getChests()) {
            if (chest.getBlock().equals(b.getLocation())) {
                if (!island.hasPerms(rank, IslandPerms.SECURED_CHEST, p.getUniqueId())) {
                    e.setCancelled(true);
                    return;
                }
                e.setCancelled(false);
                b.setType(Material.AIR);
                e.setDropItems(false);
                ItemStack item = ChestsManager.INSTANCE.getItemStack(chest.getType(), chest.getTier());

                if (InventoryUtils.INSTANCE.hasPlaceWithStackCo(item,
                        p.getInventory(), p) <= 0) b.getWorld().dropItemNaturally(b.getLocation(), item);
                else p.getInventory().addItem(item);

                p.sendMessage(Component.text("§cVous avez cassé un " + chest.getType().getName() + "§c sur votre île !"));
                toRemove = chest;
                break;
            }
        }
        if (toRemove != null) island.removeChest(toRemove);
    }

    @EventHandler
    public void onPistonPush(BlockPistonExtendEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getBlock().getWorld())) return;
        for (Block b : e.getBlocks()) {
            if (ChestType.getMaterials().contains(b.getType())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getBlock().getWorld())) return;
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
            if (chest.getType() != ChestType.CYBER_HOPPER) continue;
            if (chest.getBlock().getChunk().getChunkKey() != e.getLocation().getChunk().getChunkKey()) continue;
            Block b = chest.getBlock().getBlock();
            if (b.getType() != ChestType.CYBER_HOPPER.getMaterial()) continue;
            Hopper hopper = (Hopper) b.getState();
            if (hopper.customName() == null) continue;
            if (Objects.requireNonNull(hopper.customName()).contains(Component.text(ChestType.CYBER_HOPPER.getNameWithoutColor())))
                continue;
            int amount = InventoryUtils.INSTANCE.getAmountToFillInInv(e.getEntity().getItemStack(), hopper.getInventory());
            if (amount == 0) continue;
            if (amount >= e.getEntity().getItemStack().getAmount()) {
                e.getEntity().remove();
                hopper.getInventory().addItem(e.getEntity().getItemStack());
                break;
            } else {
                ItemStack item = e.getEntity().getItemStack().clone();
                item.setAmount(amount);
                e.getEntity().getItemStack().setAmount(e.getEntity().getItemStack().getAmount() - amount);
                hopper.getInventory().addItem(item);
            }
        }
    }
}
