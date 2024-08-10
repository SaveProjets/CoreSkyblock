package fr.farmeurimmo.coreskyblock.purpur.items.sacs;

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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SacsCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("inv")) {
                if (sender instanceof Player p) {
                    new SacsAdminInv().open(p);
                } else {
                    sender.sendMessage(Component.text("§cSeul un joueur peut ouvrir cette interface."));
                }
                return false;
            }
        }
        if (args.length == 2) {
            Player target = sender.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("§cJoueur introuvable."));
                return false;
            }
            try {
                SacsType sacsType = SacsType.valueOf(args[1]);

                if (target.getInventory().firstEmpty() == -1) {
                    sender.sendMessage(Component.text("§cLe joueur n'a pas de place dans son inventaire."));
                } else {
                    target.getInventory().addItem(SacsManager.INSTANCE.createSacs(sacsType, 0));
                    sender.sendMessage(Component.text("§aLe joueur a reçu un sac de " + sacsType.getName().toLowerCase() + "."));
                }
                return false;

            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("§cType de sacs introuvable."));
                return false;
            }
        }
        sender.sendMessage(Component.text("§c/sacs <joueur> <type>"));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return CoreSkyblock.INSTANCE.getStartingBy(Stream.concat(Bukkit.getOnlinePlayers().stream().map(Player::getName),
                    Stream.of("inv")).toList(), args[0]);
        }
        if (args.length == 2) {
            return CoreSkyblock.INSTANCE.getStartingBy(Arrays.stream(SacsType.values()).map(SacsType::name).toList(), args[1]);
        }
        return List.of();
    }
}
