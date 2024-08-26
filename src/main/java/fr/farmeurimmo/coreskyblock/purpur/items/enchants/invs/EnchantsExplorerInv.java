package fr.farmeurimmo.coreskyblock.purpur.items.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.items.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.EnchantmentsRecipients;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class EnchantsExplorerInv extends FastInv {

    private static final LinkedHashMap<EnchantmentsRecipients, ArrayList<Enchantments>> ENCHANTMENTS_RECIPIENTS_ARRAY_LIST_MAP = Enchantments.getEnchantmentsByMaterial();
    private static final int[] SLOTS = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private EnchantmentsRecipients recipients;

    public EnchantsExplorerInv(EnchantmentsRecipients recipients) {
        super(6 * 9, "§8Enchantements disponibles");

        this.recipients = recipients;

        update();
    }

    private void update() {
        if (recipients == null) {
            int slot = 0;
            for (Map.Entry<EnchantmentsRecipients, ArrayList<Enchantments>> entry : ENCHANTMENTS_RECIPIENTS_ARRAY_LIST_MAP.entrySet()) {
                setItem(SLOTS[slot], new ItemBuilder(entry.getKey().getMaterialForDisplay())
                        .meta(itemMeta -> itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                                new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("generic.attack_damage")),
                                        0.0, AttributeModifier.Operation.ADD_NUMBER)))
                        .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
                        .name("§6" + entry.getKey().getName()).build(), e -> {
                    recipients = entry.getKey();
                    update();
                });
                slot++;
            }

            for (int i = slot; i < SLOTS.length; i++) {
                setItem(SLOTS[i], null);
            }

            setItem(49, CommonItemStacks.getCommonBack(), e -> new EnchantsMainInv().open((Player) e.getWhoClicked()));

            CommonItemStacks.applyCommonPanes(Material.PURPLE_STAINED_GLASS_PANE, getInventory());

            setItem(10, null);

            return;
        }

        setItem(10, new ItemBuilder(recipients.getMaterialForDisplay()).name("§6" + recipients.getName()).build());

        int slot = 0;
        for (Enchantments enchantment : ENCHANTMENTS_RECIPIENTS_ARRAY_LIST_MAP.get(recipients)) {
            if (enchantment.hasMaxLevel()) {
                for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                    setItem(SLOTS[slot], CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantment, level));

                    slot++;
                }
                continue;
            }
            setItem(SLOTS[slot], CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantment, 1));

            slot++;
        }
        for (int i = slot; i < SLOTS.length; i++) {
            setItem(SLOTS[i], null);
        }

        setItem(49, CommonItemStacks.getCommonBack(), e -> {
            recipients = null;
            update();
        });
    }
}
