package fr.farmeurimmo.coreskyblock.purpur.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.Enchantments;
import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.EnchantmentsRecipients;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;

public class EnchantsExplorerInv extends FastInv {

    private static final Map<EnchantmentsRecipients, ArrayList<Enchantments>> ENCHANTMENTS_RECIPIENTS_ARRAY_LIST_MAP = Enchantments.getEnchantmentsByMaterial();
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
                setItem(slot, new ItemBuilder(entry.getKey().getMaterialForDisplay()).name("§6" + entry.getKey().getName()).build(), e -> {
                    recipients = entry.getKey();
                    update();
                });
                slot++;
            }

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
