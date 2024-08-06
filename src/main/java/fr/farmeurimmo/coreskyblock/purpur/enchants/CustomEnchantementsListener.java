package fr.farmeurimmo.coreskyblock.purpur.enchants;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import dev.lone.itemsadder.api.CustomBlock;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.Enchantments;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

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
        if (e.getEntity().getKiller() == null) return;

        Player killer = e.getEntity().getKiller();

        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(killer.getInventory().getItemInMainHand());
        if (enchantments.isEmpty()) return;

        ArrayList<Pair<Enchantments, Integer>> enchantmentsList = enchantments.get();

        ArrayList<ItemStack> drops = new ArrayList<>(e.getDrops());

        if (e.getEntity() instanceof Player p) {
            if (enchantmentsList.stream().anyMatch(pair -> pair.left() == Enchantments.DECAPITEUR)) {
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                skullMeta.setOwningPlayer(p);
            }
            return;
        }

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

    @EventHandler
    public void onEntityShoot(EntityShootBowEvent e) {
        ItemStack itemStack = e.getBow();

        if (itemStack == null) return;
        if (!itemStack.getType().isItem()) return;
        if (itemStack.getItemMeta() == null) return;

        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(itemStack);

        if (enchantments.isEmpty()) return;

        ArrayList<Pair<Enchantments, Integer>> enchantmentsList = enchantments.get();

        for (Pair<Enchantments, Integer> enchantmentsIntegerPair : enchantmentsList) {
            Arrow arrow = (Arrow) e.getProjectile();

            if (enchantmentsIntegerPair.left().equals(Enchantments.SNIPER)) {
                if (enchantmentsIntegerPair.left().getValueForLevel(enchantmentsIntegerPair.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    arrow.setDamage(arrow.getDamage() * 2);
                }
                continue;
            }
            if (enchantmentsIntegerPair.left().equals(Enchantments.FLECHE_GELEE)) {
                if (enchantmentsIntegerPair.left().getValueForLevel(enchantmentsIntegerPair.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    arrow.setFreezeTicks((int) enchantmentsIntegerPair.left().getValueEffectForLevel(enchantmentsIntegerPair.right()) * 20);
                }
                continue;
            }
            if (enchantmentsIntegerPair.left().equals(Enchantments.FLECHE_TONNERRE)) {
                if (enchantmentsIntegerPair.left().getValueForLevel(enchantmentsIntegerPair.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    arrow.setMetadata("lightning", new FixedMetadataValue(CoreSkyblock.INSTANCE, true));
                }
                continue;
            }
            if (enchantmentsIntegerPair.left().equals(Enchantments.TIR_EXPLOSIF)) {
                if (enchantmentsIntegerPair.left().getValueForLevel(enchantmentsIntegerPair.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    arrow.setMetadata("explosive", new FixedMetadataValue(CoreSkyblock.INSTANCE, true));
                    arrow.setDamage(arrow.getDamage() * 1.5);
                }
                continue;
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        if (e.getDamager() instanceof Arrow arrow) {
            if (arrow.hasMetadata("lightning")) {
                p.getWorld().strikeLightningEffect(p.getLocation());
                p.damage(2);
                for (ItemStack itemStack : p.getInventory().getArmorContents()) {
                    if (itemStack == null) continue;
                    itemStack.damage(2);
                }
                return;
            }
            if (arrow.hasMetadata("explosive")) {
                p.getWorld().createExplosion(p.getLocation(), 0);
                return;
            }

            return;
        }

        if (!(e.getDamager() instanceof Player damager)) return;

        ItemStack itemStack = p.getInventory().getItemInMainHand();

        if (!itemStack.getType().isItem()) return;
        if (itemStack.getItemMeta() == null) return;

        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(itemStack);

        if (enchantments.isEmpty()) return;

        ArrayList<Pair<Enchantments, Integer>> enchantmentsList = enchantments.get();

        for (Pair<Enchantments, Integer> enchantment : enchantmentsList) {
            if (enchantment.left() == Enchantments.RENVOI) {
                if (enchantment.left().getValueForLevel(enchantment.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    damager.damage(e.getDamage(), p);
                    e.setCancelled(true);
                }
            }
        }
    }
}
