package fr.farmeurimmo.coreskyblock.purpur.shop.cmds;

import fr.farmeurimmo.coreskyblock.common.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.common.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.coreskyblock.purpur.shop.ShopsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class SellAllCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour faire cela !"));
            return false;
        }
        SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
        if (user == null) {
            sender.sendMessage(Component.text("§cUne erreur est survenue, veuillez réessayer plus tard."));
            return false;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            sender.sendMessage(Component.text("§cVous devez avoir un item dans votre main !"));
            return false;
        }
        double sellPrice = ShopsManager.INSTANCE.getSellPrice(item);
        if (sellPrice <= 0) {
            sender.sendMessage(Component.text("§cVous ne pouvez pas vendre cet item !"));
            return false;
        }
        int amount = ShopsManager.INSTANCE.getAmountOf(p, item);
        if (amount <= 0) {
            sender.sendMessage(Component.text("§cVous n'avez pas cet item dans votre inventaire !"));
            return false;
        }
        double total = sellPrice * amount;
        user.addMoney(total);
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack itemStack = p.getInventory().getItem(i);
            if (itemStack == null) continue;
            if (itemStack.isSimilar(item)) {
                p.getInventory().setItem(i, null);
            }
        }
        p.sendMessage(Component.text("§aVous avez vendu §e" + amount + " " + item.getType().name() +
                "§a pour §e" + NumberFormat.getInstance().format(total) + "$§a."));
        return false;
    }
}
