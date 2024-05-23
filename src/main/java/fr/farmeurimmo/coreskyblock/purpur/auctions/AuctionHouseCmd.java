package fr.farmeurimmo.coreskyblock.purpur.auctions;

import fr.farmeurimmo.coreskyblock.purpur.auctions.invs.AuctionBrowserInv;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AuctionHouseCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("Seul les joueurs peuvent exécuter cette commande."));
            return false;
        }
        if (args.length == 0) {
            new AuctionBrowserInv().open(p);
            return false;
        }
        if (args.length != 2) {
            p.sendMessage(Component.text("§cUtilisation: /ah sell <prix>"));
            return false;
        }
        if (args[0].equalsIgnoreCase("sell")) {
            try {
                double price = Double.parseDouble(args[1]);
                if (price <= 0) {
                    p.sendMessage(Component.text("§cLe prix doit être supérieur à 0."));
                    return false;
                }
                ItemStack item = p.getInventory().getItemInMainHand().clone();
                if (item.getType().isAir()) {
                    p.sendMessage(Component.text("§cVous devez tenir un objet dans votre main."));
                    return false;
                }
                if (!AuctionHouseManager.INSTANCE.canPostAnotherItem(p.getUniqueId())) {
                    p.sendMessage(Component.text("§cVous avez déjà un objet en vente."));
                    return true;
                }
                p.getInventory().setItemInMainHand(null);
                AuctionHouseManager.INSTANCE.addAuctionItem(new AuctionItem(UUID.randomUUID(), p.getUniqueId(),
                        p.getName(), price, item, System.currentTimeMillis()));
                p.sendMessage(Component.text("§aVotre objet a été mis en vente."));
            } catch (NumberFormatException e) {
                p.sendMessage(Component.text("§cLe prix doit être un nombre."));
                return false;
            }
            return false;
        }
        p.sendMessage(Component.text("§cUtilisation: /ah sell <prix>"));
        return false;
    }
}
