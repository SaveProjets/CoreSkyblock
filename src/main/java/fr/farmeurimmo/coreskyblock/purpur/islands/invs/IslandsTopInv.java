package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsTopManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsWarpManager;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandWarp;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.UUID;

public class IslandsTopInv extends FastInv {

    private static final int[] slots = new int[]{13, 21, 22, 23, 29, 30, 31, 32, 33, 37, 38, 39, 40, 41, 42, 43};
    private boolean gotUpdate = false;
    private boolean closed = false;

    public IslandsTopInv() {
        super(54, "§8Classement des îles");

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
        gotUpdate = false;

        LinkedHashMap<Pair<UUID, String>, Double> topIslands = IslandsTopManager.INSTANCE.getTopIslands();

        int i = 0;
        for (Pair<UUID, String> island : topIslands.keySet()) {
            if (i >= slots.length) {
                break;
            }
            IslandWarp islandWarp = IslandsWarpManager.INSTANCE.getByIslandUUID(island.left());

            setItem(slots[i], ItemBuilder.copyOf(new ItemStack(Material.PLAYER_HEAD, i + 1)).name("§6" +
                            island.right().replace("&", "§")).lore("", "§7Valeur: §6" +
                            NumberFormat.getInstance().format(topIslands.get(island)), "", (islandWarp != null
                            && islandWarp.isActivated() ? ("§aCliquez pour vous téléporter") : "§cPas de warp disponible"))
                    .build(), e -> {
                if (islandWarp != null && islandWarp.isActivated()) {
                    IslandsWarpManager.INSTANCE.teleportToWarp((Player) e.getWhoClicked(), islandWarp);
                } else e.getWhoClicked().sendMessage(Component.text("§cCette île ne possède pas de warp " +
                        "disponible au publique."));
            });
            i++;
        }

        for (int iLeft = i; iLeft < slots.length; iLeft++) {
            setItem(slots[iLeft], ItemBuilder.copyOf(new ItemStack(Material.BEDROCK, iLeft + 1)).name("§cPas d'île").build());
        }

        setItem(45, ItemBuilder.copyOf(new ItemStack(Material.CLOCK)).name("§6Informations")
                .lore("§7Dernière actualisation:", "§c" + IslandsTopManager.INSTANCE.getTimeAfterRefresh(), "",
                        "§7Actualisation du classement:", "§c" + IslandsTopManager.INSTANCE.getTimeUntilRefresh())
                .build());
    }
}
