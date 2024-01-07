package fr.farmeurimmo.skylyblock.purpur.core.eco;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MoneyCmd implements TabCompleter, CommandExecutor {

    private static final Component USAGE_ADMIN = Component.text("Usage: /money <give|take|set> <player> <amount>");
    private static final Component USAGE_PLAYER = Component.text("Usage: /money <player>");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(USAGE_ADMIN);
                return false;
            }
            //FIXME
            p.sendMessage(Component.text("§7Vous avez §e" + 0 + " §7coins"));
            return false;
        }


        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
