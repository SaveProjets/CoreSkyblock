package fr.farmeurimmo.coreskyblock.purpur.blocks.elevators;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ElevatorsCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            sender.sendMessage(Component.text("§cUsage: /elevator <joueur>"));
            return false;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("§cJoueur introuvable."));
            return false;
        }
        ElevatorsManager.INSTANCE.giveElevator(target);
        sender.sendMessage(Component.text("§aVous avez donné un ascenseur à " + target.getName() + "."));
        return false;
    }
}
