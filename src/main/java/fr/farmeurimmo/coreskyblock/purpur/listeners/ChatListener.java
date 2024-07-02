package fr.farmeurimmo.coreskyblock.purpur.listeners;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.chat.ChatDisplayManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsCooldownManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsTopManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsWarpManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.bank.IslandsBankManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.chat.IslandsChatManager;
import fr.farmeurimmo.coreskyblock.purpur.prestige.PrestigesManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
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

        Island island = IslandsManager.INSTANCE.getIslandOf(p.getUniqueId());

        if (IslandsWarpManager.INSTANCE.isAwaitingInput(p.getUniqueId())) {
            e.setCancelled(true);
            if (e.getMessage().equalsIgnoreCase("cancel")) {
                p.sendMessage(Component.text("§cOpération annulée."));
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    IslandsWarpManager.INSTANCE.removeAwaitingInput(p.getUniqueId());
                    return null;
                });
                return;
            }
            String input = e.getMessage();
            if (input.length() < 4) {
                p.sendMessage(Component.text("§cLe nom doit contenir au moins 4 caractères."));
                return;
            }
            if (IslandsWarpManager.INSTANCE.isAwaitingLongString(p.getUniqueId())) {
                if (input.length() > 300) {
                    p.sendMessage(Component.text("§cLe nom doit contenir moins de 256 caractères."));
                    return;
                }
            } else {
                if (input.length() > 32) {
                    p.sendMessage(Component.text("§cLe nom doit contenir moins de 32 caractères."));
                    return;
                }
            }
            if (input.replace("\\n", "\n").split("\n").length > 12) {
                p.sendMessage(Component.text("§cMaximum 12 lignes."));
                return;
            }
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                IslandsWarpManager.INSTANCE.processInput(p, input);
                return null;
            });
            return;
        }

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
                    if (island != null)
                        IslandsCooldownManager.INSTANCE.addCooldown(island.getIslandUUID(), "island-bank");
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

        Component component = Component.text((p.hasPermission("coreskyblock.chat.color") ? e.getMessage().replace("&", "§") : e.getMessage()));

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

        SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
        if (user == null) {
            p.sendMessage(Component.text("§cUne erreur est survenue. Veuillez réessayer."));
            return;
        }

        if (island != null) {
            int islandRanking = IslandsTopManager.INSTANCE.getPosition(island.getIslandUUID(), 0);
            String level = (islandRanking > 0) ? "§8[§e#" + NumberFormat.getInstance().format(IslandsTopManager.INSTANCE.getPosition(island.getIslandUUID(), 0)) + "§8] " : "";
            String prestigeLevel = (user.getCurrentPrestigeLevel() > 0) ? PrestigesManager.INSTANCE.getColorCode(user.getLastPrestigeLevelClaimed()) +
                    NumberFormat.getInstance().format(user.getLastPrestigeLevelClaimed()) + "§l✨ " : "";
            p.getServer().sendMessage(Component.text(prestigeLevel + level + "§6???? " + p.getName() + " §8» §f").append(component));
        } else {
            String prestigeLevel = (user.getCurrentPrestigeLevel() > 0) ? PrestigesManager.INSTANCE.getColorCode(user.getLastPrestigeLevelClaimed()) +
                    NumberFormat.getInstance().format(user.getLastPrestigeLevelClaimed()) + "§l✨ " : "";
            p.getServer().sendMessage(Component.text(prestigeLevel + "§6???? " + p.getName() + " §8» §f").append(component));
        }
    }
}
