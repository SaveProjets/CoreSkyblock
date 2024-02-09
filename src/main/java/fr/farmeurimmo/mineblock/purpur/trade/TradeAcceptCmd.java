package fr.farmeurimmo.mineblock.purpur.trade;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TradeAcceptCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour faire cela !"));
            return false;
        }
        if (args.length != 1) {
            p.sendMessage(Component.text("§c/tradeaccept <Joueur>"));
            return false;
        }
        Player target = p.getServer().getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(Component.text("§cCe joueur n'existe pas !"));
            return false;
        }
        if (!TradesManager.INSTANCE.hasAlreadyRequest(target.getUniqueId(), p.getUniqueId())) {
            p.sendMessage(Component.text("§cVous n'avez pas de demande d'échange de la part de §a" +
                    target.getName()));
            return false;
        }
        TradesManager.INSTANCE.removeTradeRequest(target.getUniqueId(), p.getUniqueId());

        TradesManager.INSTANCE.addTrade(new Trade(target.getUniqueId(), p.getUniqueId()), target, p);
        p.sendMessage(Component.text("§6§lTrade §8» §aVous avez accepté la demande d'échange de §a" +
                target.getName() + "§f."));
        target.sendMessage(Component.text("§6§lTrade §8» §f" + p.getName() + " §fa accepté votre demande d'échange."));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String
            label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }
        if (args.length == 1) {
            return TradesManager.INSTANCE.getNameOfPeopleWhoWantToTradeWith((Player) sender);
        } else {
            return List.of();
        }
    }
}
