package fr.farmeurimmo.skylyblock.purpur.shop;

import java.util.ArrayList;

public class ShopPage {

    private final ShopType type;
    private final ArrayList<ShopItem> items;

    public ShopPage(ShopType type) {
        this.type = type;
        this.items = new ArrayList<>();
    }

    public String getName() {
        return type.getName();
    }

    public ArrayList<ShopItem> getItems() {
        return items;
    }

    public ShopItem getItem(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    public void addItem(ShopItem item) {
        items.add(item);
    }

    public ShopType getType() {
        return type;
    }
}
