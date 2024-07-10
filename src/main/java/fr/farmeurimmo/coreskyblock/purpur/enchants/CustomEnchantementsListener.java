package fr.farmeurimmo.coreskyblock.purpur.enchants;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.Enchantments;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

public class CustomEnchantementsListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        ItemStack itemStack = e.getPlayer().getInventory().getItemInMainHand();

        if (!itemStack.getType().isItem()) return;
        if (itemStack.getItemMeta() == null) return;

        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(itemStack);
        if (enchantments.isEmpty()) return;

        for (Pair<Enchantments, Integer> enchantment : enchantments.get()) {

        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) return;

        Player p = (Player) e.getWhoClicked();

        if (e.getCursor().getType().isItem() && e.getCurrentItem() != null && e.getCurrentItem().getType().isItem()) {
            ItemStack cursor = e.getCursor();
            ItemStack current = e.getCurrentItem();

            Optional<ArrayList<Pair<Enchantments, Integer>>> cursorEnchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(cursor);
            Optional<ArrayList<Pair<Enchantments, Integer>>> currentEnchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(current);

            if (cursor.getType() == Material.ENCHANTED_BOOK && current.getType() == Material.ENCHANTED_BOOK) {
                if (currentEnchantments.isEmpty()) return;
                if (cursorEnchantments.isEmpty()) return;

                ArrayList<Pair<Enchantments, Integer>> cursorEnchants = cursorEnchantments.get();
                ArrayList<Pair<Enchantments, Integer>> currentEnchants = currentEnchantments.get();

                for (Pair<Enchantments, Integer> cursorEnchant : cursorEnchants) {
                    for (Pair<Enchantments, Integer> currentEnchant : currentEnchants) {
                        if (cursorEnchant.left() == currentEnchant.left()) {
                            if (cursorEnchant.right().equals(currentEnchant.right())) {
                                if (!currentEnchant.left().hasMaxLevel()) {
                                    p.sendMessage(Component.text("§cVous ne pouvez pas combiner un livre enchanté sans niveau."));
                                    return;
                                }
                                if (!cursorEnchant.left().hasMaxLevel()) {
                                    p.sendMessage(Component.text("§cVous ne pouvez pas combiner un livre enchanté sans niveau."));
                                    return;
                                }
                                if (!cursorEnchant.equals(currentEnchant)) {
                                    p.sendMessage(Component.text("§cVous ne pouvez pas combiner deux livres avec des enchantements et des niveaux différents."));
                                    return;
                                }
                                if (cursorEnchant.right() + 1 > cursorEnchant.left().getMaxLevel()) {
                                    p.sendMessage(Component.text("§cVous avez atteint le niveau maximum pour cet enchantement."));
                                    return;
                                }

                                int newLevel = cursorEnchant.right() + 1;
                                ItemStack newBook = CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(cursorEnchant.left(), newLevel);

                                e.setCurrentItem(null);
                                e.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));

                                p.updateInventory();

                                Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () ->
                                        e.getWhoClicked().getInventory().setItem(e.getSlot(), newBook), 0);

                                p.sendMessage(Component.text("§aVous avez combiné deux livres enchantés."));
                            }
                        }
                    }
                }
            }
        }
    }
}
