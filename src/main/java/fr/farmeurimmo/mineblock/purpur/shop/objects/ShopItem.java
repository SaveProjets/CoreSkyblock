package fr.farmeurimmo.mineblock.purpur.shop.objects;

import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;

public record ShopItem(String name, Material material, float price, float sellPrice) {

    public String getLineOfBuyFormatted() {
        if (isBuyable()) {
            return "§aAchat : §e" + NumberFormat.getInstance().format(price) + "$";
        } else {
            return "§cNon achetable";
        }
    }

    public String getLineOfSellFormatted() {
        if (isSellable()) {
            return "§aVente : §e" + NumberFormat.getInstance().format(sellPrice) + "$";
        } else {
            return "§cNon vendable";
        }
    }

    public ItemStack getItemStack(boolean buy, int amount) {
        return ItemBuilder.copyOf(new ItemStack(material, amount)).name(buy ? "§aCliquez pour acheter" :
                "§aCliquez pour vendre").lore("§6x" + amount + " §e" + getName(), "§6Total : §e" +
                NumberFormat.getInstance().format(amount * (buy ? price : sellPrice)) + "$").build();
    }

    public String getName() {
        if (name == null) {
            return getMaterialFormatted();
        }
        return name;
    }

    public String getMaterialFormatted() {
        String name = this.material.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public ItemStack getPureItemStack() {
        return ItemBuilder.copyOf(new ItemStack(material)).build();
    }

    public boolean isBuyable() {
        return price != -1;
    }

    public boolean isSellable() {
        return sellPrice != -1;
    }

    public ItemStack getItemStack() {
        if (name != null) {
            return ItemBuilder.copyOf(new ItemStack(material)).name(getName()).lore(getLineOfBuyFormatted(),
                    getLineOfSellFormatted()).build();
        }
        return ItemBuilder.copyOf(new ItemStack(material)).lore(getLineOfBuyFormatted(),
                getLineOfSellFormatted()).build();
    }

    public ItemStack getItemStackForStackBuy(int amountOfStacks) {
        int numberOfItems = material.getMaxStackSize() * amountOfStacks;
        String price = NumberFormat.getInstance().format(this.price * numberOfItems);
        return ItemBuilder.copyOf(new ItemStack(material, amountOfStacks)).lore("§6x" + numberOfItems + " §e" +
                getName(), "§6Total : §e" + price + "$").build();
    }

}
