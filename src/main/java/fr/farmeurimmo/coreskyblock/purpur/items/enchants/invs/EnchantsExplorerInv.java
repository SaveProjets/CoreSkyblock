package fr.farmeurimmo.coreskyblock.purpur.items.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.items.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.EnchantmentsRecipients;
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
    private EnchantmentsRecipients recipients;

    public EnchantsExplorerInv(Player whoClicked, EnchantmentsRecipients recipients) {
        super(4 * 9, "§0Enchantements disponibles");

        this.recipients = recipients;

        update();
    }

    private void update() {
        for (int i = 0; i < getInventory().getSize(); i++) {
            setItem(i, null);
        }
        if (recipients == null) {
            int slot = 9;
            for (Map.Entry<EnchantmentsRecipients, ArrayList<Enchantments>> entry : ENCHANTMENTS_RECIPIENTS_ARRAY_LIST_MAP.entrySet()) {
                setItem(slot, new ItemBuilder(entry.getKey().getMaterialForDisplay())
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

            setItem(getInventory().getSize()-1, new ItemBuilder(Material.IRON_DOOR).name("§6Retour §8| §7(clic gauche)").build(),
                    e -> new EnchantsMainInv().open((Player) e.getWhoClicked()));

            return;
        }

        setItem(0, new ItemBuilder(recipients.getMaterialForDisplay()).name("§6" + recipients.getName()).build());

        int slot = 9;
        for (Enchantments enchantment : ENCHANTMENTS_RECIPIENTS_ARRAY_LIST_MAP.get(recipients)) {
            if (enchantment.hasMaxLevel()) {
                for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                    setItem(slot, CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantment, level));

                    slot++;
                }
                continue;
            }
            setItem(slot, CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantment, 1));

            slot++;
        }

        setItem(2, new ItemBuilder(Material.ARROW).name("§6Retour").build(), e -> {
            recipients = null;
            update();
        });
    }
}
