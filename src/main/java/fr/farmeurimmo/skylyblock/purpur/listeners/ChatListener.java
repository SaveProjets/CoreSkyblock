package fr.farmeurimmo.skylyblock.purpur.listeners;

import fr.farmeurimmo.skylyblock.purpur.chat.ChatDisplayManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(PlayerChatEvent e) {
        Player p = e.getPlayer();

        String message = e.getMessage();
        boolean item = message.contains("[item]");
        boolean money = message.contains("[money]");

        Component component = Component.text(e.getMessage());

        if (item) {
            component = component.replaceText(config -> config.matchLiteral("[item]")
                    .replacement(ChatDisplayManager.INSTANCE.getComponentForItem(p.getInventory().getItemInMainHand())));
        }
        if (money) {
            component = component.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("[money]")
                    .replacement(ChatDisplayManager.INSTANCE.getComponentForMoney(p))
                    .build());
        }

        Component result = Component.text("§8[§b??§8] §6???? " + p.getName() + " §8» §f").append(component);

        e.setCancelled(true);

        p.getServer().sendMessage(result);
    }
}
