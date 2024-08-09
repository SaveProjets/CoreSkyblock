package fr.farmeurimmo.coreskyblock.purpur.cmds.base;

import fr.farmeurimmo.coreskyblock.ServerType;
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

public class SpawnCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("coreskyblock.admin")) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(Component.text("§cErreur, joueur inconnu ou non connecté sur ce serveur !"));
                    return false;
                }
                target.sendActionBar(Component.text("§aTéléportation au spawn par la force..."));
                target.teleportAsync(CoreSkyblock.SPAWN).thenRun(() -> {
                    sender.sendMessage(Component.text("§aVous avez téléporté §e" + target.getName() + " §aau spawn."));
                    target.sendMessage(Component.text("§aVous avez été téléporté au spawn par §e" + sender.getName() + "§a."));
                    target.sendActionBar(Component.text("§aVous avez été téléporté au spawn."));
                }).exceptionally(throwable -> {
                    sender.sendMessage(Component.text("§cErreur lors de la téléportation au spawn !"));
                    return null;
                });
                return false;
            }
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        p.sendActionBar(Component.text("§aTéléportation au spawn..."));
        if (CoreSkyblock.SERVER_TYPE == ServerType.SPAWN) {
            p.teleportAsync(CoreSkyblock.SPAWN).thenRun(() -> p.sendActionBar(Component.text("§aVous avez été téléporté au spawn.")))
                    .exceptionally(throwable -> {
                        p.sendMessage(Component.text("§cErreur lors de la téléportation au spawn !"));
                        return null;
                    });
            return false;
        }
        String server = CoreSkyblock.INSTANCE.getASpawnServer();
        if (server == null) {
            p.sendMessage(Component.text("§cErreur, aucun serveur de spawn disponible !"));
            return false;
        }
        CoreSkyblock.INSTANCE.sendToServer(p, server);
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("coreskyblock.admin")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s -> s.startsWith(args[0])).toList();
            }
        }
        return List.of();
    }
}
