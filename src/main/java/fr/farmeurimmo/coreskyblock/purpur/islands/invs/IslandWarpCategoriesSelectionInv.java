package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandWarp;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandWarpCategories;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IslandWarpCategoriesSelectionInv extends FastInv {

    private static final long COOLDOWN = 1_000;
    private boolean gotUpdate = false;
    private long lastAction = System.currentTimeMillis() - COOLDOWN;
    private boolean closed = false;

    public IslandWarpCategoriesSelectionInv(Island island, IslandWarp warp) {
        super(27, "§8Warp de l'île");

        setItem(26, CommonItemStacks.getCommonBack(), e -> {
            new IslandWarpInv(island, warp).open((Player) e.getWhoClicked());
            gotUpdate = true;
        });

        update(island, warp);

        setCloseFilter(p -> {
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
            update(island, warp);
        }, 0, 40L);
    }

    private void update(Island island, IslandWarp warp) {
        gotUpdate = false;

        if (warp != null) {
            int slot = 10;
            for (IslandWarpCategories categories : IslandWarpCategories.getCategories()) {
                setItem(slot, ItemBuilder.copyOf(new ItemStack(categories.getMaterial()))
                        .name((warp.getCategories().contains(categories) ? "§a" : "§c") + categories.getName())
                        .lore("", (warp.getCategories().contains(categories) ? "§cCliquez pour désactiver" :
                                "§aCliquez pour activer") + " §7cette catégorie.").build(), e -> {
                    if (!island.isLoaded()) {
                        e.getWhoClicked().sendMessage(Component.text("§cL'île n'est pas chargée ici."));
                        return;
                    }

                    if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                        e.getWhoClicked().sendMessage(Component.text("§cVeuillez patienter entre chaque action."));
                        e.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO.getKey(), Sound.Source.AMBIENT, 1, 1));
                        return;
                    }
                    lastAction = System.currentTimeMillis();

                    if (warp.getCategories().contains(categories)) {
                        warp.removeCategory(categories);
                    } else {
                        warp.addCategory(categories);
                    }

                    update(island, warp);
                });
                slot++;
            }
        }
    }
}
