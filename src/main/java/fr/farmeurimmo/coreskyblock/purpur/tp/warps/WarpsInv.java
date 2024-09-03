package fr.farmeurimmo.coreskyblock.purpur.tp.warps;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.entity.Player;

public class WarpsInv extends FastInv {

    public WarpsInv() {
        super(3 * 9, "§8Warps");

        int i = 0;
        for (String name : WarpsManager.INSTANCE.warps.keySet()) {
            setItem(i, WarpsManager.INSTANCE.getItemStackForWarp(name), e -> {
                Player p = (Player) e.getWhoClicked();
                p.closeInventory();

                WarpsManager.INSTANCE.teleportToWarp(p, name);
            });
            i++;
        }
    }
}
