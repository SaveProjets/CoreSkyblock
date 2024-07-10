package fr.farmeurimmo.coreskyblock.purpur.enchants;

import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.Enchantments;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
}
