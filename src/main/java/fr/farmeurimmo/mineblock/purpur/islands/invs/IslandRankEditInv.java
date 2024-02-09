package fr.farmeurimmo.mineblock.purpur.islands.invs;

import fr.farmeurimmo.mineblock.common.islands.Island;
import fr.farmeurimmo.mineblock.common.islands.IslandPerms;
import fr.farmeurimmo.mineblock.common.islands.IslandRank;
import fr.farmeurimmo.mineblock.common.islands.IslandRanks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class IslandRankEditInv extends FastInv {

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

        int currentSlot = 10;
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
            setItem(currentSlot, ItemBuilder.copyOf(custom).name(perm.getDescription()).lore(lore).build(), e -> {
                if (e.isLeftClick()) {
                    island.removePermsToRank(IslandRank.instance.getNextRankForPerm(perm, island), perm);
                } else {
                    island.addPermsToRank(IslandRank.instance.getNextRankForPerm(perm, island), perm);
                }
                new IslandRankEditInv(island, p).open(p);
            });
        }
    }
}
