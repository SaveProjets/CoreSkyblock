package fr.farmeurimmo.coreskyblock.purpur.cmds.eco;

import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BaltopCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            sender.sendMessage(Component.text("§cUsage: /baltop [page]"));
            return false;
        }
        int page = 1;
        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("§cLa page doit être un nombre."));
                return false;
            }
        }
        int finalPage = page;
        CompletableFuture.runAsync(() -> {
            LinkedHashMap<String, Double> baltop = SkyblockUsersManager.INSTANCE.getMoneyTop();
            int maxPage = (int) Math.ceil(baltop.size() / 10.0);
            if (maxPage == 0) maxPage = 1;
            if (finalPage < 1 || finalPage > 10) {
                sender.sendMessage(Component.text("§cLa page doit être comprise entre 1 et 10."));
                return;
            }
            if (finalPage > maxPage) {
                sender.sendMessage(Component.text("§cIl n'y a pas de page " + finalPage + "."));
                return;
            }
            sender.sendMessage(Component.text("§6§lClassement des plus riches:"));
            int i = 1;
            for (Map.Entry<String, Double> entry : baltop.entrySet()) {
                if (i > (finalPage - 1) * 10 && i <= finalPage * 10) {
                    sender.sendMessage(Component.text("§e" + i + ". §7" + entry.getKey() + " §7- §e" +
                            NumberFormat.getInstance().format(entry.getValue()) + " §7$"));
                }
                i++;
                if (i > finalPage * 10) break;
            }
            sender.sendMessage(Component.text("§6Page " + finalPage + "/" + maxPage));
        });
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return java.util.stream.IntStream.rangeClosed(1, (int) Math.ceil(SkyblockUsersManager.INSTANCE.getMoneyTop().size() / 10.0))
                    .mapToObj(String::valueOf)
                    .toList();
        }
        return List.of();
    }
}
