package fr.farmeurimmo.coreskyblock.purpur.items.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.items.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnchantsAdminInv extends FastInv {

    private int page = 0;

    public EnchantsAdminInv() {
        super(6 * 9, "§0Enchantements admin");

        update();
    }

    private void update() {
        LinkedHashMap<Enchantments, List<ItemStack>> enchantmentsListMap = CustomEnchantmentsManager.INSTANCE.getAllEnchantedBooks();

        for (int i = 0; i < 54; i++) {
            setItem(i, null);
        }

        //we start at slot 0 and stop at slot 44
        int slot = 0;
        int index = page * 45;
        int current = 0;

        for (Map.Entry<Enchantments, List<ItemStack>> entry : enchantmentsListMap.entrySet()) {
            for (ItemStack itemStack : entry.getValue()) {
                current++;

                if (current <= index) {
                    continue;
                }
                if (current > index + 45) {
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
            setItem(45, CommonItemStacks.getCommonPreviousPage(), e -> {
                page--;
                update();
            });
        }

        if (page < getMaxPage()) {
            setItem(53, CommonItemStacks.getCommonNextPage(), e -> {
                page++;
                update();
            });
        }

        setItem(45, new ItemBuilder(Material.BOOK).name("§6Acheter un livre enchanté").build(), e ->
                new EnchantsBuyerInv((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(46, new ItemBuilder(Material.DIRT).name("§6Recycler les livres enchantés").build(), e ->
                new EnchantsRecyclerInv((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(47, new ItemBuilder(Material.ANVIL).name("§6Fusionner et appliquer des enchants").build(), e ->
                new EnchantsJoinerInv((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(48, new ItemBuilder(Material.GRINDSTONE).name("§6Retirer les enchants").build(), e ->
                new EnchantsRemoverInv((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(49, new ItemBuilder(Material.BOOKSHELF).name("§6Enchantements disponibles").build(), e ->
                new EnchantsExplorerInv((Player) e.getWhoClicked(), null).open((Player) e.getWhoClicked()));
    }

    private int getMaxPage() {
        Map<Enchantments, List<ItemStack>> enchantmentsListMap = CustomEnchantmentsManager.INSTANCE.getAllEnchantedBooks();
        return (int) Math.ceil(enchantmentsListMap.values().stream().mapToInt(List::size).sum() / 45.0) - 1;
    }
}
