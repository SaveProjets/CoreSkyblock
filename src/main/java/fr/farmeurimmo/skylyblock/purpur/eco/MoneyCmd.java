package fr.farmeurimmo.skylyblock.purpur.eco;

import fr.farmeurimmo.skylyblock.common.SkyblockUser;
import fr.farmeurimmo.skylyblock.common.SkyblockUsersManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;

public class MoneyCmd implements TabCompleter, CommandExecutor {

    private static final Component USAGE_ADMIN = Component.text("Usage: /money <player> <give|take|set> <amount>");
    private static final Component USAGE_PLAYER = Component.text("Usage: /money");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(USAGE_ADMIN);
                return false;
            }
            SkyblockUser skyblockUser = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
            if (skyblockUser == null) {
                p.sendMessage(Component.text("§cErreur lors de la récupération de votre compte !"));
                return false;
            }
            p.sendMessage(Component.text("§7Vous avez §e" + NumberFormat.getInstance().format(skyblockUser.getMoney()) + " §7$"));
            return false;
        }
        if (!sender.hasPermission("skylyblock.admin")) {
            sender.sendMessage(USAGE_PLAYER);
            return false;
        }
        if (args.length == 3) {
            String playerName = args[0];
            String action = args[1].toLowerCase();
            String amountStr = args[2];

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    sender.sendMessage(Component.text("§cLe montant doit être supérieur à 0 !"));
                    return false;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("§cErreur lors de la récupération du montant !"));
                return false;
            }

            SkyblockUser skyblockUser = SkyblockUsersManager.INSTANCE.getCachedUsers().values().stream()
                    .filter(user -> user.getName().equalsIgnoreCase(playerName)).findFirst().orElse(null);
            if (skyblockUser == null) {
                sender.sendMessage(Component.text("§cCe joueur n'existe pas !"));
                return false;
            }

            switch (action) {
                case "give":
                    skyblockUser.setMoney(skyblockUser.getMoney() + amount);
                    sender.sendMessage(Component.text("§aVous avez donné §e" + NumberFormat.getInstance()
                            .format(amount) + " §7$ à §e" + playerName));
                    //FIXME
                    break;
                case "take":
                    skyblockUser.setMoney(skyblockUser.getMoney() - amount);
                    sender.sendMessage(Component.text("§aVous avez retiré §e" + NumberFormat.getInstance()
                            .format(amount) + " §7$ à §e" + playerName));
                    //FIXME
                    break;
                case "set":
                    skyblockUser.setMoney(amount);
                    sender.sendMessage(Component.text("§aVous avez défini le solde de §e" + playerName +
                            " §7à §e" + NumberFormat.getInstance().format(amount) + " §7$"));
                    //FIXME
                    break;
                default:
                    sender.sendMessage(USAGE_ADMIN);
                    break;
            }
            return false;
        }
        sender.sendMessage(USAGE_ADMIN);
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        //FIXME
        return null;
    }
}
