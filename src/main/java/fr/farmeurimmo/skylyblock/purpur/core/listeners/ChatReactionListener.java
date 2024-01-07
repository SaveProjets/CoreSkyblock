package fr.farmeurimmo.skylyblock.purpur.core.listeners;

import fr.farmeurimmo.skylyblock.purpur.core.events.ChatReactionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatReactionListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent e) {
        if (ChatReactionManager.INSTANCE.isRunning) {
            ChatReactionManager.INSTANCE.end(e.getPlayer(), e.getMessage());
        }
    }
}
