package fr.farmeurimmo.coreskyblock.purpur.tp.tpa.cmds;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.tp.tpa.TpasManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
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
import java.util.concurrent.CompletableFuture;

public class TpaYesCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        if (args.length != 2) {
            p.sendMessage(Component.text("§cUtilisation: /tpaccept <tpa/tpahere> <joueur>"));
            return false;
        }
        String type = args[0];
        String targetName = args[1];
        Pair<UUID, String> player = CoreSkyblock.INSTANCE.getPlayerFromName(targetName);
        if (player == null) {
            p.sendMessage(Component.text("§cUne erreur est survenue lors de la récupération des informations du joueur."));
            return false;
        }
        if (type.equalsIgnoreCase("tpa")) {
            if (!TpasManager.INSTANCE.alreadyHasTpaRequest(player.left(), p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas de demande de téléportation de la part de ce joueur."));
                return false;
            }
            TpasManager.INSTANCE.removeTpaRequest(player.left(), p.getUniqueId(), false);
            Player senderPlayer = Bukkit.getPlayer(player.left());
            if (senderPlayer != null) {
                senderPlayer.sendMessage(Component.text("§e" + p.getName() + " §7a accepté votre demande de téléportation."));
                senderPlayer.teleport(p);
                p.sendMessage(Component.text("§7Vous avez accepté la demande de téléportation de §e" + targetName + "§7."));
                return false;
            }

            TpasManager.INSTANCE.incomingPlayersTpa.put(player.left(), p.getUniqueId());
            JedisManager.INSTANCE.publishToRedis("coreskyblock", "tpa_accept:tpa:" + player.left() + ":" + p.getUniqueId() + ":" + CoreSkyblock.SERVER_NAME);
            return false;
        }
        if (type.equalsIgnoreCase("tpahere")) {
            if (!TpasManager.INSTANCE.alreadyHasTpaHereRequest(player.left(), p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas de demande de téléportation de la part de ce joueur."));
                return false;
            }
            TpasManager.INSTANCE.removeTpaRequest(player.left(), p.getUniqueId(), true);
            Player senderPlayer = Bukkit.getPlayer(player.left());
            if (senderPlayer != null) {
                senderPlayer.sendMessage(Component.text("§e" + p.getName() + " §7a accepté votre demande de téléportation."));
                p.teleport(senderPlayer);
                p.sendMessage(Component.text("§7Vous avez accepté la demande de téléportation de §e" + targetName + "§7."));
                return false;
            }
            String to = CoreSkyblock.INSTANCE.getServerNameWherePlayerIsConnected(player.left());
            if (to == null) {
                p.sendMessage(Component.text("§cUne erreur est survenue lors de la récupération du serveur du joueur."));
                return false;
            }

            CompletableFuture.runAsync(() -> JedisManager.INSTANCE.publishToRedis("coreskyblock",
                    "tpa_accept:tpahere:" + player.left() + ":" + p.getUniqueId() + ":" + CoreSkyblock.SERVER_NAME));

            p.sendMessage(Component.text("§7Vous avez accepté la demande de téléportation de §e" + targetName + "§7. Envoi à l'autre serveur..."));

            CoreSkyblock.INSTANCE.sendToServer(p, to);
            return false;
        }
        p.sendMessage(Component.text("§cUtilisation: /tpaccept <tpa/tpahere> <joueur>"));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return TpasManager.INSTANCE.getStrings(sender, args);
    }
}
