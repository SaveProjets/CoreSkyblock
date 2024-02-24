package fr.farmeurimmo.coreskyblock.purpur.cmds;

import fr.farmeurimmo.coreskyblock.utils.InventorySyncUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventorySyncCmd implements CommandExecutor {

    private final Map<UUID, String> inventorySync = new HashMap<>();

    //COMMANDE ADMIN TEMPORAIRE POUR SYNCHRONISER L'INVENTAIRE D'UN JOUEUR

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        if (!inventorySync.containsKey(p.getUniqueId())) {
            inventorySync.put(p.getUniqueId(), InventorySyncUtils.INSTANCE.inventoryToJson(p.getInventory()));
            p.sendMessage(Component.text("§aInventaire synchronisé avec succès."));
            return true;
        }
        System.out.println(inventorySync.get(p.getUniqueId()));
        p.getInventory().clear();
        p.getInventory().setContents(InventorySyncUtils.INSTANCE.jsonToInventory(inventorySync.get(p.getUniqueId()), p.getInventory().getSize()));
        p.sendMessage(Component.text("§aInventaire synchronisé avec succès."));
        inventorySync.remove(p.getUniqueId());
        return false;
    }
}
