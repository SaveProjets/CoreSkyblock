package fr.farmeurimmo.coreskyblock.purpur.silos;

import fr.farmeurimmo.coreskyblock.purpur.shop.ShopsManager;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;

public class SilosInv extends FastInv {

    public SilosInv(ItemStack itemStack, SilosType silosType) {
        super(27, SilosManager.NAME_FORMAT.formatted(silosType.getName()));

        setItems(new int[]{9, 0, 1, 7, 8, 17, 18, 19, 25, 26}, ItemBuilder.copyOf(new ItemStack(
                Material.LIME_STAINED_GLASS_PANE)).name("§6").build());


        Material material;
        if (silosType.getAlternativeMaterial() != null) {
            material = silosType.getAlternativeMaterial();
        } else {
            material = silosType.getMaterial();
        }
        double sellPrice = ShopsManager.INSTANCE.getSellPrice(new ItemStack(material));
        int amount = SilosManager.INSTANCE.getAmount(itemStack, silosType);
        if (amount < 0) {
            amount = 0;
        }
        double price = sellPrice * amount;
        String priceFormatted = NumberFormat.getInstance().format(price);
        int finalAmount = amount;
        setItem(12, ItemBuilder.copyOf(new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 64))
                .name("§aTout vendre").lore("§6Gains prévus: §a" + priceFormatted + "$").build(), e -> {
            e.setCancelled(true);
            if (finalAmount < 1) {
                e.getWhoClicked().sendMessage(Component.text("§cIl n'y a pas assez d'items dans ce silo."));
                return;
            }
            e.getWhoClicked().closeInventory();

            e.getWhoClicked().sendMessage(Component.text("§aVous avez vendu §6x" + finalAmount + " " +
                    silosType.getName() + "§a pour §6" + priceFormatted + "$§a."));
            SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(e.getWhoClicked().getUniqueId());
            user.setMoney(user.getMoney() + price);
            SilosManager.INSTANCE.setAmount(itemStack, silosType, 0);
        });

        int finalAmount1 = amount;
        setItem(13, ItemBuilder.copyOf(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 64))
                .name("§6Récupérer").lore("§eClic droit pour tout récupérer").build(), e -> {
            e.setCancelled(true);

            int space = ShopsManager.INSTANCE.getSpaceAvailableFor((Player) e.getWhoClicked(),
                    new ItemStack(material));
            if (space == 0) {
                e.getWhoClicked().sendMessage(Component.text("§cVotre inventaire est plein."));
                return;
            }
            if (finalAmount1 < 1) {
                e.getWhoClicked().sendMessage(Component.text("§cIl n'y a pas assez d'items dans ce silo."));
                return;
            }
            e.getWhoClicked().closeInventory();
            int total = Math.min(space, finalAmount1);
            e.getWhoClicked().sendMessage(Component.text("§aVous avez récupéré §6x" + total + " " +
                    silosType.getName() + "§a."));
            SilosManager.INSTANCE.setAmount(itemStack, silosType, finalAmount1 - total);
            for (int i = 0; i < finalAmount1; i++) {
                e.getWhoClicked().getInventory().addItem(new ItemStack(material));
            }
        });

        int finalAmount2 = amount;
        setItem(14, ItemBuilder.copyOf(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 1))
                .name("§6Récupérer 64").lore("§eClic droit pour récupérer 64 items").build(), e -> {
            e.setCancelled(true);

            int space = ShopsManager.INSTANCE.getSpaceAvailableFor((Player) e.getWhoClicked(),
                    new ItemStack(material));
            if (space == 0) {
                e.getWhoClicked().sendMessage(Component.text("§cVotre inventaire est plein."));
                return;
            }
            if (finalAmount2 < 64) {
                e.getWhoClicked().sendMessage(Component.text("§cIl n'y a pas assez d'items dans ce silo."));
                return;
            }
            e.getWhoClicked().closeInventory();
            int total = Math.min(space, 64);
            e.getWhoClicked().sendMessage(Component.text("§aVous avez récupéré §6x" + total + " " +
                    silosType.getName() + "§a."));
            SilosManager.INSTANCE.setAmount(itemStack, silosType, finalAmount2 - total);
            for (int i = 0; i < total; i++) {
                e.getWhoClicked().getInventory().addItem(new ItemStack(material));
            }
        });
    }
}
