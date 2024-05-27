package fr.farmeurimmo.coreskyblock.purpur.auctions;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.auctions.AuctionHouseDataManager;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AuctionHouseManager {

    public static final int MAX_AUCTIONS = 5;
    public static AuctionHouseManager INSTANCE;
    public final Map<AuctionItem, Pair<UUID, Long>> buyingProcesses = new HashMap<>();
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
        addAuctionItemToCache(auctionItem);

        CompletableFuture.runAsync(() -> {
            AuctionHouseDataManager.INSTANCE.insertItem(auctionItem);

            JedisManager.INSTANCE.publishToRedis("coreskyblock", "auction:create:" +
                    auctionItem.itemUUID().toString() + ":" + CoreSkyblock.SERVER_NAME);
        });
    }

    public void addAuctionItemToCache(AuctionItem auctionItem) {
        auctionItems.add(auctionItem);
    }

    public final ArrayList<AuctionItem> getAuctionItemsByCreationTime() {
        ArrayList<AuctionItem> auctionItemsByCreationTime = new ArrayList<>(auctionItems);
        auctionItemsByCreationTime.sort(Comparator.comparingLong(AuctionItem::createdAt));
        return auctionItemsByCreationTime;
    }

    public boolean isStillListed(AuctionItem auctionItem) {
        return auctionItems.contains(auctionItem);
    }

    public AuctionItem getByUUID(UUID auctionUUID) {
        for (AuctionItem auctionItem : auctionItems) {
            if (auctionItem.itemUUID().equals(auctionUUID)) {
                return auctionItem;
            }
        }
        return null;
    }

    public void startBuyProcess(AuctionItem auctionItem, UUID buyer) {
        long time = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> {
            JedisManager.INSTANCE.publishToRedis("coreskyblock", "auction:buy:" +
                    auctionItem.itemUUID().toString() + ":" + buyer + ":" + time + ":" + CoreSkyblock.SERVER_NAME);
        });
        buyingProcesses.put(auctionItem, Pair.of(buyer, time));
    }

    public void removeItemFromCache(UUID auctionUUID) {
        auctionItems.removeIf(auctionItem -> auctionItem.itemUUID().equals(auctionUUID));
    }

    public void addBuyingProcess(AuctionItem auctionItem, UUID buyer, long time) {
        if (buyingProcesses.containsKey(auctionItem)) {
            if (buyingProcesses.get(auctionItem).right() < time) {
                Player oldBuyer = Bukkit.getPlayer(buyingProcesses.get(auctionItem).left());
                if (oldBuyer != null) oldBuyer.sendMessage(Component.text(
                        "§cVotre achat a été annulé car quelqu'un d'autre a acheté l'objet avant vous."));
            }
        } else {
            Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> {
                if (buyingProcesses.containsKey(auctionItem) && buyingProcesses.get(auctionItem).right() == time) {
                    // if the buyer is still the same after 20 ticks, remove the buying process and process the purchase
                    buyingProcesses.remove(auctionItem);
                    auctionItems.remove(auctionItem);
                    CompletableFuture.runAsync(() -> {
                        AuctionHouseDataManager.INSTANCE.deleteItem(auctionItem.itemUUID());
                        JedisManager.INSTANCE.publishToRedis("coreskyblock", "auction:remove:" +
                                auctionItem.itemUUID() + ":" + CoreSkyblock.SERVER_NAME);

                        Player buyerPlayer = Bukkit.getPlayer(buyer);
                        if (buyerPlayer != null) {
                            SkyblockUser buyerUser = SkyblockUsersManager.INSTANCE.getCachedUsers().get(buyer);
                            if (buyerUser != null) {
                                buyerUser.setMoney(buyerUser.getMoney() - auctionItem.price());
                                buyerPlayer.sendMessage("§aVous avez acheté l'objet pour §6" + auctionItem.priceFormatted() + "§a.");
                            }
                        }

                        Player sellerPlayer = Bukkit.getPlayer(auctionItem.ownerUUID());
                        if (sellerPlayer != null) {
                            SkyblockUser sellerUser = SkyblockUsersManager.INSTANCE.getCachedUsers().get(auctionItem.ownerUUID());
                            if (sellerUser != null) {
                                sellerUser.setMoney(sellerUser.getMoney() + auctionItem.price());
                                sellerPlayer.sendMessage("§aVotre objet a été vendu pour §6" + auctionItem.priceFormatted() + "§a.");
                            }
                            return;
                        }

                        //if seller is on another skyblock server
                        // or if seller is offline

                        if (CoreSkyblock.INSTANCE.getServerNameWherePlayerIsConnected(auctionItem.ownerUUID()) != null) {
                            JedisManager.INSTANCE.publishToRedis("coreskyblock", "auction:givemoney:" +
                                    ":" + auctionItem.price() + ":" + CoreSkyblock.SERVER_NAME);
                        }
                    });
                }
            }, 20);
        }
        buyingProcesses.put(auctionItem, Pair.of(buyer, time));
    }
}
