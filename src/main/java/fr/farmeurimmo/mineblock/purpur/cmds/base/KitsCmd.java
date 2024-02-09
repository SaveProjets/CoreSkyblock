package fr.farmeurimmo.mineblock.purpur.cmds.base;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitsCmd implements CommandExecutor, TabCompleter {

    private static final int COOLDOWN_STEAK = 60 * 5;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        if (args.length != 1) {
            p.sendMessage(Component.text("§cUtilisation: /kits <kit>\n§cListe des kits: §e§lsteak"));
            return false;
        }
        if (args[0].equalsIgnoreCase("steak")) {
            if (!cooldowns.containsKey(p.getUniqueId())) {
                cooldowns.put(p.getUniqueId(), new HashMap<>());
            }
            if (cooldowns.get(p.getUniqueId()).containsKey("steak")) {
                long secondsLeft = ((cooldowns.get(p.getUniqueId()).get("steak") / 1000) + COOLDOWN_STEAK) - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    p.sendMessage(Component.text("§cVous devez attendre " + secondsLeft +
                            " secondes avant de pouvoir récupérer un kit steak."));
                    return false;
                }
            }
            p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 5));
            p.sendMessage(Component.text("§aVous avez reçu un kit steak."));
            cooldowns.get(p.getUniqueId()).put("steak", System.currentTimeMillis());
            return false;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return null;
        }
        return List.of("steak");
    }
}
