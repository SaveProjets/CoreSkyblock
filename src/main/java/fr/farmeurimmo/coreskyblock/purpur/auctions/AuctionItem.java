package fr.farmeurimmo.coreskyblock.purpur.auctions;

import com.google.gson.JsonObject;
import fr.farmeurimmo.coreskyblock.utils.InventorySyncUtils;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.UUID;

public record AuctionItem(UUID itemUUID, UUID ownerUUID, String ownerName, double price, ItemStack itemStack,
                          long createdAt) {

    public static ItemStack itemFromJson(JsonObject json) {
        return InventorySyncUtils.INSTANCE.jsonToItemStack(json);
    }

    public JsonObject itemToJson() {
        return InventorySyncUtils.INSTANCE.itemStackToJson(itemStack);
    }

    public String priceFormatted() {
        return NumberFormat.getInstance().format(price);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt >= AuctionHouseManager.AUCTION_EXPIRATION;
    }
}
