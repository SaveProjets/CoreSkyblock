package fr.farmeurimmo.coreskyblock.purpur.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.Enchantments;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class EnchantsAdminInv extends FastInv {

    private int page = 0;

    public EnchantsAdminInv() {
        super(6 * 9, "§0Enchantements admin");

        update();
    }

    private void update() {
        Map<Enchantments, List<ItemStack>> enchantmentsListMap = CustomEnchantmentsManager.INSTANCE.getAllEnchantedBooks();

        for (int i = 9; i < 54; i++) {
            setItem(i, null);
        }

        //we start at slot 9 and stop at slot 44
        int slot = 9;
        int index = page * 36;
        int current = 0;

        for (Map.Entry<Enchantments, List<ItemStack>> entry : enchantmentsListMap.entrySet()) {
            for (ItemStack itemStack : entry.getValue()) {
                current++;

                if (current <= index) {
                    continue;
                }
                if (current > index + 36) {
                    break;
                }

                setItem(slot, itemStack, e -> {
                    Player player = (Player) e.getWhoClicked();
                    player.getInventory().addItem(itemStack);
                    player.sendMessage("§aVous avez reçu un livre enchanté.");
                });

                slot++;
            }
        }

        if (page > 0) {
            setItem(45, new ItemBuilder(Material.ARROW).name("§6Page précédente").build(), e -> {
                page--;
                update();
            });
        }

        if (page < getMaxPage()) {
            setItem(53, new ItemBuilder(Material.ARROW).name("§6Page suivante").build(), e -> {
                page++;
                update();
            });
        }

        setItem(47, new ItemBuilder(Material.ANVIL).name("§6Fusionner des enchants").build(), e ->
                new EnchantsFusionnerInv((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(49, new ItemBuilder(Material.ANVIL).name("§6Appliquer un enchantement").build(), e ->
                new EnchantsApplierInv((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));
    }

    private int getMaxPage() {
        Map<Enchantments, List<ItemStack>> enchantmentsListMap = CustomEnchantmentsManager.INSTANCE.getAllEnchantedBooks();
        return (int) Math.ceil(enchantmentsListMap.values().stream().mapToInt(List::size).sum() / 36.0) - 1;
    }
}
