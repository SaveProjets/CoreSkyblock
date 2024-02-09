package fr.farmeurimmo.mineblock.purpur.featherfly;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FeatherFlyCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§cErreur, usage: /featherfly <pseudo> <temps en s, m, h, d, M, y>");
            return false;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cErreur, le joueur n'est pas connecté sur ce serveur !");
            return false;
        }
        String sample = args[1];
        //replace every non-digit char with empty string
        String time = sample.replaceAll("[^\\d.]", "");
        //replace every digit char with empty string
        String unit = sample.replaceAll("[\\d.]", "");
        if (time.isEmpty() || unit.isEmpty()) {
            sender.sendMessage("§cErreur, usage: /featherfly <pseudo> <temps en s, m, h, d, M, y>");
            return false;
        }
        int timeInt;
        try {
            timeInt = Integer.parseInt(time);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cErreur, usage: /featherfly <pseudo> <temps en s, m, h, d, M, y>");
            return false;
        }
        if (timeInt <= 0) {
            sender.sendMessage("§cErreur, le temps doit être supérieur à 0 !");
            return false;
        }
        if (unit.equalsIgnoreCase("s")) {
            //
        } else if (unit.equalsIgnoreCase("m")) {
            timeInt *= 60;
        } else if (unit.equalsIgnoreCase("h")) {
            timeInt *= 3600;
        } else if (unit.equalsIgnoreCase("d")) {
            timeInt *= 86400;
        } else if (unit.equalsIgnoreCase("M")) {
            timeInt *= 2592000;
        } else if (unit.equalsIgnoreCase("y")) {
            timeInt *= 31104000;
        }
        if (FeatherFlyManager.INSTANCE.giveFeatherFly(target, timeInt, false)) {
            sender.sendMessage("§aVous avez donné une plume de fly à " + target.getName() + " pour " + time + unit);
            target.sendMessage("§aVous avez reçu une plume de fly pour " + time + unit);
            return false;
        } else {
            sender.sendMessage("§cErreur, l'inventaire de " + target.getName() + " est plein !");
        }
        return false;
    }
}
