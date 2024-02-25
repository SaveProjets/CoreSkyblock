package fr.farmeurimmo.coreskyblock.purpur.trade;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradesManager {

    public static TradesManager INSTANCE;
    private final Map<UUID, ArrayList<UUID>> tradeRequests = new HashMap<>();
    private final ArrayList<Trade> trades = new ArrayList<>();

    public TradesManager() {
        INSTANCE = this;
    }

    public boolean isPlayerInATrade(Player player) {
        for (Trade trade : trades) {
            if (trade.getEmitter().equals(player.getUniqueId()) || trade.getReceiver().equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public void addTradeRequest(UUID emitter, UUID receiver) {
        ArrayList<UUID> receivers;
        if (tradeRequests.containsKey(emitter)) {
            receivers = tradeRequests.get(emitter);
        } else {
            receivers = new ArrayList<>();
        }
        receivers.add(receiver);
        tradeRequests.put(emitter, receivers);

        Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> {
            if (tradeRequests.containsKey(emitter)) {
                receivers.remove(receiver);
                if (receivers.isEmpty()) {
                    tradeRequests.remove(emitter);
                } else {
                    tradeRequests.put(emitter, receivers);
                }
            }
        }, 20 * 30);
    }

    public boolean hasAlreadyRequest(UUID emitter, UUID receiver) {
        if (tradeRequests.containsKey(emitter)) {
            ArrayList<UUID> receivers = tradeRequests.get(emitter);
            return receivers.contains(receiver);
        }
        return false;
    }

    public void removeTradeRequest(UUID emitter, UUID receiver) {
        if (tradeRequests.containsKey(emitter)) {
            ArrayList<UUID> receivers = tradeRequests.get(emitter);
            receivers.remove(receiver);
            if (receivers.isEmpty()) {
                tradeRequests.remove(emitter);
            } else {
                tradeRequests.put(emitter, receivers);
            }
        }
    }

    public void addTrade(Trade trade, Player emitter, Player receiver) {
        trades.add(trade);

        new TradeInv(emitter, receiver.getName(), trade).open(emitter);
        new TradeInv(receiver, emitter.getName(), trade).open(receiver);
    }

    public Trade getTradeBetween(UUID emitter, UUID receiver) {
        for (Trade trade : trades) {
            if (trade.getEmitter().equals(emitter) && trade.getReceiver().equals(receiver)) {
                return trade;
            }
        }
        return null;
    }

    public void cancelTrade(Trade trade) {
        Player emitter = Bukkit.getPlayer(trade.getEmitter());
        Player receiver = Bukkit.getPlayer(trade.getReceiver());
        if (emitter != null) {
            for (ItemStack item : trade.getEmitterItems()) {
                emitter.getInventory().addItem(item);
            }
            SkyblockUser emitter_user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(trade.getEmitter());
            emitter_user.setMoney(emitter_user.getMoney() + trade.getEmitterMoney());
        }
        if (receiver != null) {
            for (ItemStack item : trade.getReceiverItems()) {
                receiver.getInventory().addItem(item);
            }
            SkyblockUser receiver_user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(trade.getReceiver());
            receiver_user.setMoney(receiver_user.getMoney() + trade.getReceiverMoney());
        }

        trades.remove(trade);
    }

    public void endTrade(Trade trade) {
        Player emitter = Bukkit.getPlayer(trade.getEmitter());
        Player receiver = Bukkit.getPlayer(trade.getReceiver());

        if (emitter == null || receiver == null) {
            cancelTrade(trade);
            return;
        }

        trades.remove(trade);

        SkyblockUser emitter_user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(trade.getEmitter());
        SkyblockUser receiver_user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(trade.getReceiver());

        if (emitter_user.getMoney() > 0) {
            emitter_user.setMoney(emitter_user.getMoney() - trade.getEmitterMoney());
            receiver_user.setMoney(receiver_user.getMoney() + trade.getEmitterMoney());
        }
        if (receiver_user.getMoney() > 0) {
            receiver_user.setMoney(receiver_user.getMoney() - trade.getReceiverMoney());
            emitter_user.setMoney(emitter_user.getMoney() + trade.getReceiverMoney());
        }


        for (ItemStack item : trade.getReceiverItems()) {
            emitter.getInventory().addItem(item);
        }
        emitter.closeInventory();
        emitter.sendMessage(Component.text("§6§lTrade §8» §aL'échange avec §f" + receiver.getName() + " §aa été " +
                "effectué."));
        for (ItemStack item : trade.getEmitterItems()) {
            receiver.getInventory().addItem(item);
        }
        receiver.closeInventory();
        receiver.sendMessage(Component.text("§6§lTrade §8» §aL'échange avec §f" + emitter.getName() + " §aa été " +
                "effectué."));
    }

    public ArrayList<String> getNameOfPeopleWhoWantToTradeWith(Player player) {
        ArrayList<String> names = new ArrayList<>();
        for (Map.Entry<UUID, ArrayList<UUID>> entry : tradeRequests.entrySet()) {
            if (entry.getValue().contains(player.getUniqueId())) {
                Player emitter = Bukkit.getPlayer(entry.getKey());
                if (emitter != null) names.add(emitter.getName());
            }
        }
        return names;
    }

    public ArrayList<String> getNameOfPeopleThatPlayerWantedToTradeWith(Player player) {
        ArrayList<String> names = new ArrayList<>();
        for (Map.Entry<UUID, ArrayList<UUID>> entry : tradeRequests.entrySet()) {
            if (entry.getKey().equals(player.getUniqueId())) {
                for (UUID uuid : entry.getValue()) {
                    Player receiver = Bukkit.getPlayer(uuid);
                    if (receiver != null) names.add(receiver.getName());
                }
            }
        }
        return names;
    }
}
