package fr.farmeurimmo.coreskyblock.purpur.islands.bank;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandsBankManager {

    public static IslandsBankManager INSTANCE;
    private final Map<UUID, Boolean> awaitingAmounts = new HashMap<>();

    public IslandsBankManager() {
        INSTANCE = this;
    }

    public void addAwaitingAmount(UUID uuid, boolean deposit) {
        awaitingAmounts.put(uuid, deposit);
    }

    public void removeAwaitingAmount(Player p, boolean completed, double amount) {
        final boolean deposit = awaitingAmounts.get(p.getUniqueId());

        awaitingAmounts.remove(p.getUniqueId());

        Island island = IslandsManager.INSTANCE.getIslandOf(p.getUniqueId());
        SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
        if (island != null && user != null) {
            if (completed) {
                IslandRanks rank = island.getMembers().get(p.getUniqueId());
                if (rank == null) {
                    p.sendMessage(Component.text("§cUne erreur est survenue veuillez recommencer."));
                    return;
                }
                if (deposit) {
                    if (!island.hasPerms(rank, IslandPerms.BANK_ADD, p.getUniqueId())) {
                        p.sendMessage(Component.text("§cVous n'avez pas la permission d'ajouter de l'argent à la banque de votre île."));
                        return;
                    }
                    if (user.getMoney() >= amount) {
                        user.setMoney(user.getMoney() - amount);
                        island.setBankMoney(island.getBankMoney() + amount);
                        p.sendMessage(Component.text("§aVous avez déposé §e" +
                                NumberFormat.getInstance().format(amount) + "§a$ dans la banque de votre île."));
                        return;
                    }
                    p.sendMessage(Component.text("§cVous n'avez pas assez d'argent."));
                    return;
                }
                if (!island.hasPerms(rank, IslandPerms.BANK_REMOVE, p.getUniqueId())) {
                    p.sendMessage(Component.text("§cVous n'avez pas la permission de retirer de l'argent de la banque de votre île."));
                    return;
                }
                if (island.getBankMoney() >= amount) {
                    island.setBankMoney(island.getBankMoney() - amount);
                    user.setMoney(user.getMoney() + amount);
                    p.sendMessage(Component.text("§aVous avez retiré §e" +
                            NumberFormat.getInstance().format(amount) + "§a$ de la banque de votre île."));
                    return;
                }
                p.sendMessage(Component.text("§cVotre île n'a pas assez d'argent."));
                return;
            }
            p.sendMessage(Component.text("§cVous avez annulé l'opération."));
            return;
        }
        p.sendMessage(Component.text("§cUne erreur est survenue."));
    }

    public boolean isAwaitingAmount(UUID uuid) {
        return awaitingAmounts.containsKey(uuid);
    }
}
