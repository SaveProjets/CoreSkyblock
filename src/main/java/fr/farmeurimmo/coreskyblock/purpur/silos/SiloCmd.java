package fr.farmeurimmo.coreskyblock.purpur.silos;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class SiloCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("CoreSkyblock.silos.admin")) {
            sender.sendMessage(Component.text("§cVous n'avez pas la permission d'exécuter cette partie de la commande."));
            return false;
        }
        if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("§cLe joueur spécifié n'est pas en ligne."));
                return false;
            }
            Material material = Material.matchMaterial(args[1]);
            if (material == null) {
                sender.sendMessage(Component.text("§cMerci de spécifier un type de silo valide."));
                return false;
            }
            SilosType silosType = SilosType.getByMaterial(material);
            if (silosType == null) {
                sender.sendMessage(Component.text("§cLe type de silo spécifié n'existe pas."));
                return false;
            }
            target.getInventory().addItem(SilosManager.INSTANCE.createSilo(silosType));
            target.sendMessage(Component.text("§aVous avez reçu un silo de type " + silosType.getName()));
            sender.sendMessage(Component.text("§aLe joueur " + target.getName() + " a reçu un silo de type " +
                    silosType.getName() + "."));
            return false;
        }
        sender.sendMessage(Component.text("§cUsage: /silo <pseudo> <type>"));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("CoreSkyblock.silos.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return null;
        }
        if (args.length == 2) {
            return Arrays.stream(SilosType.values()).map(SilosType -> SilosType.getMaterial().name()).toList();
        }
        return List.of();
    }
}
