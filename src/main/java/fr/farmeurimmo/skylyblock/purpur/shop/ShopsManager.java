package fr.farmeurimmo.skylyblock.purpur.shop;

import fr.farmeurimmo.skylyblock.common.DatabaseManager;

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
                    System.out.println("Loaded shop page " + type.getName() + " in " + (System.currentTimeMillis() - start) + "ms");
                }
            });
        }
    }

    public ShopPage getPage(ShopType type) {
        return pages.stream().filter(page -> page.getType() == type).findFirst().orElse(null);
    }
}
