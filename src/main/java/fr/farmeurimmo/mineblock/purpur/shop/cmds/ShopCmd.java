package fr.farmeurimmo.mineblock.purpur.shop.cmds;

import fr.farmeurimmo.mineblock.purpur.shop.invs.ShopInv;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cVous devez être un joueur pour faire cela !");
            return false;
        }
        new ShopInv().open(p);
        return false;
    }
}
