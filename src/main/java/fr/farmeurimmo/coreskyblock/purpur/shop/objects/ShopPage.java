package fr.farmeurimmo.coreskyblock.purpur.shop.objects;

import fr.farmeurimmo.coreskyblock.purpur.shop.ShopType;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
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

    public ItemStack getGlassPane(boolean isAdd, int amount) {
        return ItemBuilder.copyOf(new ItemStack((isAdd ?
                        org.bukkit.Material.GREEN_STAINED_GLASS_PANE : org.bukkit.Material.RED_STAINED_GLASS_PANE), amount))
                .name((isAdd ? "§aAjouter " + NumberFormat.getInstance().format(amount) : "§cRetirer " + NumberFormat.getInstance().format(amount)))
                .build();
    }
}
