package fr.farmeurimmo.coreskyblock.purpur.islands.chat;

import fr.farmeurimmo.coreskyblock.common.islands.Island;
import fr.farmeurimmo.coreskyblock.common.islands.IslandRanks;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class IslandsChatManager {

    public static IslandsChatManager INSTANCE;
    private final ArrayList<UUID> inIslandChat = new ArrayList<>();

    public IslandsChatManager() {
        INSTANCE = this;
    }

    public boolean isInIslandChat(UUID uuid) {
        return inIslandChat.contains(uuid);
    }

    public void addInIslandChat(UUID uuid) {
        inIslandChat.add(uuid);
    }

    public void removeInIslandChat(UUID uuid) {
        inIslandChat.remove(uuid);
    }

    public void sendIslandChatMessage(Player p, String message) {
        Island island = IslandsManager.INSTANCE.getIslandOf(p.getUniqueId());
        if (island == null) {
            p.sendMessage(Component.text("§cErreur: Vous n'êtes pas dans une île."));
            removeInIslandChat(p.getUniqueId());
            p.chat(message);
            return;
        }
        IslandRanks ranks = island.getMembers().get(p.getUniqueId());
        island.sendMessageToAll("§6§lChat île §8» §e§l" + ranks.getName() + " §f" + p.getName() + " §8» §f" + message);
    }
}
