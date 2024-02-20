package fr.farmeurimmo.mineblock.purpur.shop.cmds;

import fr.farmeurimmo.mineblock.purpur.shop.ShopsManager;
import fr.farmeurimmo.mineblock.purpur.shop.invs.ShopInv;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShopCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("mineblock.shop.reload")) {
                    sender.sendMessage(Component.text("§cVous n'avez pas la permission de faire cela !"));
                    return false;
                }
                sender.sendMessage(Component.text("§aRechargement des shops..."));
                ShopsManager.INSTANCE.loadShops().thenRun(() ->
                                sender.sendMessage(Component.text("§aLes shops ont été rechargés !")))
                        .exceptionally(e -> {
                            e.printStackTrace();
                            sender.sendMessage(Component.text("§cUne erreur est survenue lors du rechargement des shops !"));
                            return null;
                        });
                return false;
            }
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour faire cela !"));
            return false;
        }
        new ShopInv().open(p);
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("mineblock.shop.reload")) {
            return List.of("reload");
        }
        return List.of();
    }
}
