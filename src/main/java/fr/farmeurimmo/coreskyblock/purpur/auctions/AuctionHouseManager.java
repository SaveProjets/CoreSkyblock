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
    public static final long AUCTION_EXPIRATION = 7 * 24 * 60 * 60 * 1000;
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
        auctionItemsByCreationTime.removeIf(AuctionItem::isExpired);
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

    public ArrayList<AuctionItem> getAuctionItemsForPlayer(UUID uuid) {
        ArrayList<AuctionItem> auctionItemsForPlayer = new ArrayList<>();
        for (AuctionItem auctionItem : auctionItems) {
            if (auctionItem.ownerUUID().equals(uuid)) {
                auctionItemsForPlayer.add(auctionItem);
            }
        }
        return auctionItemsForPlayer;
    }

    public ArrayList<AuctionItem> getAuctionItemsForPlayerByCreationTime(UUID uuid) {
        ArrayList<AuctionItem> auctionItemsForPlayer = getAuctionItemsForPlayer(uuid);
        auctionItemsForPlayer.sort(Comparator.comparingLong(AuctionItem::createdAt));
        return auctionItemsForPlayer;
    }

    public void startBuyProcess(AuctionItem auctionItem, UUID buyer, String buyerName) {
        long time = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> {
            JedisManager.INSTANCE.publishToRedis("coreskyblock", "auction:buy:" +
                    auctionItem.itemUUID().toString() + ":" + buyer + ":" + time + ":" + buyerName + ":" + CoreSkyblock.SERVER_NAME);
        });
        addBuyingProcess(auctionItem, buyer, time, buyerName, false);
    }

    public void removeItemFromCache(UUID auctionUUID) {
        auctionItems.removeIf(auctionItem -> auctionItem.itemUUID().equals(auctionUUID));
    }

    public void addBuyingProcess(AuctionItem auctionItem, UUID buyer, long time, String buyerName, boolean fromRedis) {
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

                    Player buyerPlayer = Bukkit.getPlayer(buyer);
                    if (buyerPlayer != null) {
                        SkyblockUser buyerUser = SkyblockUsersManager.INSTANCE.getCachedUsers().get(buyer);
                        if (buyerUser != null) {
                            buyerUser.setMoney(buyerUser.getMoney() - auctionItem.price());
                            buyerPlayer.sendMessage("§aVous avez acheté l'objet pour §6" + auctionItem.priceFormatted() + " $§a.");
                        }
                        if (buyerPlayer.getInventory().firstEmpty() != -1) {
                            buyerPlayer.getInventory().addItem(auctionItem.itemStack());
                        } else {
                            buyerPlayer.getWorld().dropItem(buyerPlayer.getLocation(), auctionItem.itemStack());
                        }
                    }

                    Player sellerPlayer = Bukkit.getPlayer(auctionItem.ownerUUID());
                    if (sellerPlayer != null) {
                        SkyblockUser sellerUser = SkyblockUsersManager.INSTANCE.getCachedUsers().get(auctionItem.ownerUUID());
                        if (sellerUser != null) {
                            sellerUser.setMoney(sellerUser.getMoney() + auctionItem.price());
                            sellerPlayer.sendMessage("§6§lAH §8» §7" + buyerName +
                                    " §aa acheté votre objet pour §6" + auctionItem.priceFormatted() + " $§a.");
                        }
                        return;
                    }

                    if (fromRedis) return;

                    CompletableFuture.runAsync(() -> {
                        AuctionHouseDataManager.INSTANCE.deleteItem(auctionItem.itemUUID());
                        JedisManager.INSTANCE.publishToRedis("coreskyblock", "auction:remove:" +
                                auctionItem.itemUUID() + ":" + CoreSkyblock.SERVER_NAME);

                        if (CoreSkyblock.INSTANCE.getServerNameWherePlayerIsConnected(auctionItem.ownerUUID()) != null) {
                            JedisManager.INSTANCE.publishToRedis("coreskyblock", "auction:givemoney:" + buyer
                                    + ":" + auctionItem.price() + ":" + CoreSkyblock.SERVER_NAME);
                        } else {
                            SkyblockUser user = SkyblockUsersManager.INSTANCE.loadUser(auctionItem.ownerUUID(),
                                    auctionItem.ownerName());
                            if (user != null) {
                                user.addMoney(auctionItem.price());
                                SkyblockUsersManager.INSTANCE.unloadUser(user);
                            }
                        }
                    });
                }
            }, 10);
        }
        buyingProcesses.put(auctionItem, Pair.of(buyer, time));
    }
}
