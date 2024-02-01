package fr.farmeurimmo.skylyblock.purpur.cmds;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class NearCmd implements CommandExecutor {

    public static final long COOLDOWN = 5 * 60;
    public static final int MAX_NEAR = 50;
    private final Map<UUID, Long> cooldowns = new java.util.HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        if (!cooldowns.containsKey(p.getUniqueId()) || cooldowns.get(p.getUniqueId()) < System.currentTimeMillis()) {
            cooldowns.put(p.getUniqueId(), System.currentTimeMillis() + COOLDOWN * 1000);
        } else {
            p.sendMessage(Component.text("§cVous devez attendre encore " +
                    (cooldowns.get(p.getUniqueId()) - System.currentTimeMillis()) / 1000 +
                    " secondes avant de pouvoir réutiliser cette commande."));
            return false;
        }
        Map<Player, Double> near = new java.util.HashMap<>();
        for (Player player : p.getWorld().getPlayers()) {
            if (player == p) continue;
            double distance = player.getLocation().distance(p.getLocation());
            if (distance <= MAX_NEAR) {
                near.put(player, distance);
            }
        }
        if (near.isEmpty()) {
            p.sendMessage(Component.text("§cAucun joueur à proximité."));
            return false;
        }
        p.sendMessage(Component.text("§6Joueurs à proximité:"));
        near.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
            Player player = entry.getKey();
            double distance = entry.getValue();
            p.sendMessage(Component.text("§e" + player.getName() + " §7- §e" + distance + " §7m"));
        });
        return false;
    }
}
