package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsWarpManager;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandWarp;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;

public class IslandsWarpBrowserInv extends FastInv {

    private static final int[] promotedSlots = new int[]{0, 2, 4, 6, 8};
    private static int PAGE = 0;
    private boolean gotUpdate = false;
    private boolean closed = false;

    public IslandsWarpBrowserInv() {
        super(54, "§8Warps des îles");

        update();

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
            update();
        }, 0, 40L);
    }

    private void update() {
        for (int i = 0; i < 54; i++) {
            setItem(i, new ItemBuilder(Material.AIR).build());
        }

        int i = 0;
        ArrayList<IslandWarp> warps = IslandsWarpManager.INSTANCE.getActiveWarps();
        for (int j = PAGE * 27; j < warps.size(); j++) {
            if (i >= 27) break;
            IslandWarp warp = warps.get(j);
            setItem(i, new ItemBuilder(warp.getMaterial()).name("§6" + warp.getName()).lore("§7Cliquez pour vous téléporter").build(), e -> {
                e.getWhoClicked().closeInventory();
            });
            i++;
        }

        if (PAGE > 0) {
            setItem(45, new ItemBuilder(Material.ARROW).name("§6Page précédente (§7" + PAGE + "§6)").build(), e -> {
                PAGE--;
                gotUpdate = false;
            });
        }

        if (warps.size() > (PAGE + 1) * 27) {
            setItem(53, new ItemBuilder(Material.ARROW).name("§6Page suivante (§7" + (PAGE + 2) + "§6)").build(), e -> {
                PAGE++;
                gotUpdate = false;
            });
        }

        setItem(49, new ItemBuilder(Material.IRON_DOOR).name("§6Fermer").build(), e -> e.getWhoClicked().closeInventory());
    }
}
