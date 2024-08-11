package fr.farmeurimmo.coreskyblock.purpur.featherfly;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FeatherFlyCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text("§cErreur, usage: /featherfly <pseudo> <temps en s, m, h, d>"));
            return false;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("§cErreur, le joueur n'est pas connecté sur ce serveur !"));
            return false;
        }
        String sample = args[1];
        //replace every non-digit char with empty string
        String time = sample.replaceAll("[^\\d.]", "");
        //replace every digit char with empty string
        String unit = sample.replaceAll("[\\d.]", "");
        if (time.isEmpty() || unit.isEmpty()) {
            sender.sendMessage(Component.text("§cErreur, usage: /featherfly <pseudo> <temps en s, m, h, d, M, y>"));
            return false;
        }
        int timeInt;
        try {
            timeInt = Integer.parseInt(time);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("§cErreur, usage: /featherfly <pseudo> <temps en s, m, h, d, M, y>"));
            return false;
        }
        if (timeInt <= 0) {
            sender.sendMessage(Component.text("§cErreur, le temps doit être supérieur à 0 !"));
            return false;
        }
        if (unit.equalsIgnoreCase("m")) {
            timeInt *= 60;
        } else if (unit.equalsIgnoreCase("h")) {
            timeInt *= 3600;
        } else if (unit.equalsIgnoreCase("d")) {
            timeInt *= 86400;
        }
        if (FeatherFlyManager.INSTANCE.giveFeatherFly(target, timeInt, false)) {
            sender.sendMessage(Component.text("§aVous avez donné une plume de fly à " + target.getName() + " pour " + time + unit));
            target.sendMessage(Component.text("§aVous avez reçu une plume de fly pour " + time + unit));
            return false;
        } else {
            sender.sendMessage(Component.text("§cErreur, l'inventaire de " + target.getName() + " est plein !"));
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return CoreSkyblock.INSTANCE.getStartingBy(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[0]);
        }
        if (args.length == 2) {
            return CoreSkyblock.INSTANCE.getStartingBy(List.of(args[1] + "s", args[1] + "m", args[1] + "h", args[1] + "d"), args[1]);
        }
        return List.of();
    }
}
