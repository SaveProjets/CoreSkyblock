package fr.farmeurimmo.mineblock.purpur.minions;

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

public class MinionsCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text("§cErreur, utilisation /minions <joueur> <type>"));
            return false;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("§cErreur, joueur inconnu ou non connecté sur ce server !"));
            return false;
        }
        MinionType type = MinionType.getByName(args[1]);
        if (type == null) {
            sender.sendMessage(Component.text("§cErreur, type de minion inconnu !"));
            return false;
        }
        MinionsManager.INSTANCE.giveMinion(target, type, 1);
        sender.sendMessage(Component.text("§aVous avez donné un minion " + type.getName() + " à " + target.getName()));
        target.sendMessage(Component.text("§aVous avez reçu un minion " + type.getName() + " !"));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                                @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter(name -> name.startsWith(args[0])).toList();
        } else if (args.length == 2) {
            return Arrays.stream(MinionType.values()).map(MinionType::getName).map(String::toLowerCase)
                    .filter(name -> name.startsWith(args[1])).toList();
        }
        return null;
    }
}
