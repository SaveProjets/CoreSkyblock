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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

public class EnchantsApplierInv extends FastInv {

    private static final int slot_1 = 12;
    private static final int slot_2 = 14;

    public EnchantsApplierInv(Player whoClicked) {
        super(36, "§0Appliquer des enchantements");

        whoClicked.setCanPickupItems(false);

        setItem(0, new ItemBuilder(Material.BARRIER).name("§cFermer").build(), e -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
        });

        setItem(22, new ItemBuilder(Material.ANVIL).name("§6Appliquer").build(), e -> {
            e.setCancelled(true);

            final ItemStack item1 = getInventory().getItem(slot_1);
            final ItemStack item2 = getInventory().getItem(slot_2);

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

            int bookNum = (item1.getType() == Material.ENCHANTED_BOOK) ? 1 : (item2.getType() == Material.ENCHANTED_BOOK) ? 2 : 0;
            if (bookNum == 0) {
                e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez appliquer des enchantements qu'avec un livre d'enchantement."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;

            }
            ItemStack book = (bookNum == 1) ? item1 : item2;
            ItemStack tool = (bookNum == 1) ? item2 : item1;

            Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(book);
            if (enchantments.isEmpty()) {
                e.getWhoClicked().sendMessage(Component.text("§cCe livre d'enchantement n'est pas valide."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }
            Pair<Enchantments, Integer> enchantment = enchantments.get().get(0);

            if (!enchantment.left().canBeAppliedOn(tool.getType())) {
                e.getWhoClicked().sendMessage(Component.text("§cCet enchantement ne peut pas être appliqué sur cet item."));
                e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            Optional<ArrayList<Pair<Enchantments, Integer>>> toolEnchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(tool);
            if (toolEnchantments.isPresent()) {
                for (Pair<Enchantments, Integer> toolEnchantment : toolEnchantments.get()) {
                    if (toolEnchantment.left() == enchantment.left()) { //FIXME
                        e.getWhoClicked().sendMessage(Component.text("§cCet item possède déjà cet enchantement."));
                        e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                        return;
                    }
                }
            }
        });


    }
}
