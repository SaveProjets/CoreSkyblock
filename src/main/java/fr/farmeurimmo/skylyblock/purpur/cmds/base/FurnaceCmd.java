package fr.farmeurimmo.skylyblock.purpur.cmds.base;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class FurnaceCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            p.sendMessage(Component.text("§cVous devez avoir un item dans votre main !"));
            return false;
        }
        for (@NotNull Iterator<Recipe> it = p.getServer().recipeIterator(); it.hasNext(); ) {
            Recipe r = it.next();
            if (r instanceof FurnaceRecipe fr) {
                if (fr.getInputChoice().test(item)) {
                    int amount = item.getAmount();
                    ItemStack result = fr.getResult();
                    result.setAmount(amount);
                    p.getInventory().setItemInMainHand(result);
                    p.sendMessage(Component.text("§aL'item a été cuit !"));
                    return false;
                }
            }
        }
        p.sendMessage(Component.text("§cErreur, l'item ne peut pas être cuit !"));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
