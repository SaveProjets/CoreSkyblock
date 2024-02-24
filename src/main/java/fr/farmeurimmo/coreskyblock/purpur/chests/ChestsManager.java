package fr.farmeurimmo.coreskyblock.purpur.chests;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class ChestsManager {

    public static ChestsManager INSTANCE;

    public ChestsManager() {
        INSTANCE = this;
    }

    public void giveItem(Player p, ChestType type) {
        ItemStack item = new ItemStack(type.getMaterial());

        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(type.getName()));
        ArrayList<Component> lore = new ArrayList<>();
        type.getLore().forEach(s -> lore.add(Component.text(s)));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        item.setUnbreakable(true);

        p.getInventory().addItem(item);
    }
}
