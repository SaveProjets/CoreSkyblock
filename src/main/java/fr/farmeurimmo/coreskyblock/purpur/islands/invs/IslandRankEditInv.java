package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandRanksManager;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class IslandRankEditInv extends FastInv {

    private static final long COOLDOWN = 100;
    private final int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32,
            33, 34, 37, 38, 39, 40, 41, 42, 43};
    private boolean gotUpdate = false;
    private long lastAction = System.currentTimeMillis() - COOLDOWN;
    private boolean closed = false;
    private int page = 0;

    public IslandRankEditInv(Island island, Player p) {
        super(54, "§8Permissions des grades de l'île");

        if (island == null) {
            p.sendMessage("§cUne erreur est survenue lors de la récupération de votre île.");
            gotUpdate = true;
            return;
        }

        setItem(53, ItemBuilder.copyOf(new ItemStack(Material.IRON_DOOR))
                .name("§6Retour §8| §7(clic gauche)").build(), e -> {
            new IslandInv(island).open(p);
            gotUpdate = true;
        });

        setItem(45, ItemBuilder.copyOf(new ItemStack(Material.KNOWLEDGE_BOOK))
                .name("§6Informations complémentaires").lore("§7Les permissions (oranges & blanches) s'appliquent à",
                        "§7tous les membres de l'île.", "", "§7Les COOP(s) et les visiteurs bénéficient uniquement",
                        "§7des permissions de couleur blanche.").build());

        update(island);

        setCloseFilter(c -> {
            gotUpdate = true;
            closed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
            if (closed) {
                task.cancel();
                return;
            }
            if (gotUpdate) return;
            gotUpdate = true;
            update(island);
        }, 0, 40L);
    }

    public void update(Island island) {
        if (island == null) return;
        gotUpdate = false;

        int startValue = page * slots.length;
        ArrayList<IslandPerms> perms = IslandPerms.getPerms(startValue, slots.length);
        int currentSlot = 0;

        for (IslandPerms perm : perms) {
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
            setItem(slots[currentSlot], ItemBuilder.copyOf(custom).name(perm.getDescription()).lore(lore)
                    .flags(ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_PLACED_ON).build(), e -> {
                if (island.isReadOnly()) {
                    IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                    return;
                }
                IslandRanks rank = island.getPlayerRank(e.getWhoClicked().getUniqueId());
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
                if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                    e.getWhoClicked().sendMessage(Component.text("§cMerci de patienter avant de faire une autre action."));
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
                lastAction = System.currentTimeMillis();
                update(island);
            });
            currentSlot++;
        }

        for (int i = currentSlot; i < slots.length; i++) {
            setItem(slots[i], null);
        }

        if (page > 0) {
            setItem(48, ItemBuilder.copyOf(new ItemStack(Material.ARROW)).name("§6Page précédente").build(), e -> {
                page--;
                update(island);
            });
        } else setItem(48, null);

        if (!IslandPerms.getPerms((page + 1) * slots.length, 1).isEmpty()) {
            setItem(50, ItemBuilder.copyOf(new ItemStack(Material.ARROW)).name("§6Page suivante").build(), e -> {
                page++;
                update(island);
            });
        } else setItem(50, null);
    }
}
