package fr.farmeurimmo.skylyblock.purpur.trade;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class TradeInv extends FastInv {

    private final int[] leftSlots = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39};
    private final int[] rightSlots = new int[]{14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43};
    private final Trade trade;

    public TradeInv(Player p, Trade trade, boolean isEmitter) {
        super(54, "§6§lTrade §8» §f" + p.getName() + " §7vs §f" + p.getServer().getPlayer(trade.getReceiver()).getName());

        this.trade = trade;

        if (isEmitter) {
            setItem(0, ItemBuilder.copyOf(new ItemStack(Material.DIAMOND)).name("§aArgent")
                    .lore("§7Cliquez pour ajouter de l'argent.").build());
        } else {
            setItem(0, ItemBuilder.copyOf(new ItemStack(Material.DIAMOND)).name("§aArgent")
                    .lore("§7Cliquez pour ajouter de l'argent.").build());
        }


        setCloseFilter(e -> {
            return TradesManager.INSTANCE.getTradeBetween(trade.getEmitter(), trade.getReceiver()) == null;
        });
    }

    public void update(boolean isEmitter) {
        if (isEmitter) {
            setItem(0, ItemBuilder.copyOf(new ItemStack(Material.DIAMOND)).name("§aArgent")
                    .lore("§7Cliquez pour ajouter de l'argent.").build());
        } else {
            setItem(0, ItemBuilder.copyOf(new ItemStack(Material.DIAMOND)).name("§aArgent")
                    .lore("§7Cliquez pour ajouter de l'argent.").build());
        }
    }

    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) {
            return;
        }
        if (e.getClickedInventory().equals(e.getWhoClicked().getInventory())) {
            return;
        }
        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre d'item dans " +
                "l'inventaire de l'échange."));
    }
}
