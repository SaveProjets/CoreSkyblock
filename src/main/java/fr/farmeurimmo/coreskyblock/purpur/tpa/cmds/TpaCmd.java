package fr.farmeurimmo.coreskyblock.purpur.tpa.cmds;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.tpa.TpasManager;
import fr.farmeurimmo.coreskyblock.utils.DateUtils;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TpaCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        if (args.length != 1) {
            p.sendMessage(Component.text("§cUtilisation: /tpa <joueur>"));
            return false;
        }
        String targetName = args[0];
        if (targetName.equalsIgnoreCase(p.getName())) {
            p.sendMessage(Component.text("§cVous ne pouvez pas vous téléporter à vous-même."));
            return false;
        }
        if (!CoreSkyblock.INSTANCE.isPlayerConnected(targetName)) {
            p.sendMessage(Component.text("§cCe joueur n'est pas connecté, veuillez réessayer dans quelques instants."));
            return false;
        }
        Pair<UUID, String> player = CoreSkyblock.INSTANCE.getPlayerFromName(targetName);
        if (player == null) {
            p.sendMessage(Component.text("§cUne erreur est survenue lors de la récupération des informations du joueur."));
            return false;
        }
        if (TpasManager.INSTANCE.alreadyHasTpaRequest(p.getUniqueId(), player.left())) {
            p.sendMessage(Component.text("§cVous avez déjà envoyé une demande de téléportation à ce joueur. Elle expirera dans " +
                    DateUtils.getFormattedTimeLeft((int) (TpasManager.INSTANCE.getTpaRequestExpireTime(p.getUniqueId(), player.left()) / 1000)) + " secondes."));
            return false;
        }
        TpasManager.INSTANCE.createTpaRequest(p.getUniqueId(), p.getName(), player.left(), player.right(), false);
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            target.sendMessage(TpasManager.INSTANCE.getTpaComponent(p.getName()));
        }
        p.sendMessage(Component.text("§7Votre demande de téléportation a été envoyée à §e" + targetName + "§7."));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return CoreSkyblock.INSTANCE.getSuggestions(args[0], sender.getName());
        return List.of();
    }
}
