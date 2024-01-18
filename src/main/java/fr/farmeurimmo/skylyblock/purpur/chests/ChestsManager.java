package fr.farmeurimmo.skylyblock.purpur.chests;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChestsManager {

    public static ChestsManager INSTANCE;

    public ChestsManager() {
        INSTANCE = this;
    }

    public void giveItem(Player p, ChestType type) {
        ItemStack item = new ItemStack(type.getMaterial());

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(type.getName());
        meta.setLore(type.getLore());
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        item.setUnbreakable(true);

        p.getInventory().addItem(item);
    }
}
