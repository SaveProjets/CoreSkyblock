package fr.farmeurimmo.skylyblock.purpur.silos;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

public class SilosManager {

    public static final String NAME_FORMAT = "§lSilos de %s";
    public static final String LORE_FORMAT = "§6x%d %s";
    public static SilosManager INSTANCE;

    public SilosManager() {
        INSTANCE = this;
    }

    public ItemStack createSilo(SilosType silosType) {
        ItemStack itemStack = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(String.format(NAME_FORMAT, silosType.getName())));
        itemMeta.lore(List.of(Component.text(LORE_FORMAT.formatted(0, silosType.getName())),
                Component.text(""), Component.text("§7Stocke tous vos " + silosType.getName().toLowerCase()),
                Component.text("§7cultures en un seul endroit."), Component.text(""),
                Component.text("§7Clic droit pour ouvrir.")));
        itemMeta.setUnbreakable(true);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public boolean isASilo(ItemStack itemStack) {
        return getAmount(itemStack) != -1;
    }

    public int getAmount(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() != Material.CHEST || !itemStack.hasItemMeta()) {
            return -1;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.displayName() == null || itemMeta.lore() == null || Objects.requireNonNull(itemMeta.lore()).size() < 3) {
            return -1;
        }
        if (!itemStack.isUnbreakable()) {
            return -1;
        }
        if (!Objects.equals(itemMeta.displayName(), Component.text(String.format(NAME_FORMAT, SilosType.CARROT.getName())))) {
            return -1;
        }
        String lore = Objects.requireNonNull(itemMeta.getLore()).get(0);
        String[] split = lore.split(" ");
        return Integer.parseInt(split[0].substring(2));
    }

    public void setAmount(ItemStack itemStack, int amount) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<Component> lore = itemMeta.lore();
        if (lore == null || lore.size() < 3) {
            return;
        }
        lore.set(0, Component.text(LORE_FORMAT.formatted(amount, SilosType.CARROT.getName())));
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
    }
}
