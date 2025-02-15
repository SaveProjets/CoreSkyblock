package fr.farmeurimmo.coreskyblock.purpur.items.enchants.invs;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class EnchantsMainInv extends FastInv {

    public EnchantsMainInv() {
        super(9 * 3, "§0Enchantements");

        setItem(10, new ItemBuilder(Material.BOOKSHELF).name("§6Enchantements disponibles").build(), e ->
                new EnchantsExplorerInv((Player) e.getWhoClicked(), null).open((Player) e.getWhoClicked()));

        setItem(12, new ItemBuilder(Material.BOOK).name("§6Acheter un livre enchanté").build(), e ->
                new EnchantsBuyerInv((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(13, new ItemBuilder(Material.DIRT).name("§6Recycler les livres enchantés").build(), e ->
                new EnchantsRecyclerInv((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(15, new ItemBuilder(Material.ANVIL).name("§6Fusionner et appliquer des enchants").build(), e ->
                new EnchantsJoinerInv((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(16, new ItemBuilder(Material.GRINDSTONE).name("§6Retirer les enchants").build(), e ->
                new EnchantsRemoverInv((Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(getInventory().getSize() - 1, new ItemBuilder(Material.IRON_DOOR).name("§cFermer").build(), e -> e.getWhoClicked().closeInventory());
    }
}
