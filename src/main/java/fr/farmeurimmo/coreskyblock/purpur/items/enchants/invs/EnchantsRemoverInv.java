package fr.farmeurimmo.coreskyblock.purpur.items.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.farmeurimmo.coreskyblock.utils.InventoryUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class EnchantsRemoverInv extends FastInv {

    private static final int ITEM_SLOT = 12;
    private static final int[] ENCHANTS_SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
    private final Player p;
    private boolean isClosed = false;

    public EnchantsRemoverInv(Player p) {
        super(6 * 9, "§8Enlever les enchantements");

        this.p = p;

        p.setCanPickupItems(false);

        setItem(ITEM_SLOT, null);
        setItem(14, new ItemBuilder(Material.GRINDSTONE).name("§cEnlever les enchantements")
                .lore("§7Coût: §625k expérience").build(), e -> {
            e.setCancelled(true);

            ItemStack item = getInventory().getItem(ITEM_SLOT);

            if (item == null || item.getType() == Material.AIR) {
                p.sendMessage(Component.text("§cVous devez placer un item dans la case."));
                return;
            }

            if (p.calculateTotalExperiencePoints() < 25_000) {
                p.sendMessage(Component.text("§cVous n'avez pas assez d'expérience."));
                return;
            }

            Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(item);

            if (enchantments.isEmpty()) {
                p.sendMessage(Component.text("§cCet item n'a pas d'enchantements spéciaux."));
                return;
            }

            // Taille de la liste des enchants + de l'item enchanté
            if (InventoryUtils.INSTANCE.freeSlots(p.getInventory()) < enchantments.get().size() + 1) {
                p.sendMessage(Component.text("§cVous n'avez pas assez de place dans votre inventaire."));
                return;
            }

            getInventory().setItem(ITEM_SLOT, null);

            p.setExperienceLevelAndProgress(p.calculateTotalExperiencePoints() - 25_000);

            for (Pair<Enchantments, Integer> enchantment : enchantments.get()) {
                p.getInventory().addItem(CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantment.left(), enchantment.right()));
            }
            enchantments.get().clear();
            p.getInventory().addItem(CustomEnchantmentsManager.INSTANCE.getItemStackWithEnchantsApplied(enchantments.get(), item));

            p.sendMessage(Component.text("§aLes enchantements ont été retirés de l'item."));
            p.playSound(p.getLocation(), "block.grindstone.use", 1, 1);

            update();
        });

        update();

        setCloseFilter(event -> {
            if (getInventory().getItem(ITEM_SLOT) != null)
                p.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(ITEM_SLOT)));

            p.setCanPickupItems(true);
            if (!isClosed) {
                isClosed = true;
            }
            return false;
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
            if (!isClosed) update();
            else task.cancel();
        }, 0, 20L);
    }

    private void update() {
        for (int i = 0; i < getInventory().getSize(); i++) {
            if (i == ITEM_SLOT || i == 14) continue;
            if (getInventory().getItem(i) == null)
                setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§7").build(), e -> e.setCancelled(true));
        }

        setItem(49, CommonItemStacks.getCommonBack(), e -> new EnchantsMainInv().open(p));

        CommonItemStacks.applyCommonPanes(Material.PURPLE_STAINED_GLASS_PANE, getInventory());

        ItemStack item = getInventory().getItem(ITEM_SLOT);

        if (item == null || item.getType() == Material.AIR) {
            for (int slot : ENCHANTS_SLOTS) {
                setItem(slot, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§7").build(), e -> e.setCancelled(true));
            }
            return;
        }

        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(item);

        if (enchantments.isEmpty()) {
            for (int slot : ENCHANTS_SLOTS) {
                setItem(slot, new ItemBuilder(Material.BARRIER).name("§cAucun enchantement spécial").build());
            }
            return;
        }

        for (int i = 0; i < enchantments.get().size(); i++) {
            Pair<Enchantments, Integer> enchantment = enchantments.get().get(i);
            setItem(ENCHANTS_SLOTS[i], CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantment.left(),
                    enchantment.right()), e -> e.setCancelled(true));
        }

    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(false);
    }
}
