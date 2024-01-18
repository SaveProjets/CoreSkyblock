package fr.farmeurimmo.skylyblock.purpur.cmds;

import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BuildSpawnCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cVous devez être un joueur pour exécuter cette commande");
            return false;
        }
        if (SkylyBlock.INSTANCE.buildModePlayers.contains(p.getUniqueId())) {
            SkylyBlock.INSTANCE.buildModePlayers.remove(p.getUniqueId());
            p.sendMessage("§aVous avez quitté le mode construction");
            p.sendActionBar(Component.text());
        } else {
            SkylyBlock.INSTANCE.buildModePlayers.add(p.getUniqueId());
            p.sendMessage("§aVous avez rejoint le mode construction");
            p.sendActionBar(Component.text("§c§lVous êtes en mode construction."));
        }
        return false;
    }
}
