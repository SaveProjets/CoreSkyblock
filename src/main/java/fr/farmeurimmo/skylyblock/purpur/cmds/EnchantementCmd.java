package fr.farmeurimmo.skylyblock.purpur.cmds;

import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EnchantementCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        p.openEnchanting(SkylyBlock.ENCHANTING_TABLE_LOCATION, true);
        return false;
    }
}
