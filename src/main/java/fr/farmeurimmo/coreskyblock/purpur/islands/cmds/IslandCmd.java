package fr.farmeurimmo.coreskyblock.purpur.islands.cmds;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsCooldownManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsWarpManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.chat.IslandsChatManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.invs.*;
import fr.farmeurimmo.coreskyblock.purpur.islands.levels.IslandsLevelCalculator;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsMaxMembersManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.UUID;

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
            if (args[0].equalsIgnoreCase("top")) {
                new IslandsTopInv().open(p);
                return false;
            }
            p.sendMessage(USAGE_NO_IS);
            return false;
        }


        if (args.length == 0) {
            new IslandInv(island).open(p);
            return false;
        }
        if (args[0].equalsIgnoreCase("top")) {
            new IslandsTopInv().open(p);
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

        if (args[0].equalsIgnoreCase("warpbrowser")) {
            new IslandsWarpBrowserInv().open(p);
            return false;
        }

        if (island.isReadOnly()) {
            p.sendMessage(Component.text("§c§lVeuillez éditer votre île sur le serveur où elle est chargée."));
            return false;
        }

        if (args[0].equalsIgnoreCase("bank") || args[0].equalsIgnoreCase("banque")) {
            new IslandBankInv(island).open(p);
            return false;
        }

        if (args[0].equalsIgnoreCase("warp")) {
            new IslandWarpInv(island, IslandsWarpManager.INSTANCE.getByIslandUUID(island.getIslandUUID())).open(p);
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
            if (island.getBannedPlayers().contains(target.getUniqueId())) {
                p.sendMessage(Component.text("§cLe joueur est banni de l'île."));
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
            target.sendMessage(Component.text("§b[CoreSkyblock] §aVous avez été invité à rejoindre l'île de " +
                    p.getName() + ". " + "Elle expire dans 1 minute."));
            target.sendMessage(Component.text("§2[Cliquez sur ce message pour accepter l'invitation.]")
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/is accept " + p.getName()))
                    .hoverEvent(Component.text("§aAccepter l'invitation")));
            p.sendMessage(Component.text("§aLe joueur a été invité."));
            island.sendMessage("§a" + target.getName() + " a été invité à rejoindre l'île.", IslandPerms.INVITE);
            return false;
        }
        if (args[0].equalsIgnoreCase("cancelinvite") || args[0].equalsIgnoreCase("annulerinvite")) {
            if (args.length != 2) {
                p.sendMessage(Component.text("§cUtilisation: /is cancelinvite <joueur>"));
                return false;
            }
            if (!island.hasPerms(rank, IslandPerms.CANCEL_INVITE, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'annuler des invitations."));
                return false;
            }
            OfflinePlayer target = p.getServer().getOfflinePlayer(args[1]);
            if (!island.isInvited(target.getUniqueId())) {
                p.sendMessage(Component.text("§cLe joueur n'a pas été invité."));
                return false;
            }
            island.removeInvite(target.getUniqueId());
            p.sendMessage(Component.text("§aL'invitation a été annulée."));
            island.sendMessage("§aL'invitation de " + target.getName() + " a été annulée.", IslandPerms.CANCEL_INVITE);
            return false;
        }
        if (args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("expulser")) {
            if (args.length != 2) {
                p.sendMessage(Component.text("§cUtilisation: /is kick <joueur>"));
                return false;
            }
            if (!island.hasPerms(rank, IslandPerms.KICK, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'expulser des joueurs."));
                return false;
            }
            OfflinePlayer target = p.getServer().getOfflinePlayer(args[1]);
            if (!island.getMembers().containsKey(target.getUniqueId())) {
                p.sendMessage(Component.text("§cLe joueur n'est pas membre de l'île."));
                return false;
            }
            if (rank.getId() >= island.getMembers().get(target.getUniqueId()).getId()) {
                p.sendMessage(Component.text("§cVous ne pouvez pas expulser un joueur de ce grade ou supérieur."));
                return false;
            }
            island.removeMember(target.getUniqueId());
            p.sendMessage(Component.text("§aLe joueur a été expulsé."));
            island.sendMessage("§a" + target.getName() + " a été expulsé de l'île.", IslandPerms.KICK);
            return false;
        }
        if (args[0].equalsIgnoreCase("expel")) {
            if (args.length != 2) {
                p.sendMessage(Component.text("§cUtilisation: /is expel <joueur>"));
                return false;
            }
            if (!island.hasPerms(rank, IslandPerms.EXPEL, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'expulser des visiteurs."));
                return false;
            }
            Player target = p.getServer().getPlayer(args[1]);
            if (target == null) {
                p.sendMessage(Component.text("§cLe joueur n'est pas en ligne."));
                return false;
            }
            if (!target.getWorld().getName().equals(IslandsManager.INSTANCE.getIslandWorldName(island.getIslandUUID()))) {
                p.sendMessage(Component.text("§cLe joueur n'est pas sur l'île."));
                return false;
            }
            if (island.getMembers().containsKey(target.getUniqueId())) {
                p.sendMessage(Component.text("§cLe joueur est membre de l'île."));
                return false;
            }
            target.teleportAsync(CoreSkyblock.SPAWN).thenRun(() ->
                    target.sendMessage(Component.text("§cVous avez été expulsé du mode visiteur de l'île.")));
            p.sendMessage(Component.text("§aLe visiteur a été expulsé."));
            island.sendMessage("§a" + target.getName() + " a été expulsé de l'île.", IslandPerms.EXPEL);
            return false;
        }
        if (args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("bannir")) {
            if (args.length != 2) {
                p.sendMessage(Component.text("§cUtilisation: /is ban <joueur>"));
                return false;
            }
            if (!island.hasPerms(rank, IslandPerms.BAN, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission de bannir des joueurs."));
                return false;
            }
            OfflinePlayer target = p.getServer().getOfflinePlayer(args[1]);
            if (island.getMembers().containsKey(target.getUniqueId())) {
                p.sendMessage(Component.text("§cLe joueur est membre de l'île."));
                return false;
            }
            if (island.getBannedPlayers().contains(target.getUniqueId())) {
                p.sendMessage(Component.text("§cLe joueur est déjà banni."));
                return false;
            }
            island.addBannedPlayer(target.getUniqueId());
            p.sendMessage(Component.text("§aLe visiteur a été banni."));
            island.sendMessage("§a" + target.getName() + " a été banni de l'île.", IslandPerms.BAN);

            if (target.getPlayer() != null) target.getPlayer().teleportAsync(CoreSkyblock.SPAWN).thenRun(() ->
                    target.getPlayer().sendMessage(Component.text("§cVous avez été banni du mode visiteur de l'île.")));
            return false;
        }
        if (args[0].equalsIgnoreCase("unban") || args[0].equalsIgnoreCase("debannir")) {
            if (args.length != 2) {
                p.sendMessage(Component.text("§cUtilisation: /is unban <joueur>"));
                return false;
            }
            if (!island.hasPerms(rank, IslandPerms.UNBAN, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission de débannir des joueurs."));
                return false;
            }
            OfflinePlayer target = p.getServer().getOfflinePlayer(args[1]);
            if (!island.getBannedPlayers().contains(target.getUniqueId())) {
                p.sendMessage(Component.text("§cLe joueur n'est pas banni."));
                return false;
            }
            island.removeBannedPlayer(target.getUniqueId());
            p.sendMessage(Component.text("§aLe joueur a été débanni."));
            island.sendMessage("§a" + target.getName() + " a été débanni de l'île.", IslandPerms.UNBAN);
            if (target.getPlayer() != null) target.getPlayer().sendMessage(Component.text(
                    "§aVous avez été débanni du mode visiteur de l'île " + island.getName() + "."));
            return false;
        }
        if (args[0].equalsIgnoreCase("private") || args[0].equalsIgnoreCase("privée")) {
            if (!island.hasPerms(rank, IslandPerms.PRIVATE, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission de changer la visibilité de l'île."));
                return false;
            }
            if (!island.isPublic()) {
                p.sendMessage(Component.text("§cVotre île est déjà privée."));
                return false;
            }
            long cooldownLeft = IslandsCooldownManager.INSTANCE.getCooldownLeft(island.getIslandUUID(), "island-accessibility");
            if (cooldownLeft >= 0) {
                p.sendMessage(Component.text("§cVous devez attendre " + cooldownLeft + " secondes avant de pouvoir " +
                        "changer la visibilité de l'île."));
                return false;
            }
            IslandsCooldownManager.INSTANCE.addCooldown(island.getIslandUUID(), "island-accessibility");
            island.setPublic(false);
            p.sendMessage(Component.text("§aVotre île est désormais §cprivée§a."));
            island.sendMessageToAll("§aL'île est désormais §cprivée§a.");
            return false;
        }
        if (args[0].equalsIgnoreCase("public") || args[0].equalsIgnoreCase("publique")) {
            if (!island.hasPerms(rank, IslandPerms.PUBLIC, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission de changer la visibilité de l'île."));
                return false;
            }
            if (island.isPublic()) {
                p.sendMessage(Component.text("§cVotre île est déjà publique."));
                return false;
            }
            long cooldownLeft = IslandsCooldownManager.INSTANCE.getCooldownLeft(island.getIslandUUID(), "island-accessibility");
            if (cooldownLeft >= 0) {
                p.sendMessage(Component.text("§cVous devez attendre " + cooldownLeft + " secondes avant de pouvoir " +
                        "changer la visibilité de l'île."));
                return false;
            }
            IslandsCooldownManager.INSTANCE.addCooldown(island.getIslandUUID(), "island-accessibility");
            island.setPublic(true);
            p.sendMessage(Component.text("§aVotre île est désormais §2publique§a."));
            island.sendMessageToAll("§aL'île est désormais §2publique§a.");
            return false;
        }
        if (args[0].equalsIgnoreCase("level")) {
            if (!island.hasPerms(rank, IslandPerms.CALCULATE_ISLAND_LEVEL, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission de calculer le niveau de l'île."));
                return false;
            }
            long cooldownLeft = IslandsCooldownManager.INSTANCE.getCooldownLeft(island.getIslandUUID(),
                    "island-calculation-of-level");
            if (cooldownLeft >= 0) {
                p.sendMessage(Component.text("§cVous devez attendre " + cooldownLeft + " secondes avant de pouvoir " +
                        "calculer le niveau de l'île."));
                return false;
            }
            IslandsCooldownManager.INSTANCE.addCooldown(island.getIslandUUID(), "island-calculation-of-level");
            float level = island.getLevel();
            if (level != 0) {
                p.sendMessage(Component.text("§aNiveau de l'île: §6" + NumberFormat.getInstance().format(level) + "."));
            } else {
                p.sendMessage(Component.text("§cLe niveau de l'île n'a pas encore été calculé, veuillez patienter."));
            }
            IslandsLevelCalculator.INSTANCE.calculateIslandLevel(island, p.getUniqueId());
            return false;
        }
        if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("supprimer")) {
            if (rank.getId() != 0) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission de supprimer l'île."));
                return false;
            }
            if (!IslandsManager.INSTANCE.getDeleteConfirmation().contains(p.getUniqueId())) {
                IslandsManager.INSTANCE.getDeleteConfirmation().add(p.getUniqueId());
                p.sendMessage(Component.text("§cÊtes-vous sûr de vouloir supprimer l'île ? " +
                        "Tapez §a/is delete§c à nouveau pour confirmer."));
                Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> {
                    if (IslandsManager.INSTANCE.getDeleteConfirmation().contains(p.getUniqueId())) {
                        IslandsManager.INSTANCE.getDeleteConfirmation().remove(p.getUniqueId());
                        p.sendMessage(Component.text("§cLa confirmation de suppression a expiré."));
                    }
                }, 20 * 10);
                return false;
            }
            IslandsManager.INSTANCE.getDeleteConfirmation().remove(p.getUniqueId());
            IslandsManager.INSTANCE.deleteIsland(island);
            p.sendMessage(Component.text("§aL'île a été supprimée."));
            return false;
        }
        if (args[0].equalsIgnoreCase("sethome")) {
            if (!island.hasPerms(rank, IslandPerms.SET_HOME, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission de définir le point de spawn de l'île."));
                return false;
            }
            long cooldownLeft = IslandsCooldownManager.INSTANCE.getCooldownLeft(island.getIslandUUID(), "island-set-home");
            if (cooldownLeft >= 0) {
                p.sendMessage(Component.text("§cVous devez attendre " + cooldownLeft + " secondes avant de pouvoir " +
                        "définir le point de spawn de l'île."));
                return false;
            }
            IslandsCooldownManager.INSTANCE.addCooldown(island.getIslandUUID(), "island-set-home");
            island.setSpawn(p.getLocation());
            p.sendMessage(Component.text("§aLe point de spawn de l'île a été redéfini."));
            island.sendMessageToAll("§aLe point de spawn de l'île a été redéfini.");
            return false;
        }
        if (args[0].equalsIgnoreCase("setname")) {
            if (!island.hasPerms(rank, IslandPerms.SET_ISLAND_NAME, p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission de changer le nom de l'île."));
                return false;
            }
            if (args.length < 2) {
                p.sendMessage(Component.text("§cUtilisation: /is setname <nom>"));
                return false;
            }
            long cooldownLeft = IslandsCooldownManager.INSTANCE.getCooldownLeft(island.getIslandUUID(), "island-set-name");
            if (cooldownLeft >= 0) {
                p.sendMessage(Component.text("§cVous devez attendre " + cooldownLeft + " secondes avant de pouvoir " +
                        "changer le nom de l'île."));
                return false;
            }
            IslandsCooldownManager.INSTANCE.addCooldown(island.getIslandUUID(), "island-set-name");
            String name = String.join(" ", args).substring(8).replaceAll("[^a-zA-Z0-9&]", "");
            if (name.length() > 32) {
                p.sendMessage(Component.text("§cLe nom de l'île ne peut pas dépasser 32 caractères."));
                return false;
            }
            if (name.equals(island.getName())) {
                p.sendMessage(Component.text("§cLe nom de l'île est déjà " + name + "."));
                return false;
            }
            island.setName(name);
            p.sendMessage(Component.text("§aLe nom de l'île a été changé."));
            island.sendMessageToAll("§aLe nom de l'île a été changé en §6" + name + "§a.");
            return false;
        }
        if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("quitter") ||
                args[0].equalsIgnoreCase("quit") || args[0].equalsIgnoreCase("partir")) {
            if (island.getOwnerUUID().equals(p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous ne pouvez pas quitter l'île car vous en êtes le propriétaire."));
                return false;
            }
            island.removeMember(p.getUniqueId());
            p.sendMessage(Component.text("§aVous avez quitté l'île."));
            island.sendMessageToAll("§a" + p.getName() + " a quitté l'île.");
            return false;
        }
        if (args[0].equalsIgnoreCase("makeleader") || args[0].equalsIgnoreCase("leader")) {
            if (args.length != 2) {
                p.sendMessage(Component.text("§cUtilisation: /is makeleader <joueur>"));
                return false;
            }
            if (!island.getOwnerUUID().equals(p.getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'êtes pas propriétaire de l'île."));
                return false;
            }
            UUID targetUUID = island.getMemberUUIDFromName(args[1]);
            if (targetUUID == null) {
                p.sendMessage(Component.text("§cLe joueur n'est pas membre de l'île."));
                return false;
            }
            if (!island.getMembers().containsKey(targetUUID)) {
                p.sendMessage(Component.text("§cLe joueur est déjà membre de l'île."));
                return false;
            }
            island.changeOwner(targetUUID);
            p.sendMessage(Component.text("§aLe propriétaire de l'île a été changé."));
            island.sendMessageToAll("§aLe propriétaire de l'île a été changé pour " + args[1] + ".");
            Player target = p.getServer().getPlayer(targetUUID);
            if (target != null) {
                target.sendMessage(Component.text("§aVous êtes désormais propriétaire de l'île."));
            }
            return false;
        }

        return false;
    }
}
