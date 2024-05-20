package fr.farmeurimmo.coreskyblock.purpur.auctions;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record AuctionItem(UUID itemUUID, UUID ownerUUID, String ownerName, double price, ItemStack itemStack,
                          long createdAt) {
}
