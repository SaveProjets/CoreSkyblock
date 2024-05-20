package fr.farmeurimmo.coreskyblock.purpur.shop.invs;

import fr.farmeurimmo.coreskyblock.purpur.shop.ShopType;
import fr.farmeurimmo.coreskyblock.purpur.shop.ShopsManager;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopInv extends FastInv {

    public ShopInv() {
        super(54, "§8Boutique");

        setItems(new int[]{9, 0, 1, 7, 8, 17, 52, 53, 44, 36, 45, 46}, ItemBuilder.copyOf(new ItemStack(
                Material.LIME_STAINED_GLASS_PANE)).name("§6").build());

        setItem(20, ItemBuilder.copyOf(new ItemStack(Material.GRASS_BLOCK)).name("§6Blocs").build(), e ->
                new ShopPageInv(ShopsManager.INSTANCE.getPage(ShopType.BLOCKS), 1).open((Player) e.getWhoClicked()));

        setItem(23, ItemBuilder.copyOf(new ItemStack(Material.LIME_DYE)).name("§6Colorants").build(), e ->
                new ShopPageInv(ShopsManager.INSTANCE.getPage(ShopType.DYES), 1).open((Player) e.getWhoClicked()));

        setItem(21, ItemBuilder.copyOf(new ItemStack(Material.WHEAT)).name("§6Agriculture").build(), e ->
                new ShopPageInv(ShopsManager.INSTANCE.getPage(ShopType.AGRICULTURE), 1).open((Player) e.getWhoClicked()));

        setItem(22, ItemBuilder.copyOf(new ItemStack(Material.COOKED_BEEF)).name("§6Nourritures").build(), e ->
                new ShopPageInv(ShopsManager.INSTANCE.getPage(ShopType.FOOD), 1).open((Player) e.getWhoClicked()));

        setItem(24, ItemBuilder.copyOf(new ItemStack(Material.DIAMOND_ORE)).name("§6Minerais").build(), e ->
                new ShopPageInv(ShopsManager.INSTANCE.getPage(ShopType.ORES), 1).open((Player) e.getWhoClicked()));

        setItem(31, ItemBuilder.copyOf(new ItemStack(Material.BELL)).name("§6Autres").build(), e ->
                new ShopPageInv(ShopsManager.INSTANCE.getPage(ShopType.OTHERS), 1).open((Player) e.getWhoClicked()));

        setItem(32, ItemBuilder.copyOf(new ItemStack(Material.GUNPOWDER)).name("§6Loots de mobs").build(), e ->
                new ShopPageInv(ShopsManager.INSTANCE.getPage(ShopType.MOBS), 1).open((Player) e.getWhoClicked()));

        setItem(30, ItemBuilder.copyOf(new ItemStack(Material.REDSTONE)).name("§6Redstone").build(), e ->
                new ShopPageInv(ShopsManager.INSTANCE.getPage(ShopType.REDSTONE), 1).open((Player) e.getWhoClicked()));

        setItem(40, ItemBuilder.copyOf(new ItemStack(Material.SPAWNER)).name("§6Spawneurs").build(), e ->
                new ShopPageInv(ShopsManager.INSTANCE.getPage(ShopType.SPAWNERS), 1).open((Player) e.getWhoClicked()));
    }
}
