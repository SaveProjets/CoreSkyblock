package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsTopManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsWarpManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandWarp;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class IslandsTopInv extends FastInv {

    private static final int[] slots = new int[]{13, 21, 22, 23, 29, 30, 31, 32, 33, 37, 38, 39, 40, 41, 42, 43};
    private boolean closed = false;
    private int topSelected = 0; //0 = Top value, 1 = Top bank money, 2 = Warp rate

    public IslandsTopInv() {
        super(54, "§8Classement des îles");

        setCloseFilter(p -> {
            closed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
            if (closed) {
                task.cancel();
                return;
            }
            update();
        }, 0, 40L);
    }

    private void update() {
        ItemStack topValue = ItemBuilder.copyOf(new ItemStack(Material.DRAGON_EGG)).name("§6Top valeur de l'île")
                .lore("§7Cliquez pour changer le classement", (topSelected == 0 ? "§aAffiché" : "§cNon affiché")).build();
        ItemStack topBankMoney = ItemBuilder.copyOf(new ItemStack(Material.GOLD_INGOT)).name("§6Top argent en banque")
                .lore("§7Cliquez pour changer le classement", (topSelected == 1 ? "§aAffiché" : "§cNon affiché")).build();
        ItemStack topWarpRate = ItemBuilder.copyOf(new ItemStack(Material.ENDER_PEARL)).name("§6Top évaluation des warps")
                .lore("§7Cliquez pour changer le classement", (topSelected == 2 ? "§aAffiché" : "§cNon affiché")).build();
        if (topSelected == 0) {
            topValue.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
            topValue.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else if (topSelected == 1) {
            topBankMoney.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
            topBankMoney.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else if (topSelected == 2) {
            topWarpRate.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
            topWarpRate.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        setItem(3, topValue, e -> {
            if (topSelected == 0) {
                e.getWhoClicked().sendMessage(Component.text("§cCe classement est déjà affiché."));
                return;
            }
            topSelected = 0;
            update();
        });
        setItem(4, topBankMoney, e -> {
            if (topSelected == 1) {
                e.getWhoClicked().sendMessage(Component.text("§cCe classement est déjà affiché."));
                return;
            }
            topSelected = 1;
            update();
        });
        setItem(5, topWarpRate, e -> {
            if (topSelected == 2) {
                e.getWhoClicked().sendMessage(Component.text("§cCe classement est déjà affiché."));
                return;
            }
            topSelected = 2;
            update();
        });

        int i = 0;
        LinkedHashMap<Pair<UUID, String>, Double> topIslands = IslandsTopManager.INSTANCE.getTopIslands(topSelected);
        if (topSelected == 0) {
            for (Pair<UUID, String> island : topIslands.keySet()) {
                if (i >= slots.length) {
                    break;
                }
                IslandWarp islandWarp = IslandsWarpManager.INSTANCE.getByIslandUUID(island.left());

                setItem(slots[i], ItemBuilder.copyOf(new ItemStack(Material.PLAYER_HEAD, i + 1)).name("§6" +
                                island.right().replace("&", "§")).lore("", "§7Valeur: §6" +
                                NumberFormat.getInstance().format(topIslands.get(island)), "", (islandWarp != null
                                && islandWarp.isActivated() ? ("§aCliquez pour vous téléporter") : "§cPas de warp disponible"))
                        .build(), getConsumer(islandWarp));
                i++;
            }
        } else if (topSelected == 1) {
            for (Pair<UUID, String> island : topIslands.keySet()) {
                if (i >= slots.length) {
                    break;
                }
                IslandWarp islandWarp = IslandsWarpManager.INSTANCE.getByIslandUUID(island.left());

                setItem(slots[i], ItemBuilder.copyOf(new ItemStack(Material.PLAYER_HEAD, i + 1)).name("§6" +
                                island.right().replace("&", "§")).lore("", "§7Argent en banque: §6" +
                                NumberFormat.getInstance().format(topIslands.get(island)), "", (islandWarp != null
                                && islandWarp.isActivated() ? ("§aCliquez pour vous téléporter") : "§cPas de warp disponible"))
                        .build(), getConsumer(islandWarp));
                i++;
            }
        } else if (topSelected == 2) {
            for (Pair<UUID, String> island : topIslands.keySet()) {
                if (i >= slots.length) {
                    break;
                }
                IslandWarp islandWarp = IslandsWarpManager.INSTANCE.getByIslandUUID(island.left());

                setItem(slots[i], ItemBuilder.copyOf(new ItemStack(Material.PLAYER_HEAD, i + 1)).name("§6" +
                                island.right().replace("&", "§")).lore("", "§7Évaluation des warps: §6" +
                                NumberFormat.getInstance().format(topIslands.get(island)), "", (islandWarp != null
                                && islandWarp.isActivated() ? ("§aCliquez pour vous téléporter") : "§cPas de warp disponible"))
                        .build(), getConsumer(islandWarp));
                i++;
            }
        }

        for (int iLeft = i; iLeft < slots.length; iLeft++) {
            setItem(slots[iLeft], ItemBuilder.copyOf(new ItemStack(Material.BEDROCK, iLeft + 1)).name("§cPas d'île").build());
        }

        setItem(45, ItemBuilder.copyOf(new ItemStack(Material.CLOCK)).name("§6Informations")
                .lore("§7Dernière actualisation:", "§c" + IslandsTopManager.INSTANCE.getTimeAfterRefresh(), "",
                        "§7Actualisation du classement:", "§c" + IslandsTopManager.INSTANCE.getTimeUntilRefresh())
                .build());

        setItem(53, CommonItemStacks.getCommonBack(), e -> {
            Island island = IslandsManager.INSTANCE.getIslandOf(e.getWhoClicked().getUniqueId());
            if (island != null) {
                new IslandInv(island).open((Player) e.getWhoClicked());
            } else e.getWhoClicked().closeInventory();
        });
    }

    private Consumer<InventoryClickEvent> getConsumer(IslandWarp warp) {
        return e -> {
            if (warp != null && warp.isActivated()) {
                IslandsWarpManager.INSTANCE.teleportToWarp((Player) e.getWhoClicked(), warp);
            } else e.getWhoClicked().sendMessage(Component.text("§cCette île ne possède pas de warp " +
                    "disponible au publique."));
        };
    }
}
