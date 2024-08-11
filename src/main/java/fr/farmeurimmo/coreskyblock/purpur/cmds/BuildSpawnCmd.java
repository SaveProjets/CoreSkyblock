package fr.farmeurimmo.coreskyblock.purpur.cmds;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BuildSpawnCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        if (CoreSkyblock.INSTANCE.buildModePlayers.contains(p.getUniqueId())) {
            CoreSkyblock.INSTANCE.buildModePlayers.remove(p.getUniqueId());
            p.sendMessage(Component.text("§aVous avez quitté le mode construction"));
            p.sendActionBar(Component.text());
        } else {
            CoreSkyblock.INSTANCE.buildModePlayers.add(p.getUniqueId());
            p.sendMessage(Component.text("§aVous avez rejoint le mode construction."));
            p.sendActionBar(Component.text("§c§lVous êtes en mode construction."));
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
