package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.common.islands.Island;
import fr.farmeurimmo.coreskyblock.common.islands.IslandPerms;
import fr.farmeurimmo.coreskyblock.common.islands.IslandRanks;
import fr.farmeurimmo.coreskyblock.common.islands.IslandRanksManager;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class IslandRankEditInv extends FastInv {

    private final int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32,
            33, 34, 37, 38, 39, 40, 41, 42, 43};

    public IslandRankEditInv(Island island, Player p) {
        super(54, "§8Permissions des grades de l'île");

        if (island == null) {
            p.sendMessage("§cUne erreur est survenue lors de la récupération de votre île.");
            return;
        }

        setItem(53, ItemBuilder.copyOf(new ItemStack(Material.ARROW))
                .name("§6Retour §8| §7(clic gauche)").build(), e -> new IslandInv(island).open(p));

        setItem(45, ItemBuilder.copyOf(new ItemStack(Material.KNOWLEDGE_BOOK))
                .name("§6Informations complémentaires").lore("§7Les permissions (oranges & blanches) s'appliquent à",
                        "§7tous les membres de l'île.", "", "§7Les COOP(s) et les visiteurs bénéficient uniquement",
                        "§7des permissions de couleur blanche.").build());

        update(island);
    }

    public void update(Island island) {
        if (island == null) return;

        int currentSlot = 0;
        for (IslandPerms perm : IslandPerms.values()) {
            ItemStack custom = new ItemStack(perm.getMaterial());
            if (custom.getType() == Material.AIR) continue;
            ArrayList<String> lore = new ArrayList<>();
            lore.add("§7Clic droit pour augmenter");
            lore.add("§7Clic gauche pour diminuer");
            lore.add("");
            lore.add("§7Grades :");
            boolean isMaxRange = !perm.getDescription().startsWith("§f");
            for (IslandRanks rank : IslandRanks.values()) {
                if (rank == IslandRanks.CHEF) continue;
                if (isMaxRange && rank == IslandRanks.COOP) continue;
                if (isMaxRange && rank == IslandRanks.VISITEUR) continue;
                if (island.hasPerms(rank, perm, null)) {
                    lore.add("§a" + rank.name());
                } else {
                    lore.add("§c" + rank.name());
                }
            }
            setItem(slots[currentSlot], ItemBuilder.copyOf(custom).name(perm.getDescription()).lore(lore).build(), e -> {
                IslandRanks rank = island.getMembers().get(e.getWhoClicked().getUniqueId());
                if (rank == null) return;
                if (!island.hasPerms(rank, IslandPerms.CHANGE_PERMS, e.getWhoClicked().getUniqueId())) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas la permission de modifier " +
                            "les permissions."));
                    return;
                }
                if (!island.hasPerms(rank, perm, e.getWhoClicked().getUniqueId())) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas modifier une permission " +
                            "que vous n'avez pas."));
                    return;
                }
                if (e.isLeftClick()) {
                    IslandRanks previousRank = IslandRanksManager.INSTANCE.getPreviousRankForPerm(perm, island);
                    if (previousRank == null) return;
                    if (previousRank.getId() <= rank.getId()) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas retirer une " +
                                "permission à un grade supérieur ou égal au vôtre."));
                        return;
                    }
                    if (perm != IslandPerms.ALL_PERMS && island.hasPerms(previousRank, IslandPerms.ALL_PERMS, null)) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas retirer une " +
                                "permission à un grade qui a toutes les permissions."));
                        return;
                    }
                    island.removePermsToRank(previousRank, perm);
                } else {
                    island.addPermsToRank(IslandRanksManager.INSTANCE.getNextRankForPerm(perm, island), perm);
                }
                update(island);
            });
            currentSlot++;
        }
    }
}
