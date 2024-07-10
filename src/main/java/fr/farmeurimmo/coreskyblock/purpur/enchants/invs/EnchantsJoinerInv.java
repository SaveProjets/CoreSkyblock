package fr.farmeurimmo.coreskyblock.purpur.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.Enchantments;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class EnchantsFusionnerInv extends FastInv {

    private static final int slot_1 = 12;
    private static final int slot_2 = 14;

    public EnchantsFusionnerInv(Player p) {
        super(36, "§0Fusionner des enchantements");

        p.setCanPickupItems(false);

        setItem(0, new ItemBuilder(Material.BARRIER).name("§cFermer").build(), e -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
        });

        setItem(22, new ItemBuilder(Material.ANVIL).name("§6Fusionner").build(), e -> {
            e.setCancelled(true);

            ItemStack item1 = getInventory().getItem(slot_1);
            ItemStack item2 = getInventory().getItem(slot_2);

            if (item1 == null) {
                e.getWhoClicked().sendMessage(Component.text("§cVeuillez mettre un item dans la première case"));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }
            if (item2 == null) {
                e.getWhoClicked().sendMessage(Component.text("§cVeuillez mettre un item dans la deuxième case"));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            if (item1.getType() != Material.ENCHANTED_BOOK) {
                e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez fusionner que des livres d'enchantements."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            if (!item1.equals(item2)) {
                e.getWhoClicked().sendMessage(Component.text("§cLes deux livres d'enchantements doivent être identiques."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments1 = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(item1);
            Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments2 = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(item2);

            if (enchantments1.isEmpty()) {
                e.getWhoClicked().sendMessage(Component.text("§cL'item dans la première case n'est pas un enchantement spécial."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }
            if (enchantments2.isEmpty()) {
                e.getWhoClicked().sendMessage(Component.text("§cL'item dans la deuxième case n'est pas un enchantement spécial."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            if (enchantments1.get().size() != 1) {
                e.getWhoClicked().sendMessage(Component.text("§cL'item dans la première case ne doit contenir qu'un seul enchantement."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }
            if (enchantments2.get().size() != 1) {
                e.getWhoClicked().sendMessage(Component.text("§cL'item dans la deuxième case ne doit contenir qu'un seul enchantement."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            Pair<Enchantments, Integer> enchantment1 = enchantments1.get().get(0);
            Pair<Enchantments, Integer> enchantment2 = enchantments2.get().get(0);

            if (!enchantment1.left().equals(enchantment2.left())) {
                e.getWhoClicked().sendMessage(Component.text("§cLes deux livres d'enchantements doivent contenir le même enchantement."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }
            if (!enchantment1.right().equals(enchantment2.right())) {
                e.getWhoClicked().sendMessage(Component.text("§cLes deux livres d'enchantements doivent contenir le même niveau d'enchantement."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            if (!enchantment1.left().hasMaxLevel()) {
                e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas fusionner un livre enchanté sans niveau."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            if (enchantment1.right() + 1 > enchantment1.left().getMaxLevel()) {
                e.getWhoClicked().sendMessage(Component.text("§cVous avez atteint le niveau maximum pour cet enchantement."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            ItemStack item = CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantment1.left(), enchantment1.right() + 1);

            getInventory().setItem(slot_1, null);
            getInventory().setItem(slot_2, null);
            e.getWhoClicked().setItemOnCursor(item);

            e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, Sound.Source.PLAYER, 1, 1));
            e.getWhoClicked().sendMessage(Component.text("§aVous avez fusionné deux livres enchantés."));
        });

        for (int i = 0; i < getInventory().getSize(); i++) {
            if (i == slot_1 || i == slot_2) {
                continue;
            }
            if (getInventory().getItem(i) == null)
                setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("§0").build(), e -> e.setCancelled(true));
        }

        setCloseFilter(e -> {
            if (getInventory().getItem(slot_1) != null) {
                if (e.getInventory().firstEmpty() != -1) {
                    e.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(slot_1)));
                    getInventory().setItem(slot_1, null);
                } else {
                    e.sendMessage(Component.text("§cVeuillez retirer l'item de la première case."));
                    e.playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                    return true;
                }
            }
            if (getInventory().getItem(slot_2) != null) {
                if (e.getInventory().firstEmpty() != -1) {
                    e.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(slot_2)));
                    getInventory().setItem(slot_2, null);
                } else {
                    e.sendMessage(Component.text("§cVeuillez retirer l'item de la deuxième case."));
                    e.playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                    return true;
                }
            }
            e.setCanPickupItems(true);
            return false;
        });
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(false);
    }
}
