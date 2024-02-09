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

public class TradeCancelCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour faire cela !"));
            return false;
        }
        if (args.length != 1) {
            p.sendMessage(Component.text("§c/tradecancel <Joueur>"));
            return false;
        }
        Player target = p.getServer().getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(Component.text("§cCe joueur n'existe pas !"));
            return false;
        }
        if (!TradesManager.INSTANCE.hasAlreadyRequest(p.getUniqueId(), target.getUniqueId())) {
            p.sendMessage(Component.text("§cVous n'avez pas de demande d'échange en cours avec ce joueur !"));
            return false;
        }
        TradesManager.INSTANCE.removeTradeRequest(p.getUniqueId(), target.getUniqueId());
        p.sendMessage(Component.text("§6§lTrade §8» §fVous avez annulé votre demande d'échange avec §a" +
                target.getName()));
        target.sendMessage(Component.text("§6§lTrade §8» §a" + p.getName() + " §fa annulé sa demande d'échange " +
                "avec vous."));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String
            label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }
        if (args.length == 1) {
            return TradesManager.INSTANCE.getNameOfPeopleThatPlayerWantedToTradeWith((Player) sender);
        } else {
            return List.of();
        }
    }
}
