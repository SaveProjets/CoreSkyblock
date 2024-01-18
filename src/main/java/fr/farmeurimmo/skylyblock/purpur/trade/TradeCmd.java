package fr.farmeurimmo.skylyblock.purpur.trade;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TradeCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour faire cela !"));
            return false;
        }
        if (args.length != 1) {
            p.sendMessage(Component.text("§c/trade <Joueur>"));
            return false;
        }
        Player target = p.getServer().getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(Component.text("§cCe joueur n'existe pas !"));
            return false;
        }
        if (TradesManager.INSTANCE.hasAlreadyRequest(p.getUniqueId(), target.getUniqueId())) {
            p.sendMessage(Component.text("§cVous avez déjà une demande en cours, §cannulez §fla avec §c/tradecancel"
                    + " §fOU §cattendez 30 secondes §fpour pouvoir en relancer une."));
            return false;
        }
        if (target.getName().equalsIgnoreCase(p.getName())) {
            p.sendMessage(Component.text("§cVous ne pouvez pas échanger à vous même."));
            return false;
        }
        TradesManager.INSTANCE.addTradeRequest(p.getUniqueId(), target.getUniqueId());
        p.sendMessage(Component.text("§6§lTrade §8» §fVous avez envoyé une demande d'échange à §a" +
                target.getName() + "§f."));

        Component clickToAccept = Component.text("§aCliquez pour accepter l'échange.")
                .hoverEvent(HoverEvent.showText(Component.text("§7Cliquez pour accepter l'échange.")))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tradeaccept " + p.getName()));

        Component clickToDeny = Component.text("§cCliquez pour refuser l'échange.")
                .hoverEvent(HoverEvent.showText(Component.text("§7Cliquez pour refuser l'échange.")))
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tradedeny " + p.getName()));

        target.sendMessage(Component.text("§6§lTrade §8» §a" + p.getName() + " §fveut échanger avec vous.")
                .append(Component.newline())
                .append(clickToAccept)
                .append(Component.text(" §fOU "))
                .append(clickToDeny));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                                @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter(name -> name.startsWith(args[0])
                    && !name.equalsIgnoreCase(sender.getName())).toList();
        }
        return List.of();
    }
}
