package fr.farmeurimmo.coreskyblock.purpur.enchants;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (args.length == 2) {
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
            target.getInventory().addItem(CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantments, enchantments.getMaxLevel()));
            sender.sendMessage(Component.text("§aL'enchantement a été ajouté à l'inventaire du joueur."));
            target.sendMessage(Component.text("§aVous avez reçu un livre enchanté."));
            return false;
        }
        sender.sendMessage(Component.text("§cUtilisation: /enchantsadmin <joueur> <enchantement>"));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
