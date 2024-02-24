package fr.farmeurimmo.coreskyblock.purpur.listeners;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.chat.ChatDisplayManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsBankManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsChatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (IslandsBankManager.INSTANCE.isAwaitingAmount(p.getUniqueId())) {
            e.setCancelled(true);
            try {
                double amount = Double.parseDouble(e.getMessage());
                if (amount <= 0) {
                    p.sendMessage(Component.text("§cVeuillez entrer un nombre positif."));
                    return;
                }
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    IslandsBankManager.INSTANCE.removeAwaitingAmount(p, true, amount);
                    return null;
                });
            } catch (NumberFormatException ex) {
                p.sendMessage(Component.text("§cVeuillez entrer un nombre valide."));
            }
            return;
        }

        if (IslandsChatManager.INSTANCE.isInIslandChat(p.getUniqueId())) {
            e.setCancelled(true);
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                IslandsChatManager.INSTANCE.sendIslandChatMessage(p, e.getMessage());
                return null;
            });
            return;
        }

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

        e.setCancelled(true);

        p.getServer().sendMessage(Component.text("§8[§b??§8] §6???? " + p.getName() + " §8» §f").append(component));
    }
}
