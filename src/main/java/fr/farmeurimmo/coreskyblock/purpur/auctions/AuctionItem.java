package fr.farmeurimmo.coreskyblock.purpur.auctions;

import fr.farmeurimmo.coreskyblock.utils.InventorySyncUtils;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.UUID;

public record AuctionItem(UUID itemUUID, UUID ownerUUID, String ownerName, double price, ItemStack itemStack,
                          long createdAt) {

    public static ItemStack itemFromBase64(String itemStack) {
        return InventorySyncUtils.INSTANCE.itemStackFromBase64(itemStack);
    }

    public String itemToBase64() {
        return InventorySyncUtils.INSTANCE.itemStackToBase64(itemStack);
    }

    public String priceFormatted() {
        return NumberFormat.getInstance().format(price);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt >= AuctionHouseManager.AUCTION_EXPIRATION;
    }
}
