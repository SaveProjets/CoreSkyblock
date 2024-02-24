package fr.farmeurimmo.coreskyblock.purpur.shop;

import fr.farmeurimmo.coreskyblock.common.DatabaseManager;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.shop.objects.ShopItem;
import fr.farmeurimmo.coreskyblock.purpur.shop.objects.ShopPage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ShopsManager {

    public static ShopsManager INSTANCE;
    private final ArrayList<ShopPage> pages = new ArrayList<>();

    public ShopsManager() {
        INSTANCE = this;

        loadShops();
    }

    public CompletableFuture<Void> loadShops() {
        return CompletableFuture.runAsync(() -> {
            ArrayList<CompletableFuture<ShopPage>> futures = new ArrayList<>();
            for (ShopType type : ShopType.values()) {
                long start = System.currentTimeMillis();
                CompletableFuture<ShopPage> future = DatabaseManager.INSTANCE.getShopPage(type);
                future.thenAccept(page -> {
                    if (page != null) {
                        System.out.println("Loaded shop page " + type.getName() + "§f in §6" + (System.currentTimeMillis() - start) + "ms");
                    }
                });
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                System.out.println("Loaded §6" + futures.size() + "§f shop pages");
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    this.pages.clear();
                    futures.forEach(future -> future.thenAccept(page -> {
                        if (page != null) {
                            this.pages.add(page);
                        }
                    }));
                    return null;
                });
            });
        });
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
