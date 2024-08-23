package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsWarpManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandWarp;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;

public class IslandsWarpBrowserInv extends FastInv {

    private static final int[] promotedSlots = new int[]{1, 3, 5, 7};
    private static final long COOLDOWN = 6_000;
    private static int PAGE = 0;
    private boolean gotUpdate = false;
    private long lastAction = System.currentTimeMillis() - COOLDOWN;
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

        ArrayList<IslandWarp> warps = IslandsWarpManager.INSTANCE.getActiveWarps();

        ArrayList<IslandWarp> forwardedWarps = IslandsWarpManager.INSTANCE.getForwardedWarps();

        ArrayList<Integer> slotsTook = new ArrayList<>();
        for (int slot : promotedSlots) {
            if (forwardedWarps.isEmpty()) continue;
            IslandWarp warp = forwardedWarps.get(0);
            if (warp == null) continue;
            if (!warp.isActivated() && warp.isStillForwarded()) {
                setItem(slot, new ItemBuilder(Material.BEDROCK).enchant(Enchantment.CHANNELING)
                        .flags(ItemFlag.HIDE_ENCHANTS).name("§c§lPlace de mise en avant occupée")
                        .lore("§7La place est prise mais le warp", "§7n'est pas encore actif.").build());
                slotsTook.add(slot);
                forwardedWarps.remove(0);
                continue;
            }
            if (!warp.isActivated()) continue;
            if (!warp.isStillForwarded()) continue;
            slotsTook.add(slot);
            setItemForWarp(slot, warp);
            forwardedWarps.remove(0);
        }
        for (int slot : promotedSlots) {
            if (!slotsTook.contains(slot)) {
                setItem(slot, new ItemBuilder(Material.BARRIER).name("§6§lPlace de mise en avant disponible")
                        .lore("§7Si le warp de votre île n'est pas", "§7en cooldown de mise en avant,",
                                "§7contre de l'argent votre warp peut", "§7occuper cette place pendant 24H.", "",
                                "§7Plus d'information dans le menu de", "§7votre warp d'île.").build());
            }
        }

        int i = 18;
        for (int j = PAGE * 27; j < warps.size(); j++) {
            if (i >= 27) break;
            IslandWarp warp = warps.get(j);
            setItemForWarp(i, warp);
            i++;
        }

        if (PAGE > 0) {
            setItem(45, CommonItemStacks.getCommonPreviousPage(), e -> {
                PAGE--;
                gotUpdate = false;
            });
        }

        if (warps.size() > (PAGE + 1) * 27) {
            setItem(53, CommonItemStacks.getCommonNextPage(), e -> {
                PAGE++;
                gotUpdate = false;
            });
        }

        setItem(49, CommonItemStacks.getCommonBack(), e -> {
            Island island = IslandsManager.INSTANCE.getIslandOf(e.getWhoClicked().getUniqueId());
            if (island != null) {
                new IslandInv(island).open((Player) e.getWhoClicked());
            } else e.getWhoClicked().closeInventory();
        });
    }

    private void setItemForWarp(int i, IslandWarp warp) {
        setItem(i, new ItemBuilder(warp.getMaterial()).name("§6" + warp.getName())
                .lore(IslandsWarpManager.INSTANCE.getLore(warp)).build(), e -> {
            if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                e.getWhoClicked().sendMessage("§cVeuillez attendre avant de refaire une action.");
                return;
            }
            lastAction = System.currentTimeMillis();

            IslandsWarpManager.INSTANCE.teleportToWarp((Player) e.getWhoClicked(), warp);
        });
    }
}
