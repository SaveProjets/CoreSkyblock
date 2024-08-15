package fr.farmeurimmo.coreskyblock.purpur.items.legendaryhoe;

import fr.farmeurimmo.coreskyblock.purpur.items.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

public class LegendaryHoeListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

        if (!LegendaryHoeManager.INSTANCE.isALegendaryHoe(item)) return;

        Player p = e.getPlayer();

        ArrayList<ItemStack> drops = new ArrayList<>(e.getBlock().getDrops(item));
        e.setDropItems(false);

        for (ItemStack drop : drops) {
            drop.setAmount(drop.getAmount() * 2);
        }

        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(item);
        if (enchantments.isPresent()) {

            ArrayList<Pair<Enchantments, Integer>> enchantmentsList = enchantments.get();
            for (Pair<Enchantments, Integer> enchantment : enchantmentsList) {
                if (enchantment.left().equals(Enchantments.AIMANT)) {
                    CustomEnchantmentsManager.INSTANCE.applyAimant(drops, p);
                    e.setDropItems(false);
                    return;
                }
            }
        }
        for (ItemStack drop : drops) {
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop);
        }
    }
}
