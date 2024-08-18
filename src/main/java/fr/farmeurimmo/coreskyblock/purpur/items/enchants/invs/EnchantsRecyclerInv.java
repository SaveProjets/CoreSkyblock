package fr.farmeurimmo.coreskyblock.purpur.items.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.EnchantmentRarity;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class EnchantsRecyclerInv extends FastInv {

    private static final Map<EnchantmentRarity, Integer> EXP_BY_RARITY = Map.of(
            EnchantmentRarity.UNCOMMON, 1_000,
            EnchantmentRarity.RARE, 2_000,
            EnchantmentRarity.EPIC, 4_000
    );
    private final Player p;
    private boolean isClosed = false;

    public EnchantsRecyclerInv(Player p) {
        super(5 * 9, "§0Recycler des enchantements");

        this.p = p;
        p.setCanPickupItems(false);

        for (int i = 36; i < getInventory().getSize(); i++) {
            setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§0").build(), e -> e.setCancelled(true));
        }

        setItem(getInventory().getSize()-1, new ItemBuilder(Material.IRON_DOOR).name("§6Retour §8| §7(clic gauche)").build(), e -> new EnchantsMainInv().open(p));

        setCloseFilter(e -> {
            for (int i = 0; i < 36; i++) {
                if (getInventory().getItem(i) != null) {
                    p.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(i)));
                }
            }

            p.setCanPickupItems(true);
            isClosed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, (task) -> {
            if (isClosed) {
                task.cancel();
                return;
            }
            update();
        }, 0, 15);
    }

    private void update() {
        setItem(40, new ItemBuilder(Material.GRINDSTONE).name("§6Recycler").lore(
                "§7Total: §e" + NumberFormat.getInstance().format(getTotalExp(false)) + "xp").build(), e -> {
            int totalExp = getTotalExp(true);

            if (totalExp == 0) {
                p.sendMessage(Component.text("§cVous n'avez pas d'enchantements à recycler."));
                p.playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1.0f, 1.0f));
                return;
            }

            p.giveExp(totalExp);

            p.playSound(Sound.sound(org.bukkit.Sound.BLOCK_GRINDSTONE_USE, Sound.Source.PLAYER, 1.0f, 1.0f));
            p.sendMessage(Component.text("§aVous avez recyclé des livres enchantés pour un total de §e" +
                    NumberFormat.getInstance().format(totalExp) + "xp§a."));

            e.setCancelled(true);
        });
    }

    public int getTotalExp(boolean delete) {
        int totalExp = 0;
        for (int i = 0; i < 36; i++) {
            if (getInventory().getItem(i) != null) {
                Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(getInventory().getItem(i));
                if (enchantments.isPresent()) {
                    for (Pair<Enchantments, Integer> enchantment : enchantments.get()) {
                        totalExp += EXP_BY_RARITY.get(enchantment.left().getRarity());
                        break;
                    }
                    if (delete) {
                        getInventory().setItem(i, null);
                    }
                }
            }
        }
        return totalExp;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(false);
        if (!isClosed) {
            update();
        }
    }
}
