package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IslandCoopsInv extends FastInv {

    private final int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32,
            33, 34, 37, 38, 39, 40, 41, 42, 43};
    private boolean closed = false;

    public IslandCoopsInv(Island island) {
        super(54, "§8Coops de l'île");

        CommonItemStacks.applyCommonPanes(Material.YELLOW_STAINED_GLASS_PANE, getInventory());

        setItem(49, CommonItemStacks.getCommonBack(), e -> new IslandInv(island).open((Player) e.getWhoClicked()));

        update(island);

        setCloseFilter(p -> {
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

    private void update(Island island) {
        int i = 0;
        // key = player that is being cooped, value = player that gave the coop
        for (Map.Entry<UUID, UUID> coop : island.getCoops().entrySet()) {
            if (i >= slots.length) {
                break;
            }

            ItemStack custom = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) custom.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(coop.getKey()));
            meta.displayName(Component.text("§6" + Bukkit.getOfflinePlayer(coop.getKey()).getName()));
            meta.lore(List.of(Component.text("§7Clic pour enlever la coopération")));
            custom.setItemMeta(meta);

            setItem(slots[i], custom, e -> {
                Player p = (Player) e.getWhoClicked();

                p.chat("/is coop " + Bukkit.getOfflinePlayer(coop.getKey()).getName());

                update(island);
            });

            i++;
        }

        for (; i < slots.length; i++) {
            setItem(slots[i], null);
        }
    }
}
