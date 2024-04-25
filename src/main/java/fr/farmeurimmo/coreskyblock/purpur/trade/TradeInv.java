package fr.farmeurimmo.coreskyblock.purpur.trade;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.coreskyblock.utils.ExperienceUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.Objects;
import java.util.UUID;

public class TradeInv extends FastInv {

    private static final int[] PANE_SLOTS = new int[]{4, 13, 22, 31, 40, 49};
    private final static int MAX_PER_TRADE = 15;
    private final int[] leftSlots = new int[]{18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48};
    private final int[] rightSlots = new int[]{24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53};
    private final UUID emitter;
    private final UUID receiver;

    public TradeInv(Player p, String target, Trade trade) {
        super(54, "§6§lTrade §8» §f" + p.getName() + " §7vs §f" + target);

        this.emitter = trade.getEmitter();
        this.receiver = trade.getReceiver();

        update(p.getUniqueId(), trade.getEmitter(), trade.getReceiver());

        setCloseFilter(e -> {
            if (TradesManager.INSTANCE.getTradeBetween(trade.getEmitter(), trade.getReceiver()) == null) return false;
            e.getPlayer().sendMessage(Component.text("§6§lTrade §8» §fVous avez annulé l'échange."));
            if (trade.getEmitter().equals(p.getUniqueId())) {
                Player receiver = Bukkit.getPlayer(trade.getReceiver());
                if (receiver != null) {
                    receiver.sendMessage(Component.text("§6§lTrade §8» §f" + p.getName() +
                            " §fa annulé l'échange."));
                    Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> {
                        receiver.closeInventory();
                        p.closeInventory();
                    }, 1);
                }
            } else {
                Player emitter = Bukkit.getPlayer(trade.getEmitter());
                if (emitter != null) {
                    emitter.sendMessage(Component.text("§6§lTrade §8» §f" + p.getName() +
                            " §fa annulé l'échange."));
                    Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> {
                        emitter.closeInventory();
                        p.closeInventory();
                    }, 1);
                }
            }
            TradesManager.INSTANCE.cancelTrade(trade);
            return false;
        });

        Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, (task) -> {
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

        ItemStack nugget_left = ItemBuilder.copyOf(new ItemStack(Material.GOLD_NUGGET)).name("§e100$")
                .lore("§aClic gauche §7pour ajouter", "§cClic droit §7pour enlever",
                        "§8» §7Votre mise: §e" + (isEmitter ? trade.getEmitterMoney() : trade.getReceiverMoney()) + "$").build();
        ItemStack nugget_right = ItemBuilder.copyOf(new ItemStack(Material.GOLD_NUGGET)).name("§8» §fArgent misé §e§l" +
                (isEmitter ? trade.getReceiverMoney() : trade.getEmitterMoney()) + "$").build();

        ItemStack goldIngot_left = ItemBuilder.copyOf(new ItemStack(Material.GOLD_INGOT)).name("§e1 000$")
                .lore("§aClic gauche §7pour ajouter", "§cClic droit §7pour enlever",
                        "§8» §7Votre mise: §e" + (isEmitter ? trade.getEmitterMoney() : trade.getReceiverMoney()) + "$").build();
        ItemStack goldIngot_right = ItemBuilder.copyOf(new ItemStack(Material.GOLD_INGOT)).name("§8» §fArgent misé §e§l" +
                (isEmitter ? trade.getReceiverMoney() : trade.getEmitterMoney()) + "$").build();

        ItemStack goldBlock_left = ItemBuilder.copyOf(new ItemStack(Material.GOLD_BLOCK)).name("§e100 000$")
                .lore("§aClic gauche §7pour ajouter", "§cClic droit §7pour enlever",
                        "§8» §7Votre mise: §e" + (isEmitter ? trade.getEmitterMoney() : trade.getReceiverMoney()) + "$").build();
        ItemStack goldBlock_right = ItemBuilder.copyOf(new ItemStack(Material.GOLD_BLOCK)).name("§8» §fArgent misé §e§l" +
                (isEmitter ? trade.getReceiverMoney() : trade.getEmitterMoney()) + "$").build();

        ItemStack ready_right = ItemBuilder.copyOf(new ItemStack((isEmitter ? trade.isReceiverReady() : trade.isEmitterReady()) ?
                        Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE))
                .name("§8» §fPrêt").build();
        ItemStack ready = ItemBuilder.copyOf(new ItemStack((isEmitter ? trade.isEmitterReady() : trade.isReceiverReady()) ?
                        Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE))
                .name("§8» §fPrêt").build();

        ItemStack lowExp_left = ItemBuilder.copyOf(new ItemStack(Material.EXPERIENCE_BOTTLE)).name("§e10 points d'expérience")
                .lore("§aClic gauche §7pour ajouter", "§cClic droit §7pour enlever",
                        "§8» §7Votre mise: §e" + (isEmitter ? trade.getEmitterExp() : trade.getReceiverExp()) + " points").build();
        ItemStack lowExp_right = ItemBuilder.copyOf(new ItemStack(Material.EXPERIENCE_BOTTLE)).name("§8» §fPoints d'expérience §e§l" +
                (isEmitter ? trade.getReceiverExp() : trade.getEmitterExp()) + " points").build();

        ItemStack mediumExp_left = ItemBuilder.copyOf(new ItemStack(Material.EXPERIENCE_BOTTLE, 32)).name("§e250 points d'expérience")
                .lore("§aClic gauche §7pour ajouter", "§cClic droit §7pour enlever",
                        "§8» §7Votre mise: §e" + (isEmitter ? trade.getEmitterExp() : trade.getReceiverExp()) + " points").build();
        ItemStack mediumExp_right = ItemBuilder.copyOf(new ItemStack(Material.EXPERIENCE_BOTTLE, 32)).name("§8» §fPoints d'expérience §e§l" +
                (isEmitter ? trade.getReceiverExp() : trade.getEmitterExp()) + " points").build();

        ItemStack highExp_left = ItemBuilder.copyOf(new ItemStack(Material.EXPERIENCE_BOTTLE, 64)).name("§e1 000 points d'expérience")
                .lore("§aClic gauche §7pour ajouter", "§cClic droit §7pour enlever",
                        "§8» §7Votre mise: §e" + (isEmitter ? trade.getEmitterExp() : trade.getReceiverExp()) + " points").build();
        ItemStack highExp_right = ItemBuilder.copyOf(new ItemStack(Material.EXPERIENCE_BOTTLE, 64)).name("§8» §fPoints d'expérience §e§l" +
                (isEmitter ? trade.getReceiverExp() : trade.getEmitterExp()) + " points").build();


        Player emitterPlayer = Bukkit.getPlayer(emitter);
        double exp = 0;
        if (emitterPlayer != null) {
            exp = ExperienceUtils.getExp(emitterPlayer);
        }
        Player receiverPlayer = Bukkit.getPlayer(receiver);
        double exp2 = 0;
        if (receiverPlayer != null) {
            exp2 = ExperienceUtils.getExp(receiverPlayer);
        }
        ItemStack playerLeftHead = getPlayerHeads(emitter, exp);
        ItemStack playerRightHead = getPlayerHeads(receiver, exp2);

        if (trade.isEmitterReady() && trade.isReceiverReady()) {
            if (trade.getEmitterReadyTime() != -1 && trade.getReceiverReadyTime() != -1) {
                long timeLeft = 5000 - (System.currentTimeMillis() - trade.getEmitterReadyTime());
                long timeLeft2 = 5000 - (System.currentTimeMillis() - trade.getReceiverReadyTime());
                if (timeLeft2 <= 0 && timeLeft <= 0) {
                    TradesManager.INSTANCE.endTrade(trade);
                    return;
                }
                long time = Math.max(timeLeft, timeLeft2);
                int seconds = (int) (time / 1000);
                if (seconds == 0) seconds = 1;

                ready.setAmount(seconds);
                ready_right.setAmount(seconds);
            }
        }


        if (isEmitter) {
            setItem(0, ready, e -> {
                trade.setEmitterReady(!trade.isEmitterReady());
                if (trade.isTradeReadyToBeEnded()) {
                    TradesManager.INSTANCE.endTrade(trade);
                    return;
                }
                update(p, emitter, receiver);
            });
            setItem(1, nugget_left, e -> {
                if (trade.isEmitterReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(e.getWhoClicked().getUniqueId());
                if (user == null) {
                    e.getWhoClicked().sendMessage(Component.text("§cUne erreur est survenue lors de la récupération de votre profil."));
                    return;
                }
                if (e.isLeftClick()) {
                    if (user.getMoney() < trade.getEmitterMoney() + 100) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez d'argent."));
                        return;
                    }
                    trade.setEmitterMoney(trade.getEmitterMoney() + 100);
                } else {
                    if (trade.getEmitterMoney() - 100 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0$"));
                        return;
                    }
                    trade.setEmitterMoney(trade.getEmitterMoney() - 100);
                }
                update(p, emitter, receiver);
            });
            setItem(2, goldIngot_left, e -> {
                if (trade.isEmitterReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(e.getWhoClicked().getUniqueId());
                if (user == null) {
                    e.getWhoClicked().sendMessage(Component.text("§cUne erreur est survenue lors de la récupération de votre profil."));
                    return;
                }
                if (e.isLeftClick()) {
                    if (user.getMoney() < trade.getEmitterMoney() + 1_000) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez d'argent."));
                        return;
                    }
                    trade.setEmitterMoney(trade.getEmitterMoney() + 1_000);
                } else {
                    if (trade.getEmitterMoney() - 1000 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0$"));
                        return;
                    }
                    trade.setEmitterMoney(trade.getEmitterMoney() - 1_000);
                }
                update(p, emitter, receiver);
            });
            setItem(3, goldBlock_left, e -> {
                if (trade.isEmitterReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                if (e.isLeftClick()) {
                    SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(e.getWhoClicked().getUniqueId());
                    if (user == null) {
                        e.getWhoClicked().sendMessage(Component.text("§cUne erreur est survenue lors de la récupération de votre profil."));
                        return;
                    }
                    if (user.getMoney() < trade.getEmitterMoney() + 100_000) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez d'argent."));
                        return;
                    }
                    trade.setEmitterMoney(trade.getEmitterMoney() + 100_000);
                } else {
                    if (trade.getEmitterMoney() - 100_000 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0$"));
                        return;
                    }
                    trade.setEmitterMoney(trade.getEmitterMoney() - 100_000);
                }
                update(p, emitter, receiver);
            });
            setItem(9, playerLeftHead);
            setItem(14, playerRightHead);
            setItem(10, lowExp_left, e -> {
                if (trade.isEmitterReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                if (e.isLeftClick()) {
                    Player whoClicked = (Player) e.getWhoClicked();
                    if (trade.getEmitterExp() + 10 > ExperienceUtils.getExp(whoClicked)) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre plus de votre expérience."));
                        return;
                    }
                    trade.setEmitterExp(trade.getEmitterExp() + 10);
                } else {
                    if (trade.getEmitterExp() - 10 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0 point d'expérience."));
                        return;
                    }
                    trade.setEmitterExp(trade.getEmitterExp() - 10);
                }
                update(p, emitter, receiver);
            });
            setItem(11, mediumExp_left, e -> {
                if (trade.isEmitterReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                if (e.isLeftClick()) {
                    Player whoClicked = (Player) e.getWhoClicked();
                    if (trade.getEmitterExp() + 250 > ExperienceUtils.getExp(whoClicked)) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre plus de votre expérience."));
                        return;
                    }
                    trade.setEmitterExp(trade.getEmitterExp() + 250);
                } else {
                    if (trade.getEmitterExp() - 250 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0 point d'expérience."));
                        return;
                    }
                    trade.setEmitterExp(trade.getEmitterExp() - 250);
                }
                update(p, emitter, receiver);
            });
            setItem(12, highExp_left, e -> {
                if (trade.isEmitterReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                if (e.isLeftClick()) {
                    Player whoClicked = (Player) e.getWhoClicked();
                    if (trade.getEmitterExp() + 1_000 > ExperienceUtils.getExp(whoClicked)) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre plus de votre expérience."));
                        return;
                    }
                    trade.setEmitterExp(trade.getEmitterExp() + 1_000);
                } else {
                    if (trade.getEmitterExp() - 1_000 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0 point d'expérience."));
                        return;
                    }
                    trade.setEmitterExp(trade.getEmitterExp() - 1_000);
                }
                update(p, emitter, receiver);
            });
        } else {
            setItem(0, ready, e -> {
                trade.setReceiverReady(!trade.isReceiverReady());
                update(p, emitter, receiver);
            });
            setItem(1, nugget_left, e -> {
                if (trade.isReceiverReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                if (e.isLeftClick()) {
                    SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(e.getWhoClicked().getUniqueId());
                    if (user == null) {
                        e.getWhoClicked().sendMessage(Component.text("§cUne erreur est survenue lors de la récupération de votre profil."));
                        return;
                    }
                    if (user.getMoney() < trade.getReceiverMoney() + 100) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez d'argent."));
                        return;
                    }
                    trade.setReceiverMoney(trade.getReceiverMoney() + 100);
                } else {
                    if (trade.getReceiverMoney() - 100 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0$"));
                        return;
                    }
                    trade.setReceiverMoney(trade.getReceiverMoney() - 100);
                }
                update(p, emitter, receiver);
            });
            setItem(2, goldIngot_left, e -> {
                if (trade.isReceiverReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                if (e.isLeftClick()) {
                    SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(e.getWhoClicked().getUniqueId());
                    if (user == null) {
                        e.getWhoClicked().sendMessage(Component.text("§cUne erreur est survenue lors de la récupération de votre profil."));
                        return;
                    }
                    if (user.getMoney() < trade.getReceiverMoney() + 1_000) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez d'argent."));
                        return;
                    }
                    trade.setReceiverMoney(trade.getReceiverMoney() + 1_000);
                } else {
                    if (trade.getReceiverMoney() - 1_000 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0$"));
                        return;
                    }
                    trade.setReceiverMoney(trade.getReceiverMoney() - 1_000);
                }
                update(p, emitter, receiver);
            });
            setItem(3, goldBlock_left, e -> {
                if (trade.isReceiverReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                if (e.isLeftClick()) {
                    SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(e.getWhoClicked().getUniqueId());
                    if (user == null) {
                        e.getWhoClicked().sendMessage(Component.text("§cUne erreur est survenue lors de la récupération de votre profil."));
                        return;
                    }
                    if (user.getMoney() < trade.getReceiverMoney() + 100_000) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez d'argent."));
                        return;
                    }
                    trade.setReceiverMoney(trade.getReceiverMoney() + 100_000);
                } else {
                    if (trade.getReceiverMoney() - 100_000 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0$"));
                        return;
                    }
                    trade.setReceiverMoney(trade.getReceiverMoney() - 100_000);
                }
                update(p, emitter, receiver);
            });
            setItem(9, playerRightHead);
            setItem(14, playerLeftHead);
            setItem(10, lowExp_left, e -> {
                if (trade.isReceiverReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                if (e.isLeftClick()) {
                    Player whoClicked = (Player) e.getWhoClicked();
                    if (trade.getReceiverExp() + 10 > ExperienceUtils.getExp(whoClicked)) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre plus de votre expérience."));
                        return;
                    }
                    trade.setReceiverExp(trade.getReceiverExp() + 10);
                } else {
                    if (trade.getReceiverExp() - 10 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0 point d'expérience."));
                        return;
                    }
                    trade.setReceiverExp(trade.getReceiverExp() - 10);
                }
                update(p, emitter, receiver);
            });
            setItem(11, mediumExp_left, e -> {
                if (trade.isReceiverReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                if (e.isLeftClick()) {
                    Player whoClicked = (Player) e.getWhoClicked();
                    if (trade.getReceiverExp() + 250 > ExperienceUtils.getExp(whoClicked)) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre plus de votre expérience."));
                        return;
                    }
                    trade.setReceiverExp(trade.getReceiverExp() + 250);
                } else {
                    if (trade.getReceiverExp() - 250 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0 point d'expérience."));
                        return;
                    }
                    trade.setReceiverExp(trade.getReceiverExp() - 250);
                }
                update(p, emitter, receiver);
            });
            setItem(12, highExp_left, e -> {
                if (trade.isReceiverReady()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                    return;
                }
                if (e.isLeftClick()) {
                    Player whoClicked = (Player) e.getWhoClicked();
                    if (trade.getReceiverExp() + 1_000 > ExperienceUtils.getExp(whoClicked)) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre plus de votre expérience."));
                        return;
                    }
                    trade.setReceiverExp(trade.getReceiverExp() + 1_000);
                } else {
                    if (trade.getReceiverExp() - 1_000 < 0) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre moins de 0 point d'expérience."));
                        return;
                    }
                    trade.setReceiverExp(trade.getReceiverExp() - 1_000);
                }
                update(p, emitter, receiver);
            });
        }
        setItem(8, ready_right);
        setItem(7, nugget_right);
        setItem(6, goldIngot_right);
        setItem(5, goldBlock_right);
        setItem(15, lowExp_right);
        setItem(16, mediumExp_right);
        setItem(17, highExp_right);

        // place the items (the player should always have his items on the left side)
        // so if he is the emitter, he will have his items on the left side same for the receiver
        // when a player click an item in his inventory, it will be removed from his inventory and added to the trade
        // if the player click an item in the trade, it will be removed from the trade and added to his inventory
        // on the right side, the items are not clickable

        // emitter
        for (int i = 0; i < MAX_PER_TRADE; i++) {
            setItem(leftSlots[i], null);
            setItem(rightSlots[i], null);
        }
        if (isEmitter) {
            for (int i = 0; i < trade.getEmitterItems().size(); i++) {
                setItem(leftSlots[i], trade.getEmitterItems().get(i), e -> {
                    if (trade.isEmitterReady()) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                        return;
                    }
                    trade.getEmitterItems().remove(e.getCurrentItem());
                    e.getWhoClicked().getInventory().addItem(Objects.requireNonNull(e.getCurrentItem()));
                    update(p, emitter, receiver);
                });
            }
            for (int i = 0; i < trade.getReceiverItems().size(); i++) {
                setItem(rightSlots[i], trade.getReceiverItems().get(i));
            }
        } else {
            for (int i = 0; i < trade.getReceiverItems().size(); i++) {
                setItem(leftSlots[i], trade.getReceiverItems().get(i), e -> {
                    if (trade.isReceiverReady()) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier votre mise car vous êtes prêt."));
                        return;
                    }
                    trade.getReceiverItems().remove(e.getCurrentItem());
                    e.getWhoClicked().getInventory().addItem(Objects.requireNonNull(e.getCurrentItem()));
                    update(p, emitter, receiver);
                });
            }
            for (int i = 0; i < trade.getEmitterItems().size(); i++) {
                setItem(rightSlots[i], trade.getEmitterItems().get(i));
            }
        }

        if (trade.isEmitterReady() && trade.isReceiverReady()) {
            if (trade.getEmitterReadyTime() != -1 && trade.getReceiverReadyTime() != -1) {
                long timeLeft = 5000 - (System.currentTimeMillis() - trade.getEmitterReadyTime());
                long timeLeft2 = 5000 - (System.currentTimeMillis() - trade.getReceiverReadyTime());
                if (timeLeft2 <= 0 && timeLeft <= 0) {
                    return;
                }
                long time = Math.max(timeLeft, timeLeft2);
                int seconds = (int) (time / 1000);
                for (int i = PANE_SLOTS.length - 1; i > seconds; i--) {
                    setItem(PANE_SLOTS[i], ItemBuilder.copyOf(new ItemStack(Material.GREEN_STAINED_GLASS_PANE)).name("§aEn attente...").build());
                }
            }
        } else
            setItems(PANE_SLOTS, ItemBuilder.copyOf(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)).name("§7").build());
    }

    private ItemStack getPlayerHeads(UUID emitter, double exp) {
        SkyblockUser userEmitter = SkyblockUsersManager.INSTANCE.getCachedUsers().get(emitter);
        return ItemBuilder.copyOf(new ItemStack(Material.PLAYER_HEAD)).name("§8» §f" +
                Bukkit.getOfflinePlayer(emitter).getName()).lore("§7Argent: §e" + (userEmitter == null ? "§cErreur" :
                NumberFormat.getInstance().format(userEmitter.getMoney())) + "$", "§7Points d'expérience: §e" +
                NumberFormat.getInstance().format(exp)).build();
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (Objects.equals(e.getClickedInventory(), e.getWhoClicked().getOpenInventory().getTopInventory())) {
            return;
        }
        e.setCancelled(true);
        Trade trade = TradesManager.INSTANCE.getTradeBetween(emitter, receiver);
        if (trade == null) {
            return;
        }
        boolean isEmitter = trade.getEmitter().equals(e.getWhoClicked().getUniqueId());
        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }
        if (isEmitter) {
            if (trade.getEmitterItems().size() >= MAX_PER_TRADE) {
                e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre plus de " +
                        MAX_PER_TRADE + " items."));
                return;
            }
            trade.addItem(trade.getEmitter(), item);
        } else {
            if (trade.getReceiverItems().size() >= MAX_PER_TRADE) {
                e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas mettre plus de " +
                        MAX_PER_TRADE + " items."));
                return;
            }
            trade.addItem(trade.getReceiver(), item);
        }
        e.getClickedInventory().removeItem(item);
        update(e.getWhoClicked().getUniqueId(), emitter, receiver);
    }
}
