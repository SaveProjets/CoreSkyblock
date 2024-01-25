package fr.farmeurimmo.skylyblock.purpur.shop;

import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;

public record ShopItem(String name, Material material, float price, float sellPrice) {

    public String getLineOfBuyFormatted() {
        if (price != -1) {
            return "§aAchat : §e" + NumberFormat.getInstance().format(price) + "$";
        } else {
            return "§cNon achetable";
        }
    }

    public String getLineOfSellFormatted() {
        if (sellPrice != -1) {
            return "§aVente : §e" + NumberFormat.getInstance().format(sellPrice) + "$";
        } else {
            return "§cNon vendable";
        }
    }

    public ItemStack getItemStack() {
        return ItemBuilder.copyOf(new ItemStack(material)).name(name).lore(getLineOfBuyFormatted(),
                getLineOfSellFormatted()).build();
    }

}
