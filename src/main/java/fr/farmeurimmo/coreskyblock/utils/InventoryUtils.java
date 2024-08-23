package fr.farmeurimmo.coreskyblock.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

    public static InventoryUtils INSTANCE;

    public InventoryUtils() {
        INSTANCE = this;
    }

    public int hasPlaceWithStackCo(ItemStack item, Inventory inv, Player p) {

        int place = 0;
        for (ItemStack itemStack : inv.getContents()) {
            if (itemStack == null) {
                place += item.getMaxStackSize();
                continue;
            }
            if (itemStack.getType() == Material.AIR) {
                place += item.getMaxStackSize();
                continue;
            }

            if (item.isSimilar(itemStack)) {
                place += item.getMaxStackSize() - itemStack.getAmount();
            }
        }

        if (p != null) {
            if (p.getInventory().getHelmet() == null) {
                place -= 64;
            }
            if (p.getInventory().getChestplate() == null) {
                place -= 64;
            }
            if (p.getInventory().getLeggings() == null) {
                place -= 64;
            }
            if (p.getInventory().getBoots() == null) {
                place -= 64;
            }
            if (p.getInventory().getItemInOffHand().getType() == Material.AIR) {
                place -= 64;
            }
        }

        return place;

    }

    public int getAmountToFillInInv(ItemStack aa, Inventory player) {
        int total = 0;

        int size = player.getSize();
        for (int slot = 0; slot < size; slot++) {
            ItemStack is = player.getItem(slot);
            if (is == null) {
                total += 64;
                continue;
            } else if (is.getType() == aa.getType()) {
                total += 64 - is.getAmount();
                continue;
            }
        }

        return total;
    }

    public int freeSlots(Inventory inv) {
        int free = 0;
        for (ItemStack itemStack : inv.getStorageContents()) {
            if (itemStack == null) {
                free++;
            }
        }
        return free;
    }
}
