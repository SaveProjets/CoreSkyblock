package fr.farmeurimmo.mineblock.purpur.cmds.base;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FeedCmd implements CommandExecutor, TabCompleter {

    public static final int COOLDOWN = 30;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande"));
            return false;
        }
        if (!cooldowns.containsKey(p.getUniqueId()) || cooldowns.get(p.getUniqueId()) <= System.currentTimeMillis()) {
            p.setFoodLevel(20);
            p.sendMessage(Component.text("§aVous avez été nourri."));
            cooldowns.put(p.getUniqueId(), System.currentTimeMillis() + (COOLDOWN * 1000));
        } else {
            p.sendMessage(Component.text("§cVous devez attendre encore " +
                    ((cooldowns.get(p.getUniqueId()) - System.currentTimeMillis()) / 1000) +
                    " seconde(s) avant de pouvoir vous nourrir."));
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
