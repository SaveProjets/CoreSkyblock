package fr.farmeurimmo.coreskyblock.purpur.auctions;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.auctions.AuctionHouseDataManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AuctionHouseManager {

    public static final int MAX_AUCTIONS = 5;
    public static AuctionHouseManager INSTANCE;
    private final ArrayList<AuctionItem> auctionItems = new ArrayList<>();

    public AuctionHouseManager() {
        INSTANCE = this;

        new AuctionHouseDataManager();

        CompletableFuture.supplyAsync(() -> AuctionHouseDataManager.INSTANCE.loadAuctions())
                .thenAccept((auctionItems) -> Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    this.auctionItems.addAll(auctionItems);
                    CoreSkyblock.INSTANCE.console.sendMessage("§7Loaded §6" + auctionItems.size()
                            + "§7 auction items from the database.");
                    return null;
                }));
    }

    public boolean canPostAnotherItem(UUID uuid) {
        int count = 0;
        for (AuctionItem auctionItem : auctionItems) {
            if (auctionItem.ownerUUID().equals(uuid)) {
                count++;
            }
        }
        return count < MAX_AUCTIONS;
    }

    public void addAuctionItem(AuctionItem auctionItem) {
        auctionItems.add(auctionItem);

        CompletableFuture.runAsync(() -> {
            AuctionHouseDataManager.INSTANCE.insertItem(auctionItem);

            //pubsub to indicate that a new auction item has been added
        });
    }
}
