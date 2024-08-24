package fr.farmeurimmo.coreskyblock.purpur.auctions.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.auctions.AuctionHouseManager;
import fr.farmeurimmo.coreskyblock.purpur.auctions.AuctionItem;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class AuctionBrowserInv extends FastInv {

    public static final long DELAY_BETWEEN_ACTIONS = 500;
    private final int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32,
            33, 34, 37, 38, 39, 40, 41, 42, 43};
    private boolean closed = false;
    private int page = 0;
    private long lastAction = System.currentTimeMillis();

    public AuctionBrowserInv(UUID uuid) {
        super(54, "§0Hôtel des ventes");

        setCloseFilter(p -> {
            closed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
            if (closed) {
                task.cancel();
                return;
            }
            update(uuid);
        }, 0, 40L);
    }

    private void update(UUID uuid) {
        int i = page * slots.length;

        ArrayList<AuctionItem> auctionItems = AuctionHouseManager.INSTANCE.getAuctionItemsByCreationTime();
        if (i >= auctionItems.size()) {
            page--;
            update(uuid);
            return;
        }

        for (int slot : slots) {
            if (i >= auctionItems.size()) {
                setItem(slot, null);
                continue;
            }
            if (i < 0) {
                continue;
            }
            AuctionItem auctionItem = auctionItems.get(i);
            ItemStack itemStack = auctionItem.itemStack().clone();

            ArrayList<Component> lore = new ArrayList<>();
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
                lore.addAll(itemStack.getItemMeta().lore());
            }

            lore.add(Component.text(""));
            lore.add(Component.text("§7Vendeur: §6" + auctionItem.ownerName()));
            lore.add(Component.text("§7Prix: §6" + auctionItem.priceFormatted() + " §7$"));

            itemStack.editMeta(meta -> meta.lore(lore));

            setItem(slot, itemStack, e -> {
                Player p = (Player) e.getWhoClicked();
                if (System.currentTimeMillis() - lastAction < DELAY_BETWEEN_ACTIONS) {
                    p.sendMessage("§cVeuillez patienter avant de faire une autre action.");
                    return;
                }
                if (!AuctionHouseManager.INSTANCE.isStillListed(auctionItem)) {
                    p.sendMessage("§cCet objet n'est plus en vente.");
                    return;
                }
                if (p.getUniqueId().equals(auctionItem.ownerUUID())) {
                    p.sendMessage("§cVous ne pouvez pas acheter votre propre objet.");
                    return;
                }
                if (p.getInventory().firstEmpty() == -1) {
                    p.sendMessage("§cVotre inventaire est plein.");
                    return;
                }
                SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
                if (user == null) {
                    p.sendMessage("§cErreur: Impossible de charger vos données.");
                    return;
                }
                if (user.getMoney() < auctionItem.price()) {
                    p.sendMessage("§cVous n'avez pas assez d'argent.");
                    return;
                }
                if (AuctionHouseManager.INSTANCE.buyingProcesses.containsKey(auctionItem)) {
                    p.sendMessage("§cCet objet est déjà en cours d'achat par quelqu'un d'autre.");
                    return;
                }
                lastAction = System.currentTimeMillis();
                p.sendMessage(Component.text("§7Tentative d'achat..."));
                AuctionHouseManager.INSTANCE.startBuyProcess(auctionItem, p.getUniqueId(), p.getName());
            });
            i++;
        }

        if (page > 0) {
            setItem(48, CommonItemStacks.getCommonPreviousPage(), e -> {
                page--;
                update(uuid);
            });
        } else {
            setItem(48, null);
        }

        if (auctionItems.size() > (page + 1) * slots.length) {
            setItem(50, CommonItemStacks.getCommonNextPage(), e -> {
                page++;
                update(uuid);
            });
        } else {
            setItem(50, null);
        }

        setItem(45, ItemBuilder.copyOf(new ItemStack(Material.CHEST)).name("§6Gérer mes objets").build(), e -> {
            new AuctionItemManagerInv(uuid).open((Player) e.getWhoClicked());
        });
    }
}
