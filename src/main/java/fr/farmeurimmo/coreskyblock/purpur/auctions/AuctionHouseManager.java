package fr.farmeurimmo.coreskyblock.purpur.auctions;

import fr.farmeurimmo.coreskyblock.storage.auctions.AuctionHouseDataManager;

public class AuctionHouseManager {

    public static AuctionHouseManager INSTANCE;

    public AuctionHouseManager() {
        INSTANCE = this;

        new AuctionHouseDataManager();
    }
}
