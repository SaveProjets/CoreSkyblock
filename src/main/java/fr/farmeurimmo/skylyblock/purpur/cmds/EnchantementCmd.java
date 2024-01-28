package fr.farmeurimmo.skylyblock.purpur.cmds;

import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EnchantementCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande"));
            return false;
        }
        Block block = SkylyBlock.SPAWN.clone().add(0, 25, 0).getBlock();
        if (block.getType().isAir()) {
            block.setType(Material.ENCHANTING_TABLE);
            p.openEnchanting(block.getLocation(), true);
            block.setType(Material.AIR);
        }
        //FIXME: Find a better way + a level 30 enchantment table
        return false;
    }
}
