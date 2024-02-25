package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandRanks;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandRanksManager;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandMembersInv extends FastInv {

    private final int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32,
            33, 34, 37, 38, 39, 40, 41, 42, 43};

    public IslandMembersInv(Island island, Player whoClicked) {
        super(54, "§8Membres de l'île");

        if (island == null) {
            whoClicked.sendMessage("§cUne erreur est survenue lors de la récupération de votre île.");
            return;
        }

        setItem(53, ItemBuilder.copyOf(new ItemStack(Material.ARROW))
                .name("§6Retour §8| §7(clic gauche)").build(), e -> new IslandInv(island).open((Player) e.getWhoClicked()));

        update(island);
    }

    public void update(Island island) {
        if (island == null) return;

        for (int slot : slots) {
            setItem(slot, null);
        }

        int currentSlot = 0;
        ArrayList<UUID> members = new ArrayList<>();
        int currentLevelRank = 0;
        Map<UUID, IslandRanks> levelRank = island.getMembers();
        HashMap<IslandRanks, Integer> rankPos = IslandRanksManager.INSTANCE.getIslandRankPos();
        while (members.size() != levelRank.size()) {
            if (currentLevelRank > 3) {
                break;
            }
            for (Map.Entry<UUID, IslandRanks> playerEntry : levelRank.entrySet()) {
                if (members.contains(playerEntry.getKey())) {
                    continue;
                }
                if (rankPos.get(playerEntry.getValue()) != currentLevelRank) {
                    continue;
                }
                ItemStack custom = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) custom.getItemMeta();
                meta.setOwner(island.getMemberName(playerEntry.getKey()));
                meta.displayName(Component.text("§f" + island.getMemberName(playerEntry.getKey())));
                custom.setItemMeta(meta);
                ArrayList<String> lore = new ArrayList<>();
                lore.add("§7Clic droit pour promouvoir");
                lore.add("§7Clic gauche pour rétrograder");
                lore.add("§7Shift + clic pour retirer");
                lore.add("");
                lore.add("§7Grade : " + playerEntry.getValue().getName());
                setItem(slots[currentSlot], ItemBuilder.copyOf(custom).lore(lore).build(), e -> {
                    IslandRanks rank = island.getMembers().get(e.getWhoClicked().getUniqueId());
                    if (rank.getId() >= playerEntry.getValue().getId()) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier les " +
                                "grades de joueurs ayant un grade supérieur ou égal au vôtre."));
                        return;
                    }
                    if (e.isShiftClick()) {
                        if (!island.hasPerms(rank, IslandPerms.KICK, e.getWhoClicked().getUniqueId())) {
                            e.getWhoClicked().sendMessage(Component.text(
                                    "§cVous n'avez pas la permission de retirer un membre."));
                            return;
                        }
                        island.removeMember(playerEntry.getKey());
                        island.sendMessageToAll("§c" + Bukkit.getOfflinePlayer(playerEntry.getKey()).getName() + " a été retiré de l'île.");
                        update(island);
                        return;
                    }
                    if (e.isLeftClick()) {
                        if (!island.hasPerms(rank, IslandPerms.PROMOTE, e.getWhoClicked().getUniqueId())) {
                            e.getWhoClicked().sendMessage(Component.text(
                                    "§cVous n'avez pas la permission de promouvoir un membre."));
                            return;
                        }
                        if (rank.getId() >= playerEntry.getValue().getId() - 1) {
                            e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas promouvoir " +
                                    "un joueur à un grade supérieur ou égal au vôtre."));
                            return;
                        }
                        if (island.promote(playerEntry.getKey())) {
                            e.getWhoClicked().sendMessage(Component.text("§aVous avez promu " +
                                    island.getMemberName(playerEntry.getKey()) + " au grade " +
                                    island.getMembers().get(playerEntry.getKey()).getName()));
                        } else {
                            e.getWhoClicked().sendMessage(Component.text("§cImpossible de promouvoir " +
                                    island.getMemberName(playerEntry.getKey()) + "."));
                        }
                    } else if (e.isRightClick()) {
                        if (!island.hasPerms(rank, IslandPerms.DEMOTE, e.getWhoClicked().getUniqueId())) {
                            e.getWhoClicked().sendMessage(Component.text(
                                    "§cVous n'avez pas la permission de rétrograder un membre."));
                            return;
                        }
                        if (playerEntry.getValue().getId() <= rank.getId()) {
                            e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas rétrograder " +
                                    "un joueur d'un grade supérieur ou égal au vôtre."));
                            return;
                        }
                        if (island.demote(playerEntry.getKey())) {
                            e.getWhoClicked().sendMessage(Component.text("§aVous avez rétrogradé " +
                                    island.getMemberName(playerEntry.getKey()) + " au grade " +
                                    island.getMembers().get(playerEntry.getKey()).getName()));
                        } else {
                            e.getWhoClicked().sendMessage(Component.text("§cImpossible de rétrograder " +
                                    island.getMemberName(playerEntry.getKey()) + "."));
                        }
                    }
                    update(island);
                });
                members.add(playerEntry.getKey());
                currentSlot++;
            }
            currentLevelRank += 1;
        }
    }
}
