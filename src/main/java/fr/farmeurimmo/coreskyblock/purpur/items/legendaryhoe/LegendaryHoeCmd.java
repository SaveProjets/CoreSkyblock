package fr.farmeurimmo.coreskyblock.purpur.items.legendaryhoe;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class LegendaryHoeCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1 && args.length != 2) {
            sender.sendMessage(Component.text("§cUsage: /legendaryhoe <joueur> [énergie]"));
            return false;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("§cCe joueur n'est pas connecté."));
            return false;
        }
        int energy = 0;
        if (args.length == 2) {
            try {
                energy = Integer.parseInt(args[1]);
                if (energy < 0) {
                    sender.sendMessage(Component.text("§cL'énergie doit être positive."));
                    return false;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("§cL'énergie doit être un nombre."));
                return false;
            }
        }
        if (target.getInventory().firstEmpty() == -1) {
            sender.sendMessage(Component.text("§cLe joueur n'a pas de place dans son inventaire."));
        } else {
            target.getInventory().addItem(LegendaryHoeManager.INSTANCE.createLegendaryHoe(energy));
            sender.sendMessage(Component.text("§aLe joueur a reçu une houe légendaire avec " +
                    NumberFormat.getInstance().format(energy) + " points d'énergie."));
        }
        return false;
    }
}
