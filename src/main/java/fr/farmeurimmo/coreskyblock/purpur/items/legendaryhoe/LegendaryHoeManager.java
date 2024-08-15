package fr.farmeurimmo.coreskyblock.purpur.items.legendaryhoe;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LegendaryHoeManager {

    public static final String NAME_FORMAT = "§6§lHoue légendaire";
    public static final String LORE_FORMAT = "§7Contient x%d points d'énergie";
    public static final int MAX_AMOUNT = 5_000;
    public static LegendaryHoeManager INSTANCE;

    public LegendaryHoeManager() {
        INSTANCE = this;
    }

    public ItemStack createLegendaryHoe(int energy) {
        ItemStack item = new ItemStack(Material.NETHERITE_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(NAME_FORMAT));
        meta.lore(List.of(Component.text(LORE_FORMAT.formatted(energy)),
                Component.text("§0" + UUID.randomUUID()),
                Component.text("§6Capacité passive: "),
                Component.text("  §ex2 récolte"),
                Component.text("  §ex2 chance d'obtenir un fragment de"),
                Component.text("    §eclef agriculture"),
                Component.text(""),
                Component.text("§6Capacité active:"),
                Component.text("  §eCasse et replante en 3x3 et perd"),
                Component.text("  §e1 point par utilisation du 3x3."),
                Component.text("  §7(Shift + clic droit pour activer/désactiver)")));
        meta.setUnbreakable(true);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("generic.attack_damage")),
                        0.0, AttributeModifier.Operation.ADD_NUMBER));
        item.setItemMeta(meta);

        item.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        return item;
    }

    public boolean isALegendaryHoe(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        if (!item.getItemMeta().isUnbreakable()) return false;
        return Objects.equals(item.getItemMeta().displayName(), Component.text(NAME_FORMAT));
    }
}
