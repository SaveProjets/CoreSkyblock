package fr.farmeurimmo.coreskyblock.purpur.items.enchants;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import dev.lone.itemsadder.api.CustomBlock;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
                            fortuneLevel = e.getPlayer().getItemInHand().getEnchantmentLevel(Objects.requireNonNull(Enchantment.FORTUNE));
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

            CustomEnchantmentsManager.INSTANCE.applyAimant(drops, p);
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
            Pair<Enchantments, Integer> enchantmentsIntegerPair = enchantmentsList.stream().filter(pair -> pair.left() == Enchantments.DECAPITEUR).findFirst().orElse(null);
            if (enchantmentsIntegerPair != null) {
                if (enchantmentsIntegerPair.left().getValueForLevel(enchantmentsIntegerPair.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                    skullMeta.setOwningPlayer(p);
                    skull.setItemMeta(skullMeta);

                    e.getDrops().add(skull);
                }
            }
            return;
        }

        if (enchantmentsList.stream().anyMatch(pair -> pair.left() == Enchantments.XP_TRANSFORMATEUR)) {
            e.getDrops().clear();

            e.setDroppedExp((int) (e.getDroppedExp() + e.getDroppedExp() * (Enchantments.XP_TRANSFORMATEUR.getBaseValue() / 100)));
        }

        if (enchantmentsList.stream().anyMatch(pair -> pair.left() == Enchantments.AIMANT)) {
            if (!drops.isEmpty()) {
                CustomEnchantmentsManager.INSTANCE.applyAimant(drops, killer);
            }
        }
    }

    @EventHandler
    public void onEntityShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

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
                    arrow.setMetadata("frozen", new FixedMetadataValue(CoreSkyblock.INSTANCE,
                            enchantmentsIntegerPair.left().getValueEffectForLevel(enchantmentsIntegerPair.right())));
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
            if (enchantmentsIntegerPair.left().equals(Enchantments.PLUIE_DE_FLECHES)) {
                if (enchantmentsIntegerPair.left().getValueForLevel(enchantmentsIntegerPair.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    arrow.setMetadata("rain", new FixedMetadataValue(CoreSkyblock.INSTANCE, p.getName()));
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
                    itemStack.damage(2, p);
                }
            }
            if (arrow.hasMetadata("explosive")) {
                p.getWorld().createExplosion(p.getLocation(), 0);
            }
            if (arrow.hasMetadata("frozen")) {
                p.lockFreezeTicks(true);
                p.setFreezeTicks(arrow.getMetadata("frozen").get(0).asInt() * 20);
                Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> p.lockFreezeTicks(false), p.getFreezeTicks());
            }
            if (arrow.hasMetadata("rain")) {
                try {
                    Player shooter = Bukkit.getPlayer(arrow.getMetadata("rain").get(0).asString());
                    if (shooter == null) return;

                    //use pythagorean theorem to make them spawn in a circle around the player
                    double angle = 0;
                    double yAdd = 3;
                    double radius = 4;

                    for (int i = 0; i < 4; i++) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Arrow arrow1 = p.getWorld().spawn(p.getLocation().add(x, yAdd, z), Arrow.class);
                        arrow1.setShooter(shooter);
                        arrow1.setGravity(false);

                        Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> {
                            arrow1.setGravity(true);
                            arrow1.setVelocity(p.getLocation().add(0, 1, 0).toVector().subtract(arrow1.getLocation().toVector()).normalize().multiply(2));
                            arrow1.setDamage(arrow.getDamage());
                        }, 6);

                        angle += Math.PI / 2;
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }

            return;
        }

        if (!(e.getDamager() instanceof Player damager)) return;

        ItemStack itemStack = damager.getInventory().getItemInMainHand();

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
            if (enchantment.left().equals(Enchantments.INTIMIDATION)) {
                if (enchantment.left().getValueForLevel(enchantment.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 *
                            enchantment.left().getValueEffectForLevel(enchantment.right()), 0));
                }
            }
            if (enchantment.left().equals(Enchantments.TEMPETE_DE_FOUDRE)) {
                if (enchantment.left().getValueForLevel(enchantment.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    p.getWorld().strikeLightningEffect(p.getLocation());
                    p.damage(2);
                    for (ItemStack itemStack1 : p.getInventory().getArmorContents()) {
                        if (itemStack1 == null) continue;
                        itemStack1.damage(3, p);
                    }
                }
            }
            if (enchantment.left().equals(Enchantments.GELURE)) {
                if (enchantment.left().getValueForLevel(enchantment.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    p.lockFreezeTicks(true);
                    p.setFreezeTicks(20 * enchantment.left().getValueEffectForLevel(enchantment.right()));
                    Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> p.lockFreezeTicks(false), p.getFreezeTicks());
                }
            }
            if (enchantment.left().equals(Enchantments.GEYSER)) {
                if (enchantment.left().getValueForLevel(enchantment.right()) > CustomEnchantmentsManager.INSTANCE.getRng()) {
                    p.setVelocity(p.getVelocity().setX(0).setZ(0).setY(3.5));
                    for (int i = 0; i < 10; i++) {
                        Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> {
                            p.getWorld().playSound(p.getLocation(), "block.water.ambient", 1, 1);
                            p.getWorld().spawnParticle(Particle.DRIPPING_WATER, p.getLocation(), 3);
                        }, i);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        if (!(e.getRightClicked() instanceof Player damaged)) return;

        ItemStack itemStack = p.getInventory().getItemInMainHand();

        if (!itemStack.getType().isItem()) return;

        if (itemStack.getItemMeta() == null) return;

        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(itemStack);

        if (enchantments.isEmpty()) return;

        ArrayList<Pair<Enchantments, Integer>> enchantmentsList = enchantments.get();

        for (Pair<Enchantments, Integer> enchantment : enchantmentsList) {
            if (enchantment.left().equals(Enchantments.SEISME)) {
                if (!CustomEnchantmentsManager.INSTANCE.isInCooldown(p.getUniqueId(), enchantment.left().name())) {
                    CustomEnchantmentsManager.INSTANCE.addAbilityCooldown(p.getUniqueId(), enchantment.left().name(), enchantment.left().getCooldown(enchantment.right()));

                    Vector direction = damaged.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
                    direction.setY(0.8);

                    damaged.setVelocity(direction);


                    p.sendMessage(Component.text("§aVous avez utilisé la compétence " + enchantment.left().name() + "."));
                } else {
                    p.sendMessage(Component.text("§cVous devez attendre §l" + CustomEnchantmentsManager.INSTANCE.getCooldownLeft(p.getUniqueId(), enchantment.left().name()) + "§c secondes avant de réutiliser cette compétence."));
                }
            }
        }
    }
}
