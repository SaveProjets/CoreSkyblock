package fr.farmeurimmo.skylyblock.purpur.trade;

import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    public Trade getTradeBetween(Player emitter, Player receiver) {
        Trade trade = getTradeBetween(emitter.getUniqueId(), receiver.getUniqueId());
        if (trade == null) {
            trade = getTradeBetween(receiver.getUniqueId(), emitter.getUniqueId());
        }
        return trade;
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

        Bukkit.getScheduler().runTaskLater(SkylyBlock.INSTANCE, () -> {
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

    public void endTrade(Trade trade) {
        //give items + money

        trades.remove(trade);
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
