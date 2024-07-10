package fr.farmeurimmo.coreskyblock.purpur.enchants;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantsAdminCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player p) {
                new EnchantsAdminInv().open(p);
                return false;
            }
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter la partie de cette commande."));
            return false;
        }
        if (args.length == 2 || args.length == 3) {
            Player target = sender.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("§cLe joueur spécifié n'est pas en ligne."));
                return false;
            }
            Enchantments enchantments;
            try {
                enchantments = Enchantments.valueOf(args[1]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("§cL'enchantement spécifié n'existe pas."));
                return false;
            }
            int level = 1;
            if (args.length == 3) {
                try {
                    level = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("§cLe niveau de l'enchantement doit être un nombre."));
                    return false;
                }
                if (level < 1 || level > enchantments.getMaxLevel()) {
                    sender.sendMessage(Component.text("§cLe niveau de l'enchantement doit être compris entre 1 et " + enchantments.getMaxLevel() + "."));
                    return false;
                }
            }
            target.getInventory().addItem(CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantments, level));
            sender.sendMessage(Component.text("§aL'enchantement a été ajouté à l'inventaire du joueur."));
            target.sendMessage(Component.text("§aVous avez reçu un livre enchanté."));
            return false;
        }
        sender.sendMessage(Component.text("§cUtilisation: /enchantsadmin <joueur> <enchantement>"));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return null;
        }
        if (args.length == 2) {
            return Arrays.stream(Enchantments.values()).map(Enum::name).filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }
        if (args.length == 3) {
            Enchantments enchantments;
            try {
                enchantments = Enchantments.valueOf(args[1]);
            } catch (IllegalArgumentException e) {
                return null;
            }
            List<String> completions = new ArrayList<>();
            for (int i = 1; i <= enchantments.getMaxLevel(); i++) {
                completions.add(String.valueOf(i));
            }
            return completions;
        }
        return List.of();
    }
}
