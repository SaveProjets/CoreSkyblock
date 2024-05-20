package fr.farmeurimmo.coreskyblock.purpur.cmds.base;

import fr.farmeurimmo.coreskyblock.utils.ExperienceUtils;
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

public class XpCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        int exp = ExperienceUtils.getExp(p);
        int level = ExperienceUtils.getIntLevelFromExp(exp);
        p.sendMessage(Component.text("§e" + p.getName() + "§6 a §c" + NumberFormat.getInstance().format(exp) +
                "§6 exp (niveau §c" + level + "§6) et a besoin de §c" + NumberFormat.getInstance().format(
                p.getExperiencePointsNeededForNextLevel()) + "§6 exp pour passer au niveau suivant."));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
