package fr.farmeurimmo.skylyblock.purpur.island.chests;

import fr.farmeurimmo.skylyblock.purpur.core.chests.ChestType;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Chest {

    private final ChestType type;
    private final Location block;
    private UUID owner;
    private ItemStack itemToBuySell;
    private double price;
    private boolean isSell;
    private boolean activeSellOrBuy;

    public Chest(ChestType type, Location block, UUID owner, ItemStack itemToBuySell, double price, boolean isSell, boolean activeSellOrBuy) {
        this.type = type;
        this.block = block;
        this.owner = owner;
        this.itemToBuySell = itemToBuySell;
        this.price = price;
        this.isSell = isSell;
        this.activeSellOrBuy = activeSellOrBuy;
    }

    public ChestType getType() {
        return type;
    }

    public Location getBlock() {
        return block;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public ItemStack getItemToBuySell() {
        return itemToBuySell;
    }

    public void setItemToBuySell(ItemStack itemToBuySell) {
        this.itemToBuySell = itemToBuySell;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isSell() {
        return isSell;
    }

    public void setSell(boolean sell) {
        isSell = sell;
    }

    public boolean isActiveSellOrBuy() {
        return activeSellOrBuy;
    }

    public String getOwnerName() {
        return owner == null ? "§cAucun" : owner.toString();
    }

    public String getSellOrBuy() {
        return isSell ? "§aVente" : "§cAchat";
    }

    public String getActiveSellOrBuy() {
        return activeSellOrBuy ? "§aActivé" : "§cDésactivé";
    }

    public void setActiveSellOrBuy(boolean activeSellOrBuy) {
        this.activeSellOrBuy = activeSellOrBuy;
    }

    public String getActiveSellOrBuyColor() {
        return activeSellOrBuy ? "§a" : "§c";
    }


}
