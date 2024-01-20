package fr.farmeurimmo.skylyblock.purpur.trade;

import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class TradeInv extends FastInv {

    private final int[] leftSlots = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39};
    private final int[] rightSlots = new int[]{14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43};

    public TradeInv(Player p, String target, Trade trade) {
        super(54, "§6§lTrade §8» §f" + p.getName() + " §7vs §f" + target);

        update(p.getUniqueId(), trade.getEmitter(), trade.getReceiver());

        setCloseFilter(e -> {
            if (TradesManager.INSTANCE.getTradeBetween(trade.getEmitter(), trade.getReceiver()) == null) return false;
            e.getPlayer().sendMessage(Component.text("§6§lTrade §8» §fVous avez annulé l'échange."));
            if (trade.getEmitter().equals(p.getUniqueId())) {
                Player receiver = Bukkit.getPlayer(trade.getReceiver());
                if (receiver != null) {
                    receiver.sendMessage(Component.text("§6§lTrade §8» §f" + p.getName() +
                            " §fa annulé l'échange."));
                    Bukkit.getScheduler().runTaskLater(SkylyBlock.INSTANCE, () -> {
                        receiver.closeInventory();
                        p.closeInventory();
                    }, 1);
                }
            } else {
                Player emitter = Bukkit.getPlayer(trade.getEmitter());
                if (emitter != null) {
                    emitter.sendMessage(Component.text("§6§lTrade §8» §f" + p.getName() +
                            " §fa annulé l'échange."));
                    Bukkit.getScheduler().runTaskLater(SkylyBlock.INSTANCE, () -> {
                        emitter.closeInventory();
                        p.closeInventory();
                    }, 1);
                }
            }
            TradesManager.INSTANCE.endTrade(trade);
            return false;
        });

        Bukkit.getScheduler().runTaskTimer(SkylyBlock.INSTANCE, (task) -> {
            if (TradesManager.INSTANCE.getTradeBetween(trade.getEmitter(), trade.getReceiver()) == null) {
                task.cancel();
                return;
            }
            update(p.getUniqueId(), trade.getEmitter(), trade.getReceiver());
        }, 0, 10);
    }

    public void update(UUID p, UUID emitter, UUID receiver) {
        Trade trade = TradesManager.INSTANCE.getTradeBetween(emitter, receiver);
        if (trade == null) {
            return;
        }

        boolean isEmitter = trade.getEmitter().equals(p);

        ItemStack nugget_left = ItemBuilder.copyOf(new ItemStack(Material.GOLD_NUGGET)).name("§e1 000$")
                .lore("§aClic gauche §7pour ajouter", "§cClic droit §7pour enlever",
                        "§8» §7Votre mise: §e" + (isEmitter ? trade.getEmitterMoney() : trade.getReceiverMoney()) + "$").build();
        ItemStack nugget_right = ItemBuilder.copyOf(new ItemStack(Material.GOLD_NUGGET)).name("§8» §fArgent misé §e§l" +
                (isEmitter ? trade.getReceiverMoney() : trade.getEmitterMoney()) + "$").build();


        if (isEmitter) {
            setItem(1, nugget_left, e -> {
                if (e.isLeftClick()) {
                    trade.setEmitterMoney(trade.getEmitterMoney() + 1000);
                } else {
                    if (trade.getEmitterMoney() - 1000 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0$"));
                        return;
                    }
                    trade.setEmitterMoney(trade.getEmitterMoney() - 1000);
                }
                update(p, emitter, receiver);
            });
            setItem(7, nugget_right);
        } else {
            setItem(1, nugget_left, e -> {
                if (e.isLeftClick()) {
                    trade.setReceiverMoney(trade.getReceiverMoney() + 1000);
                } else {
                    if (trade.getReceiverMoney() - 1000 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0$"));
                        return;
                    }
                    trade.setReceiverMoney(trade.getReceiverMoney() - 1000);
                }
                update(p, emitter, receiver);
            });

            setItem(7, nugget_right);
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
