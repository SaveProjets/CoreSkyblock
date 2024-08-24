package fr.farmeurimmo.coreskyblock.purpur.items.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.EnchantmentRarity;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

import java.text.NumberFormat;
import java.util.*;

public class EnchantsRecyclerInv extends FastInv {

    private static final Map<EnchantmentRarity, Integer> EXP_BY_RARITY = Map.of(
            EnchantmentRarity.UNCOMMON, 1_000,
            EnchantmentRarity.RARE, 2_000,
            EnchantmentRarity.EPIC, 4_000
    );
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private final Player p;
    private boolean isClosed = false;

    public EnchantsRecyclerInv(Player p) {
        super(6 * 9, "§0Recycler des enchantements");

        this.p = p;
        p.setCanPickupItems(false);

        setItem(50, CommonItemStacks.getCommonBack(), e -> new EnchantsMainInv().open(p));

        CommonItemStacks.applyCommonPanes(Material.PURPLE_STAINED_GLASS_PANE, getInventory());

        ArrayList<Integer> slots = Arrays.stream(SLOTS).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        for (int i = 0; i < getInventory().getSize(); i++) {
            if (slots.contains(i)) continue;
            if (getInventory().getItem(i) == null) {
                setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§0").build());
            }
        }

        setCloseFilter(e -> {
            for (int slot : SLOTS) {
                if (getInventory().getItem(slot) != null) {
                    p.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(slot)));
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
        setItem(48, new ItemBuilder(Material.GRINDSTONE).name("§6Recycler").lore(
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
        for (int slot : SLOTS) {
            if (getInventory().getItem(slot) != null) {
                Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(getInventory().getItem(slot));
                if (enchantments.isPresent()) {
                    for (Pair<Enchantments, Integer> enchantment : enchantments.get()) {
                        totalExp += EXP_BY_RARITY.get(enchantment.left().getRarity());
                        break;
                    }
                    if (delete) {
                        getInventory().setItem(slot, null);
                    }
                }
            }
        }
        return totalExp;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        boolean shouldCancel = true;
        for (int slot : SLOTS) {
            if (e.getRawSlot() == slot) {
                shouldCancel = false;
                break;
            }
        }
        if (shouldCancel && !(e.getClickedInventory() instanceof PlayerInventory)) {
            e.setCancelled(true);
            return;
        }
        e.setCancelled(false);
        if (!isClosed) {
            update();
        }
    }
}
