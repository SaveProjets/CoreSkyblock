package fr.farmeurimmo.skylyblock.purpur.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(PlayerChatEvent e) {
        Player p = e.getPlayer();

        String message = e.getMessage();
        String formatted = "§8[§b??§8] §6???? " + p.getName() + " §8» §f" + message;

        e.setFormat(formatted);
    }
}
