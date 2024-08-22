package fr.farmeurimmo.coreskyblock.purpur.auctions.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.auctions.AuctionHouseManager;
import fr.farmeurimmo.coreskyblock.purpur.auctions.AuctionItem;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.auctions.AuctionHouseDataManager;
import fr.farmeurimmo.coreskyblock.utils.DateUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AuctionItemManagerInv extends FastInv {

    public static final long DELAY_BETWEEN_ACTIONS = 500;
    private final int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32,
            33, 34, 37, 38, 39, 40, 41, 42, 43};
    private boolean gotUpdate = false;
    private boolean closed = false;
    private long lastAction = System.currentTimeMillis();

    public AuctionItemManagerInv(UUID uuid) {
        super(54, "§0Gestionnaire d'objets de l'hôtel des ventes");

        setCloseFilter(p -> {
            gotUpdate = true;
            closed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
            if (closed) {
                task.cancel();
                return;
            }
            if (gotUpdate) return;
            gotUpdate = true;
            update(uuid);
        }, 0, 40L);
    }

    private void update(UUID uuid) {
        gotUpdate = false;

        ArrayList<AuctionItem> auctionItems = AuctionHouseManager.INSTANCE.getAuctionItemsForPlayerByCreationTime(uuid);

        int i = 0;
        for (int slot : slots) {
            if (i >= auctionItems.size()) {
                setItem(slot, null);
                continue;
            }
            AuctionItem auctionItem = auctionItems.get(i);
            ItemStack itemStack = auctionItem.itemStack().clone();

            ArrayList<Component> lore = new ArrayList<>();
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
                lore.addAll(itemStack.getItemMeta().lore());
            }

            lore.add(Component.text(""));
            lore.add(Component.text("§7Prix: §6" + auctionItem.priceFormatted() + " §7$"));
            lore.add(Component.text((auctionItem.isExpired() ? "§c§lExpiré" : "§aExpire dans: §7" +
                    DateUtils.getFormattedTimeLeft((int) ((AuctionHouseManager.AUCTION_EXPIRATION -
                            (System.currentTimeMillis() - auctionItem.createdAt())) / 1000)))));

            itemStack.editMeta(meta -> meta.lore(lore));

            setItem(slot, itemStack, e -> {
                if (System.currentTimeMillis() - lastAction < DELAY_BETWEEN_ACTIONS) return;

                Player p = (Player) e.getWhoClicked();
                if (p.getInventory().firstEmpty() == -1) {
                    p.sendMessage(Component.text("§cVous devez avoir un emplacement vide dans votre inventaire."));
                    return;
                }

                if (AuctionHouseManager.INSTANCE.buyingProcesses.containsKey(auctionItem)) {
                    p.sendMessage(Component.text("§cCet objet est déjà en cours d'achat."));
                    return;
                }

                AuctionHouseManager.INSTANCE.removeItemFromCache(auctionItem.itemUUID());
                CompletableFuture.runAsync(() -> {
                    JedisManager.INSTANCE.publishToRedis("coreskyblock", "auction:remove:" +
                            auctionItem.itemUUID() + ":" + CoreSkyblock.SERVER_NAME);
                    AuctionHouseDataManager.INSTANCE.deleteItem(auctionItem.itemUUID());
                });
                p.getInventory().addItem(auctionItem.itemStack());
                if (auctionItem.isExpired()) {
                    p.sendMessage(Component.text("§aVous avez récupéré votre objet expiré."));
                } else {
                    p.sendMessage(Component.text("§aVous avez récupéré votre objet."));
                }

                lastAction = System.currentTimeMillis();
            });

            i++;
        }

        setItem(49, ItemBuilder.copyOf(new ItemStack(Material.IRON_DOOR)).name("§6Retour").build(), e -> {
            new AuctionBrowserInv(uuid).open((Player) e.getWhoClicked());
        });
    }
}
