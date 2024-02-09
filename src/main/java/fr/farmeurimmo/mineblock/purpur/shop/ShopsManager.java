package fr.farmeurimmo.mineblock.purpur.shop;

import fr.farmeurimmo.mineblock.common.DatabaseManager;
import fr.farmeurimmo.mineblock.purpur.shop.objects.ShopItem;
import fr.farmeurimmo.mineblock.purpur.shop.objects.ShopPage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ShopsManager {

    public static ShopsManager INSTANCE;
    private final ArrayList<ShopPage> pages = new ArrayList<>();

    public ShopsManager() {
        INSTANCE = this;

        for (ShopType type : ShopType.values()) {
            long start = System.currentTimeMillis();
            DatabaseManager.INSTANCE.getShopPage(type).thenAccept(page -> {
                if (page != null) {
                    pages.add(page);
                    System.out.println("Loaded shop page " + type.getName() + "§f in §6" + (System.currentTimeMillis() - start) + "ms");
                }
            });
        }
    }

    public ShopPage getPage(ShopType type) {
        return pages.stream().filter(page -> page.getType() == type).findFirst().orElse(null);
    }

    public int getSpaceAvailableFor(Player p, ItemStack iS) {
        int space = 0;
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack item = p.getInventory().getItem(i);
            if (item == null) {
                space += iS.getMaxStackSize();
            } else if (item.isSimilar(iS)) {
                space += iS.getMaxStackSize() - item.getAmount();
            }
        }
        if (p.getInventory().getItemInOffHand().getType().isAir()) {
            space -= iS.getMaxStackSize();
        }
        //same for armor
        if (p.getInventory().getHelmet() == null) {
            space -= iS.getMaxStackSize();
        }
        if (p.getInventory().getChestplate() == null) {
            space -= iS.getMaxStackSize();
        }
        if (p.getInventory().getLeggings() == null) {
            space -= iS.getMaxStackSize();
        }
        if (p.getInventory().getBoots() == null) {
            space -= iS.getMaxStackSize();
        }
        return space;
    }

    public int getAmountOf(Player p, ItemStack iS) {
        int amount = 0;
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack item = p.getInventory().getItem(i);
            if (item != null && item.isSimilar(iS)) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    public double getSellPrice(ItemStack iS) {
        for (ShopPage page : pages) {
            for (ShopItem item : page.getItems()) {
                if (item.getPureItemStack().isSimilar(iS)) {
                    return item.sellPrice();
                }
            }
        }
        return -1;
    }

    public boolean isSellable(ItemStack iS) {
        return getSellPrice(iS) > 0;
    }
}
