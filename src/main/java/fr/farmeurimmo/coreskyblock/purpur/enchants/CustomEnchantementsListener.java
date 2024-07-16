package fr.farmeurimmo.coreskyblock.purpur.enchants;

import dev.lone.itemsadder.api.CustomBlock;
import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.Enchantments;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

public class CustomEnchantementsListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;

        ItemStack itemStack = e.getPlayer().getInventory().getItemInMainHand();

        if (!itemStack.getType().isItem()) return;
        if (itemStack.getItemMeta() == null) return;

        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(itemStack);
        if (enchantments.isEmpty()) return;

        ArrayList<Pair<Enchantments, Integer>> enchantmentsList = enchantments.get();
        Player p = e.getPlayer();

        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(e.getBlock());
        ArrayList<ItemStack> drops = new ArrayList<>((customBlock != null ? customBlock.getLoot(
                e.getPlayer().getItemInHand(), false) : e.getBlock().getDrops(e.getPlayer().getItemInHand())));


        if (enchantmentsList.stream().anyMatch(pair -> pair.left() == Enchantments.SMELTING)) {
            ArrayList<ItemStack> newDrops = new ArrayList<>();
            for (ItemStack drop : drops) {
                if (!CustomEnchantmentsManager.SMELTING_ALLOWED_MATERIALS.contains(drop.getType())) {
                    newDrops.add(drop);
                    continue;
                }

                for (FurnaceRecipe furnaceRecipe : CustomEnchantmentsManager.SMELL_RECIPES) {
                    if (furnaceRecipe.getInputChoice().test(drop) && furnaceRecipe.getResult().getType() != Material.AIR) {
                        newDrops.add(furnaceRecipe.getResult());
                        break;
                    }
                }
            }

            drops.clear();
            drops.addAll(newDrops);
        }

        if (enchantmentsList.stream().anyMatch(pair -> pair.left() == Enchantments.AIMANT)) {
            e.setDropItems(false);

            CustomEnchantmentsManager.INSTANCE.applyAmant(drops, p);
        }
    }
}
