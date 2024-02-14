package fr.farmeurimmo.mineblock.purpur.chests;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ChestsCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text("§cErreur, utilisation /chests <joueur> <type>"));
            return false;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("§cErreur, joueur inconnu"));
            return false;
        }
        ChestType type = ChestType.getByName(args[1]);
        if (type == null) {
            sender.sendMessage(Component.text("§cErreur, coffre/hoppeur inconnu"));
            return false;
        }
        ChestsManager.INSTANCE.giveItem(target, type);
        sender.sendMessage(Component.text("§aVous avez donné un " + type.getName() + "§a à §e" + target.getName() + "§a."));
        target.sendMessage(Component.text("§aVous avez reçu un " + type.getName() + "§a de la part de §e" + sender.getName() + "§a."));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter(name -> name.startsWith(args[0])).toList();
        } else if (args.length == 2) {
            return Arrays.stream(ChestType.values()).map(ChestType::getNameWithoutColor)
                    .filter(name -> name.startsWith(args[1])).toList();
        }
        return null;
    }
}
