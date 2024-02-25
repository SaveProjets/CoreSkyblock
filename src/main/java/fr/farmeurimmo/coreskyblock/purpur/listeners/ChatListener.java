package fr.farmeurimmo.coreskyblock.purpur.listeners;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.chat.ChatDisplayManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.bank.IslandsBankManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.chat.IslandsChatManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.NumberFormat;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (IslandsBankManager.INSTANCE.isAwaitingAmount(p.getUniqueId())) {
            e.setCancelled(true);
            if (e.getMessage().equalsIgnoreCase("cancel")) {
                p.sendMessage(Component.text("§cOpération annulée."));
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    IslandsBankManager.INSTANCE.removeAwaitingAmount(p, false, 0);
                    return null;
                });
                return;
            }
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
            IslandsChatManager.INSTANCE.sendIslandChatMessage(p, e.getMessage());
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

        Island island = IslandsManager.INSTANCE.getIslandOf(p.getUniqueId());
        String level = (island != null) ? "§8[§e" + NumberFormat.getInstance().format(island.getLevel()) + "§8] " : "";
        p.getServer().sendMessage(Component.text(level + "§6???? " + p.getName() + " §8» §f").append(component));
    }
}
