package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandRanksManager;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Objects;

public class IslandRankEditInv extends FastInv {

    private static final long COOLDOWN = 100;
    private final int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32,
            33, 34, 37, 38, 39, 40, 41, 42, 43};
    private long lastAction = System.currentTimeMillis() - COOLDOWN;
    private boolean closed = false;
    private int page = 0;

    public IslandRankEditInv(Island island, Player p) {
        super(54, "§8Permissions des grades");

        if (island == null) {
            p.sendMessage("§cUne erreur est survenue lors de la récupération de votre île.");
            closed = true;
            return;
        }

        CommonItemStacks.applyCommonPanes(Material.YELLOW_STAINED_GLASS_PANE, getInventory());

        setItem(49, CommonItemStacks.getCommonBack(), e -> {
            new IslandInv(island).open(p);
        });

        update(island);

        setCloseFilter(c -> {
            closed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
            if (closed) {
                task.cancel();
                return;
            }
            update(island);
        }, 0, 40L);
    }

    public void update(Island island) {
        if (island == null) return;

        int startValue = page * slots.length;
        ArrayList<IslandPerms> perms = IslandPerms.getPerms(startValue, slots.length);
        int currentSlot = 0;

        for (IslandPerms perm : perms) {
            ItemStack custom = new ItemStack(perm.getMaterial());
            if (custom.getType() == Material.AIR) continue;
            ItemMeta meta = custom.getItemMeta();
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("generic.attack_damage")),
                            0.0, AttributeModifier.Operation.ADD_NUMBER));
            custom.setItemMeta(meta);
            custom.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_PLACED_ON);
            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§dInformation:");
            boolean isMaxRange = !perm.getDescription().startsWith("§f");
            for (IslandRanks rank : IslandRanks.values()) {
                if (rank == IslandRanks.CHEF) continue;
                if (isMaxRange && rank == IslandRanks.COOP) continue;
                if (isMaxRange && rank == IslandRanks.VISITEUR) continue;
                if (island.hasPerms(rank, perm, null)) {
                    lore.add(CommonItemStacks.getArrowWithColors(true, false) + rank.name());
                } else {
                    lore.add(CommonItemStacks.getArrowWithColors(false, false) + rank.name());
                }
            }
            lore.add("");
            lore.add("§8➡ §fCliquez pour changer.");
            setItem(slots[currentSlot], ItemBuilder.copyOf(custom).name(perm.getDescription()).lore(lore)
                    .flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_PLACED_ON).build(), e -> {
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
            setItem(48, CommonItemStacks.getCommonPreviousPage(), e -> {
                page--;
                update(island);
            });
        } else setItem(48, null);

        if (!IslandPerms.getPerms((page + 1) * slots.length, 1).isEmpty()) {
            setItem(50, CommonItemStacks.getCommonNextPage(), e -> {
                page++;
                update(island);
            });
        } else setItem(50, null);
    }
}
