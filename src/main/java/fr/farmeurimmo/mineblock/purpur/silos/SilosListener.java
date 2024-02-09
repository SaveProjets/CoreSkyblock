package fr.farmeurimmo.mineblock.purpur.silos;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class SilosListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        SilosType type = SilosType.getByMaterial(e.getBlock().getType());
        if (type == null) {
            type = SilosType.getByAlternativeMaterial(e.getBlock().getType());
            if (type == null) return;
        }
        int multiplier = 1;
        if (type.isHigh()) {
            int currentY = e.getBlock().getY() + 1;
            while (e.getBlock().getWorld().getBlockAt(e.getBlock().getX(), currentY, e.getBlock().getZ()).getType() == type.getMaterial()) {
                multiplier++;
                currentY++;
            }
        }
        ArrayList<ItemStack> drops = new ArrayList<>();
        for (ItemStack itemStack : e.getBlock().getDrops(p.getInventory().getItemInMainHand())) {
            SilosType silosType = SilosType.getByMaterial(itemStack.getType());
            boolean isAlternative = false;
            if (silosType == null) {
                silosType = SilosType.getByAlternativeMaterial(itemStack.getType());
                if (silosType == null) {
                    drops.add(itemStack);
                    continue;
                }
                isAlternative = true;
            }
            drops.add(itemStack);
            for (int i = 0; i < e.getPlayer().getInventory().getSize(); i++) {
                ItemStack is = p.getInventory().getItem(i);
                if (is == null) continue;
                int amount = SilosManager.INSTANCE.getAmount(is, silosType);
                if (amount == -1) continue;
                if (amount == -2) continue;
                int amountToAdd = itemStack.getAmount() * multiplier;
                if (isAlternative) {
                    amountToAdd = (int) Math.ceil(amountToAdd / 3.0);
                }
                int result = SilosManager.MAX_AMOUNT - (amount + amountToAdd);
                if (result < 0) {
                    SilosManager.INSTANCE.setAmount(is, silosType, SilosManager.MAX_AMOUNT);
                    if (itemStack.getAmount() > 0 && itemStack.getAmount() - result > 0) {
                        itemStack.setAmount(itemStack.getAmount() - result);
                    } else {
                        drops.remove(itemStack);
                    }
                    continue;
                }
                SilosManager.INSTANCE.setAmount(is, silosType, amount + amountToAdd);
                e.setDropItems(false);
                drops.remove(itemStack);
                break;
            }
        }
        for (ItemStack itemStack : drops) {
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), itemStack);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (SilosManager.INSTANCE.isASilo(e.getItemInHand())) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        ItemStack itemStack = e.getItem();
        if (!SilosManager.INSTANCE.isASilo(itemStack)) return;
        if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;
        SilosType silosType = SilosManager.INSTANCE.getSilosType(itemStack);
        if (silosType == null) return;
        e.setCancelled(true);
        new SilosInv(itemStack, silosType).open(e.getPlayer());
    }

    @EventHandler
    public void prepareItemCraft(PrepareItemCraftEvent e) {
        if (e.getRecipe() == null) return;
        if (e.getInventory().getMatrix()[0] == null) return;
        SilosType silosType = SilosType.getByMaterial(e.getInventory().getMatrix()[0].getType());
        if (silosType == null) return;
        if (!SilosManager.INSTANCE.isASilo(e.getInventory().getResult(), silosType)) return;
        int total = 0;
        for (ItemStack itemStack : e.getInventory().getMatrix()) {
            if (itemStack == null) continue;
            if (itemStack.getType() != silosType.getMaterial()) continue;
            total += itemStack.getAmount();
        }
        if (total < (48 * 8)) {
            e.getInventory().setResult(null);
            return;
        }
        for (int i = 0; i < e.getInventory().getMatrix().length; i++) {
            ItemStack itemStack = e.getInventory().getMatrix()[i];
            if (itemStack == null) continue;
            if (itemStack.getType() != silosType.getMaterial()) continue;
            if (itemStack.getAmount() < 48) {
                e.getInventory().setResult(null);
                return;
            }
        }
        e.getInventory().setResult(SilosManager.INSTANCE.createSilo(silosType));
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent e) {
        if (e.getInventory().getMatrix()[0] == null) return;
        SilosType silosType = SilosType.getByMaterial(e.getInventory().getMatrix()[0].getType());
        if (silosType == null) return;
        if (!SilosManager.INSTANCE.isASilo(e.getInventory().getResult(), silosType)) return;
        for (int i = 0; i < e.getInventory().getMatrix().length; i++) {
            ItemStack itemStack = e.getInventory().getMatrix()[i];
            if (itemStack == null) continue;
            if (itemStack.getType() != silosType.getMaterial()) continue;
            itemStack.setAmount(itemStack.getAmount() - 47);
            e.getInventory().setMatrix(e.getInventory().getMatrix());
        }
        e.getInventory().setResult(SilosManager.INSTANCE.createSilo(silosType));
    }
}
