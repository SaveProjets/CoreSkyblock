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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class IslandsWarpBrowserInv extends FastInv {

    private static final int[] PROMOTED_SLOTS = new int[]{1, 3, 5, 7};
    private static final long COOLDOWN = 6_000;
    // 0 = affiche par icône définit, 1 = affiche par taille d'amethyste
    private static final int[] SLOTS = new int[]{20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
    private int page = 0;
    private boolean gotUpdate = false;
    private long lastAction = System.currentTimeMillis() - COOLDOWN;
    private boolean closed = false;
    private int displayType = 0;

    public IslandsWarpBrowserInv() {
        super(54, "§8Warps des îles");

        update();

        setCloseFilter(p -> {
            gotUpdate = true;
            closed = true;

            page = 0;
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
        for (int i = 0; i < getInventory().getSize(); i++) {
            setItem(i, new ItemBuilder(Material.AIR).build());
        }

        CommonItemStacks.applyCommonPanes(Material.PINK_STAINED_GLASS_PANE, getInventory());

        ArrayList<IslandWarp> warps = IslandsWarpManager.INSTANCE.getActiveWarps();

        ArrayList<IslandWarp> forwardedWarps = IslandsWarpManager.INSTANCE.getForwardedWarps();

        ArrayList<Integer> slotsTook = new ArrayList<>();
        for (int slot : PROMOTED_SLOTS) {
            if (forwardedWarps.isEmpty()) continue;
            IslandWarp warp = forwardedWarps.get(0);
            if (warp == null) continue;
            if (!warp.isActivated() && warp.isStillForwarded()) {
                setItem(slot, new ItemBuilder(Material.BARRIER).name("§c§lPlace de mise en avant occupée")
                        .lore("", "§dInformation:", "§f▶  §7Cette place est loué,", "    §7mais le warp n'est pas actif.")
                        .build());
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
        for (int slot : PROMOTED_SLOTS) {
            if (!slotsTook.contains(slot)) {
                setItem(slot, new ItemBuilder(Material.BEDROCK).name("§6Place de mise en avant disponible").lore("",
                        "§aDescription:", "§f▶  §7Votre warp d'île peut être", "    §7mis en avant contre de l'argent",
                        "    §7pour une durée de 24h.", "", "§dInformation:", "§f▶ §7Prix: §e25 000$",
                        "§f▶ §7Cooldown: §c24h après utilisation", "", "§8➡ §fCliquez pour acheter.").build());
            }
        }

        int i = 0;
        for (int j = page * SLOTS.length; j < warps.size(); j++) {
            if (j < 0) break;
            if (i >= SLOTS.length) break;
            IslandWarp warp = warps.get(j);
            setItemForWarp(SLOTS[i], warp);
            i++;
        }

        ItemStack item = ItemBuilder.copyOf(new ItemStack(Material.OAK_HANGING_SIGN))
                .name("§e§lIcône")
                .lore("", "§aDescrption:", "§f▶ §7Afficher les warps", "   §7en fonction des icônes.", "", "§8➡ §fCliquez pour y accéder.")
                .build();
        if (displayType == 0) {
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        setItem(18, item, e -> {
            displayType = 0;
            page = 0;
            update();
        });

        item = ItemBuilder.copyOf(new ItemStack(Material.AMETHYST_SHARD))
                .name("§d§lNotation")
                .lore("", "§aDescrption:", "§f▶ §7Afficher les warps", "   §7en fonction des notations.", "", "§8➡ §fCliquez pour y accéder.")
                .build();
        if (displayType == 1) {
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        setItem(27, item, e -> {
            displayType = 1;
            page = 0;
            update();
        });

        if (page > 0) {
            setItem(48, CommonItemStacks.getCommonPreviousPage(), e -> {
                page--;
                update();
            });
        } else setItem(48, null);

        if (warps.size() > (page + 1) * SLOTS.length) {
            setItem(50, CommonItemStacks.getCommonNextPage(), e -> {
                page++;
                update();
            });
        } else setItem(50, null);

        setItem(49, CommonItemStacks.getCommonBack(), e -> {
            Island island = IslandsManager.INSTANCE.getIslandOf(e.getWhoClicked().getUniqueId());
            if (island != null) {
                new IslandInv(island).open((Player) e.getWhoClicked());
            } else e.getWhoClicked().closeInventory();
        });
    }

    private void setItemForWarp(int i, IslandWarp warp) {
        setItem(i, new ItemBuilder(warp.getMaterial(displayType)).name("§6" + warp.getName())
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
