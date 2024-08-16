package fr.farmeurimmo.coreskyblock.purpur.items.legendaryhoe;

import fr.farmeurimmo.coreskyblock.purpur.items.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class LegendaryHoeListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

        if (!LegendaryHoeManager.INSTANCE.isALegendaryHoe(item)) return;

        Player p = e.getPlayer();
        e.setCancelled(true);

        ArrayList<ItemStack> drops = new ArrayList<>();
        int startY = e.getBlock().getY();
        int maxY = (e.getBlock().getType() == Material.SUGAR_CANE || e.getBlock().getType() == Material.CACTUS || e.getBlock().getType() == Material.BAMBOO)
                ? e.getBlock().getWorld().getMaxHeight()
                : startY;

        for (int y = startY; y <= maxY; y++) {
            Block block = e.getBlock().getWorld().getBlockAt(e.getBlock().getX(), y, e.getBlock().getZ());
            if (block.getType() != e.getBlock().getType()) break;
            drops.addAll(block.getDrops(item));
        }

        for (int y = maxY; y >= startY; y--) {
            e.getBlock().getWorld().getBlockAt(e.getBlock().getX(), y, e.getBlock().getZ()).setType(Material.AIR);
        }

        drops.forEach(drop -> {
            if (LegendaryHoeManager.CROPS.contains(drop.getType())) drop.setAmount(drop.getAmount() * 2);
        });

        boolean aimantApplied = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(item)
                .flatMap(enchantmentsList -> enchantmentsList.stream()
                        .filter(enchantment -> enchantment.left().equals(Enchantments.AIMANT))
                        .findFirst())
                .map(enchantment -> {
                    CustomEnchantmentsManager.INSTANCE.applyAimant(drops, p);
                    return true;
                })
                .orElse(false);

        if (!aimantApplied) {
            drops.forEach(drop -> e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop));
        }
    }
}
