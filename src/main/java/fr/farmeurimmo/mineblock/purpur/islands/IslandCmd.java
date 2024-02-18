package fr.farmeurimmo.mineblock.purpur.islands;

import fr.farmeurimmo.mineblock.common.islands.Island;
import fr.farmeurimmo.mineblock.common.islands.IslandPerms;
import fr.farmeurimmo.mineblock.common.islands.IslandRanks;
import fr.farmeurimmo.mineblock.purpur.islands.invs.IslandInv;
import fr.farmeurimmo.mineblock.purpur.islands.upgrades.IslandsMaxMembersManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class IslandCmd implements CommandExecutor {

    private static final Component USAGE_NO_IS = Component.text("§cUtilisation: /is create OU /is join <joueur> " +
            "tout en possédant une invitation.");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        Island island = IslandsManager.INSTANCE.getIslandOf(p.getUniqueId());
        if (island == null) {
            if (args.length == 0) {
                p.sendMessage(USAGE_NO_IS);
                return false;
            }
            if (args[0].equalsIgnoreCase("create")) {
                IslandsManager.INSTANCE.createIsland(p.getUniqueId());
                return false;
            }
            if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("join")) {
                if (args.length != 2) {
                    p.sendMessage(Component.text("§cUtilisation: /is accept <joueur>"));
                    return false;
                }
                OfflinePlayer target = p.getServer().getOfflinePlayer(args[1]);
                Island targetIsland = IslandsManager.INSTANCE.getIslandOf(target.getUniqueId());
                if (targetIsland == null) {
                    p.sendMessage(Component.text("§cLe joueur n'a pas d'île."));
                    return false;
                }
                if (!targetIsland.isInvited(p.getUniqueId())) {
                    p.sendMessage(Component.text("§cVous n'avez pas été invité par ce joueur."));
                    return false;
                }
                if (IslandsMaxMembersManager.INSTANCE.isFull(targetIsland.getMaxMembers(),
                        targetIsland.getMembers().size())) {
                    p.sendMessage(Component.text("§cL'île est pleine."));
                    return false;
                }
                targetIsland.removeInvite(p.getUniqueId());
                targetIsland.addMember(p.getUniqueId(), p.getName(), IslandRanks.MEMBRE);
                p.sendMessage(Component.text("§aVous avez rejoint l'île de " + args[1] + "."));
                targetIsland.sendMessageToAll("§a" + p.getName() + " a rejoint l'île.");
                return false;
            }
            p.sendMessage(USAGE_NO_IS);
            return false;
        }


        if (args.length == 0) {
            new IslandInv(island).open(p);
            return false;
        }
        if (args[0].equalsIgnoreCase("go")) {
            IslandsManager.INSTANCE.teleportToIsland(island, p);
            return false;
        }
        if (args[0].equalsIgnoreCase("chat")) {
            if (IslandsChatManager.INSTANCE.isInIslandChat(p.getUniqueId())) {
                IslandsChatManager.INSTANCE.removeInIslandChat(p.getUniqueId());
                p.sendMessage(Component.text("§aVos messages seront désormais envoyés dans le chat §6général§a."));
            } else {
                IslandsChatManager.INSTANCE.addInIslandChat(p.getUniqueId());
                p.sendMessage(Component.text("§aVos messages seront désormais envoyés dans le §6chat de l'île§a."));
            }
            return false;
        }
        if (args[0].equalsIgnoreCase("private") || args[0].equalsIgnoreCase("privée")) {
            if (!island.isPublic()) {
                p.sendMessage(Component.text("§cVotre île est déjà privée."));
                return false;
            }
            island.setPublic(false);
            p.sendMessage(Component.text("§aVotre île est désormais §cprivée§a."));
            island.sendMessageToAll("§aL'île est désormais §cprivée§a.");
            return false;
        }
        if (args[0].equalsIgnoreCase("public") || args[0].equalsIgnoreCase("publique")) {
            if (island.isPublic()) {
                p.sendMessage(Component.text("§cVotre île est déjà publique."));
                return false;
            }
            island.setPublic(true);
            p.sendMessage(Component.text("§aVotre île est désormais §2publique§a."));
            island.sendMessageToAll("§aL'île est désormais §2publique§a.");
            return false;
        }

        IslandRanks rank = island.getMembers().get(p.getUniqueId());
        if (rank == null) {
            p.sendMessage(Component.text("§cVous n'êtes pas membre de cette île."));
            return false;
        }

        if (args[0].equalsIgnoreCase("invite")) {
            if (args.length != 2) {
                p.sendMessage(Component.text("§cUtilisation: /is invite <joueur>"));
                return false;
            }
            if (!island.hasPerms(rank, IslandPerms.INVITE, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'inviter des joueurs."));
                return false;
            }
            Player target = p.getServer().getPlayer(args[1]);
            if (target == null) {
                p.sendMessage(Component.text("§cLe joueur n'est pas en ligne."));
                return false;
            }
            if (island.getMembers().containsKey(target.getUniqueId())) {
                p.sendMessage(Component.text("§cLe joueur est déjà membre de l'île."));
                return false;
            }
            if (IslandsMaxMembersManager.INSTANCE.isFull(island.getMaxMembers(), island.getMembers().size())) {
                p.sendMessage(Component.text("§cL'île est pleine."));
                return false;
            }
            if (island.isInvited(target.getUniqueId())) {
                p.sendMessage(Component.text("§cLe joueur a déjà été invité."));
                return false;
            }
            island.addInvite(target.getUniqueId());
            target.sendMessage(Component.text("§b[MineBlock] §aVous avez été invité à rejoindre l'île de " +
                    p.getName() + ". " + "Elle expire dans 1 minute."));
            target.sendMessage(Component.text("§2[Cliquez sur ce message pour accepter l'invitation.]")
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/is accept " + p.getName()))
                    .hoverEvent(Component.text("§aAccepter l'invitation")));
            p.sendMessage(Component.text("§aLe joueur a été invité."));
            island.sendMessage("§a" + target.getName() + " a été invité à rejoindre l'île.", IslandPerms.INVITE);
            return false;
        }
        return false;
    }
}
