package fr.farmeurimmo.coreskyblock.purpur.items.sacs;

import fr.farmeurimmo.coreskyblock.purpur.shop.ShopsManager;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class SacInv extends FastInv {

    public SacInv(@NotNull ItemStack item, SacsType sacsType) {
        super(3 * 9, "§0" + sacsType.getName());

        Material material = sacsType.getMaterial();
        double sellPrice = ShopsManager.INSTANCE.getSellPrice(new ItemStack(material));
        int amount = SacsManager.INSTANCE.getAmount(item, sacsType);
        if (amount < 0) {
            amount = 0;
        }
        double price = sellPrice * amount;
        String priceFormatted = NumberFormat.getInstance().format(price);
        int finalAmount = amount;

        setItem(11, ItemBuilder.copyOf(new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 64))
                .name("§aTout vendre").lore("§6Gains prévus: §a" + priceFormatted + "$").build(), e -> {
            e.setCancelled(true);
            if (finalAmount < 1) {
                e.getWhoClicked().sendMessage(Component.text("§cIl n'y a pas assez d'items dans ce silo."));
                return;
            }
            e.getWhoClicked().closeInventory();

            e.getWhoClicked().sendMessage(Component.text("§aVous avez vendu §6x" + finalAmount + " " +
                    sacsType.getName() + "§a pour §6" + priceFormatted + "$§a."));
            SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(e.getWhoClicked().getUniqueId());
            user.addMoney(price);
            SacsManager.INSTANCE.setAmount(item, sacsType, 0);
        });

        setItem(14, ItemBuilder.copyOf(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 64))
                .name("§6Récupérer").lore("§eClic droit pour tout récupérer").build(), e -> {
            e.setCancelled(true);

            int space = ShopsManager.INSTANCE.getSpaceAvailableFor((Player) e.getWhoClicked(),
                    new ItemStack(material));
            if (space == 0) {
                e.getWhoClicked().sendMessage(Component.text("§cVotre inventaire est plein."));
                return;
            }
            if (finalAmount < 1) {
                e.getWhoClicked().sendMessage(Component.text("§cIl n'y a pas assez d'items dans ce silo."));
                return;
            }
            e.getWhoClicked().closeInventory();
            int total = Math.min(space, finalAmount);
            e.getWhoClicked().sendMessage(Component.text("§aVous avez récupéré §6x" + total + " " +
                    sacsType.getName() + "§a."));
            SacsManager.INSTANCE.setAmount(item, sacsType, finalAmount - total);
            for (int i = 0; i < finalAmount; i++) {
                e.getWhoClicked().getInventory().addItem(new ItemStack(material));
            }
        });

        setItem(15, ItemBuilder.copyOf(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 16))
                .name("§6Récupérer 16").lore("§eClic droit pour récupérer 16 items").build(), e -> {
            e.setCancelled(true);

            int space = ShopsManager.INSTANCE.getSpaceAvailableFor((Player) e.getWhoClicked(),
                    new ItemStack(material));
            if (space == 0) {
                e.getWhoClicked().sendMessage(Component.text("§cVotre inventaire est plein."));
                return;
            }
            if (finalAmount < 16) {
                e.getWhoClicked().sendMessage(Component.text("§cIl n'y a pas assez d'items dans ce silo."));
                return;
            }
            e.getWhoClicked().closeInventory();
            int total = Math.min(space, 16);
            e.getWhoClicked().sendMessage(Component.text("§aVous avez récupéré §6x" + total + " " +
                    sacsType.getName() + "§a."));
            SacsManager.INSTANCE.setAmount(item, sacsType, finalAmount - total);
            for (int i = 0; i < total; i++) {
                e.getWhoClicked().getInventory().addItem(new ItemStack(material));
            }
        });
    }
}
