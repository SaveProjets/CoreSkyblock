package fr.farmeurimmo.coreskyblock.purpur.auctions.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;

public class AuctionBrowserInv extends FastInv {

    private static final int[] slots = new int[]{13, 21, 22, 23, 29, 30, 31, 32, 33, 37, 38, 39, 40, 41, 42, 43};
    private boolean gotUpdate = false;
    private boolean closed = false;
    private final int page = 0;

    public AuctionBrowserInv() {
        super(54, "§8Hôtel des ventes");

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

    }
}
