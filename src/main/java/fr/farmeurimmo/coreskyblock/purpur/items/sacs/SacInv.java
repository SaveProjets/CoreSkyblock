package fr.farmeurimmo.coreskyblock.purpur.items.sacs;

import fr.mrmicky.fastinv.FastInv;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SacInv extends FastInv {

    public SacInv(@Nullable ItemStack item, SacsType sacsType) {
        super(3 * 9, "§0" + sacsType.getName());


    }
}
