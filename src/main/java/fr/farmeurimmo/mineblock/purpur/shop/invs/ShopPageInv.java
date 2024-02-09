package fr.farmeurimmo.mineblock.purpur.shop.invs;

import fr.farmeurimmo.mineblock.purpur.shop.objects.ShopItem;
import fr.farmeurimmo.mineblock.purpur.shop.objects.ShopPage;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopPageInv extends FastInv {

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33,
            34, 37, 38, 39, 40, 41, 42, 43};

    public ShopPageInv(ShopPage shopPage, int page) {
        super(54, "§8Boutique - " + shopPage.getName());

        setItems(new int[]{9, 0, 1, 7, 8, 17, 52, 53, 44, 36, 45, 46}, ItemBuilder.copyOf(new ItemStack(
                Material.LIME_STAINED_GLASS_PANE)).name("§6").build());

        int maxPage = (int) Math.ceil((double) shopPage.getItems().size() / SLOTS.length);
        if (page > maxPage) {
            page = maxPage;
        }

        int start = (page - 1) * SLOTS.length;
        int end = Math.min(start + SLOTS.length, shopPage.getItems().size());

        for (int i = start; i < end; i++) {
            ShopItem item = shopPage.getItem(i);
            if (item == null) break;
            int finalPage1 = page;
            setItem(SLOTS[i - start], item.getItemStack(), e -> {
                if (e.isLeftClick()) {
                    if (item.isBuyable()) {
                        new ShopAmountInv(item, true, shopPage, finalPage1).open((Player) e.getWhoClicked());
                    } else {
                        e.getWhoClicked().sendMessage(Component.text("§cCet item n'est pas achetable"));
                    }
                } else if (e.isRightClick()) {
                    if (item.isSellable()) {
                        new ShopAmountInv(item, false, shopPage, finalPage1).open((Player) e.getWhoClicked());
                    } else {
                        e.getWhoClicked().sendMessage(Component.text("§cCet item n'est pas vendable"));
                    }
                }
            });
        }

        if (page > 1) {
            int finalPage = page;
            setItem(45, ItemBuilder.copyOf(new ItemStack(Material.ARROW)).name("§6Page précédente").build(), e -> {
                new ShopPageInv(shopPage, finalPage - 1).open((Player) e.getWhoClicked());
            });
        }

        if (page < maxPage) {
            int finalPage = page;
            setItem(53, ItemBuilder.copyOf(new ItemStack(Material.ARROW)).name("§6Page suivante").build(), e -> {
                new ShopPageInv(shopPage, finalPage + 1).open((Player) e.getWhoClicked());
            });
        }

        setItem(0, ItemBuilder.copyOf(new ItemStack(Material.IRON_DOOR)).name("§6Retour").build(), e -> {
            new ShopInv().open((Player) e.getWhoClicked());
        });
    }
}
