package fr.farmeurimmo.coreskyblock.purpur.enchants;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import dev.lone.itemsadder.api.CustomBlock;
import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.Enchantments;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;
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

                        int fortuneLevel = 0;

                        try {
                            fortuneLevel = e.getPlayer().getItemInHand().getEnchantmentLevel(Objects.requireNonNull(Enchantment.LOOT_BONUS_BLOCKS));
                        } catch (NullPointerException ignored) {
                        }

                        ItemStack smeltResult = furnaceRecipe.getResult().clone();

                        if (fortuneLevel > 0) {
                            double fortune = 1.0 / (fortuneLevel + 2) + (fortuneLevel + 1) / 2.0;
                            smeltResult.setAmount((int) Math.round(furnaceRecipe.getResult().getAmount() * fortune));
                        }

                        e.setExpToDrop((int) Math.ceil(furnaceRecipe.getExperience()));

                        newDrops.add(smeltResult);
                        break;
                    }
                }
            }

            drops.clear();
            drops.addAll(newDrops);
        }

        if (enchantmentsList.stream().anyMatch(enchantmentsIntegerPair -> enchantmentsIntegerPair.left() == Enchantments.ORE_XP)) {
            e.setExpToDrop((int) (e.getExpToDrop() + e.getExpToDrop() * (Enchantments.ORE_XP.getBaseValue() / 100)));
        }

        if (enchantmentsList.stream().anyMatch(pair -> pair.left() == Enchantments.AIMANT)) {
            e.setDropItems(false);

            CustomEnchantmentsManager.INSTANCE.applyAmant(drops, p);
        }

        p.giveExp(e.getExpToDrop());
        e.setExpToDrop(0);
    }

    @EventHandler
    public void onPlayerArmorChange(PlayerArmorChangeEvent e) {
        CustomEnchantmentsManager.INSTANCE.checkForArmorEffects(e.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        CustomEnchantmentsManager.INSTANCE.checkForArmorEffects(e.getPlayer());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) return;

        if (e.getEntity().getKiller() == null) return;

        Player killer = e.getEntity().getKiller();

        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(killer.getInventory().getItemInMainHand());
        if (enchantments.isEmpty()) return;

        ArrayList<Pair<Enchantments, Integer>> enchantmentsList = enchantments.get();

        ArrayList<ItemStack> drops = new ArrayList<>(e.getDrops());

        if (enchantmentsList.stream().anyMatch(pair -> pair.left() == Enchantments.XP_TRANSFORMATEUR)) {
            e.getDrops().clear();

            e.setDroppedExp((int) (e.getDroppedExp() + e.getDroppedExp() * (Enchantments.XP_TRANSFORMATEUR.getBaseValue() / 100)));
        }

        if (enchantmentsList.stream().anyMatch(pair -> pair.left() == Enchantments.AIMANT)) {
            if (!drops.isEmpty()) {
                CustomEnchantmentsManager.INSTANCE.applyAmant(drops, killer);
            }
        }
    }
}
