package fr.farmeurimmo.skylyblock.purpur.islands.invs;

import fr.farmeurimmo.skylyblock.common.islands.Island;
import fr.farmeurimmo.skylyblock.purpur.islands.IslandsManager;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IslandInv extends FastInv {

    public IslandInv(Island island) {
        super(45, "§8Menu de l'île");

        setItem(10, ItemBuilder.copyOf(new ItemStack(Material.ENDER_EYE))
                .name("§6Téléportation §8| §7(clic gauche)").build(), e ->
                IslandsManager.INSTANCE.teleportToIsland(island, (Player) e.getWhoClicked()));
    }
}
