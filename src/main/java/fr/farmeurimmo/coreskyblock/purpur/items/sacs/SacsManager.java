package fr.farmeurimmo.coreskyblock.purpur.items.sacs;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SacsManager {

    public static final String NAME_FORMAT = "§lSac de %s";
    public static final String LORE_FORMAT = "§6x%d %s";
    public static final int MAX_AMOUNT = 5_000;
    public static final Material MATERIAL = Material.PAPER;
    public static SacsManager INSTANCE;

    public SacsManager() {
        INSTANCE = this;
    }

    public ItemStack createSacs(SacsType type, int amount) {
        ItemStack item = new ItemStack(MATERIAL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(String.format(NAME_FORMAT, type.getName())));
        meta.lore(List.of(Component.text(LORE_FORMAT.formatted(amount, type.getName())),
                Component.text("§0" + UUID.randomUUID()),
                Component.text("§7Stocke tous/toute vos/votre §e" + type.getName().toLowerCase()),
                Component.text("§7en un seul endroit."), Component.text(""),
                Component.text("§7Clic droit pour ouvrir.")));
        meta.setUnbreakable(true);
        meta.setCustomModelData(10468);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isASacs(ItemStack itemStack, SacsType sacsType) {
        return getAmount(itemStack, sacsType) != -1;
    }

    public int getAmount(ItemStack itemStack, SacsType sacsType) {
        if (itemStack == null) return -1;
        if (itemStack.getType() != MATERIAL) return -1;
        if (!itemStack.hasItemMeta()) return -1;
        if (!itemStack.getItemMeta().hasLore()) return -1;
        if (!itemStack.getItemMeta().isUnbreakable()) return -1;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta.displayName() == null) return -1;
        if (meta.lore() == null) return -1;
        if (Objects.requireNonNull(meta.lore()).size() < 3) return -1;
        if (!Objects.equals(itemStack.getItemMeta().getDisplayName(), String.format(NAME_FORMAT, sacsType.getName())))
            return -1;
        String lore = Objects.requireNonNull(meta.getLore()).get(0);
        int amount = Integer.parseInt(lore.substring(lore.indexOf("x") + 1, lore.indexOf(" ")));
        if (amount >= MAX_AMOUNT) return -2;
        return amount;
    }

    public boolean isASacs(ItemStack itemStack) {
        return getSacsType(itemStack) != null;
    }

    public SacsType getSacsType(ItemStack itemStack) {
        for (SacsType sacsType : SacsType.values()) {
            if (isASacs(itemStack, sacsType)) {
                return sacsType;
            }
        }
        return null;
    }

    public void setAmount(ItemStack itemStack, SacsType sacsType, int amount) {
        ItemMeta meta = itemStack.getItemMeta();
        List<Component> lore = meta.lore();
        if (lore == null || lore.size() < 3) return;
        lore.set(0, Component.text(LORE_FORMAT.formatted(amount, sacsType.getName())));
        meta.lore(lore);
        itemStack.setItemMeta(meta);
    }
}
