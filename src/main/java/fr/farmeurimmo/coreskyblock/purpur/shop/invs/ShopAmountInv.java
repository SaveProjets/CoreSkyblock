package fr.farmeurimmo.coreskyblock.purpur.shop.invs;

import fr.farmeurimmo.coreskyblock.purpur.shop.ShopsManager;
import fr.farmeurimmo.coreskyblock.purpur.shop.objects.ShopItem;
import fr.farmeurimmo.coreskyblock.purpur.shop.objects.ShopPage;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.Objects;

public class ShopAmountInv extends FastInv {

    private final int MAX_AMOUNT;
    private int amount;

    public ShopAmountInv(ShopItem item, boolean buy, ShopPage shopPage, int page) {
        super(54, "§8Boutique - Quantité " + (buy ? "achat" : "vente"));

        amount = 1;
        MAX_AMOUNT = item.material().getMaxStackSize();

        update(shopPage, item, buy, page);
    }

    private void update(ShopPage shopPage, ShopItem item, boolean buy, int page) {
        setItems(new int[]{9, 0, 1, 7, 8, 17, 52, 53, 44, 36, 45, 46}, ItemBuilder.copyOf(new ItemStack(
                Material.LIME_STAINED_GLASS_PANE)).name("§6").build());

        setItem(23, shopPage.getGlassPane(true, 1), e -> {
            if (amount + 1 < MAX_AMOUNT) amount++;
            else amount = 64;
            update(shopPage, item, buy, page);
        });
        setItem(24, shopPage.getGlassPane(true, 4), e -> {
            if (amount + 4 < MAX_AMOUNT) amount += 4;
            else amount = 64;
            update(shopPage, item, buy, page);
        });
        setItem(32, shopPage.getGlassPane(true, 16), e -> {
            if (amount + 16 < MAX_AMOUNT) amount += 16;
            else amount = 64;
            update(shopPage, item, buy, page);
        });
        setItem(33, shopPage.getGlassPane(true, 32), e -> {
            if (amount + 32 < MAX_AMOUNT) amount += 32;
            else amount = 64;
            update(shopPage, item, buy, page);
        });

        setItem(20, shopPage.getGlassPane(false, 1), e -> {
            if (amount - 1 > 1) amount--;
            else amount = 1;
            update(shopPage, item, buy, page);
        });
        setItem(21, shopPage.getGlassPane(false, 4), e -> {
            if (amount - 4 > 1) amount -= 4;
            else amount = 1;
            update(shopPage, item, buy, page);
        });
        setItem(29, shopPage.getGlassPane(false, 16), e -> {
            if (amount - 16 > 1) amount -= 16;
            else amount = 1;
            update(shopPage, item, buy, page);
        });
        setItem(30, shopPage.getGlassPane(false, 32), e -> {
            if (amount - 32 > 1) amount -= 32;
            else amount = 1;
            update(shopPage, item, buy, page);
        });

        setItem(13, item.getItemStack(buy, amount), e -> {
            SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(e.getWhoClicked()
                    .getUniqueId());
            if (user == null) {
                e.getWhoClicked().sendMessage("§cUne erreur est survenue lors de la récupération de votre profil.");
                return;
            }
            if (buy) {
                if (ShopsManager.INSTANCE.getSpaceAvailableFor((Player) e.getWhoClicked(), item.getPureItemStack()) >=
                        amount) {
                    if (user.getMoney() >= item.price() * amount) {
                        user.removeMoney(item.price() * amount);
                        e.getWhoClicked().getInventory().addItem(new ItemStack(item.material(), amount));
                        e.getWhoClicked().sendMessage(Component.text("§aVous avez acheté §ex" + amount + " " +
                                item.material() + "§a pour §e" +
                                NumberFormat.getInstance().format(item.price() * amount) + "$."));
                    } else {
                        e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez d'argent pour " +
                                "acheter §ex" + amount + " " + item.getName() + "§c."));
                    }
                } else {
                    e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez de place dans votre " +
                            "inventaire pour acheter §ex" + amount + " " + item.getName() + "§c."));
                }
            } else {
                int amountInInventory = 0;
                for (ItemStack itemStack : e.getWhoClicked().getInventory().getContents()) {
                    if (itemStack == null) continue;
                    if (itemStack.getType() != item.material()) continue;
                    if (itemStack.hasItemMeta()) {
                        ItemMeta meta = itemStack.getItemMeta();
                        if (meta.hasDisplayName() && Objects.requireNonNull(meta.displayName()).
                                contains(Component.text(item.getName()))) {
                            amountInInventory += itemStack.getAmount();
                        }
                    }
                    amountInInventory += itemStack.getAmount();
                }
                if (amountInInventory >= amount) {
                    user.addMoney(item.sellPrice() * amount);
                    e.getWhoClicked().getInventory().removeItem(new ItemStack(item.material(), amount));
                    e.getWhoClicked().sendMessage(Component.text("§aVous avez vendu §ex" + amount + " " +
                            item.material() + "§a pour §e" + item.sellPrice() * amount + "$"));
                } else {
                    e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez d'items pour vendre" +
                            " §ex" + amount + " " + item.getName()));
                }
            }
            e.getWhoClicked().closeInventory();
        });

        setItem(0, CommonItemStacks.getCommonBack(), e ->
                new ShopPageInv(shopPage, page).open((Player) e.getWhoClicked()));

        if (buy) {
            setItem(40, ItemBuilder.copyOf(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 64))
                    .name("§6Acheter des stacks").build(), e ->
                    new ShopStacksInv(item, shopPage, page).open((Player) e.getWhoClicked()));
        } else {
            setItem(40, ItemBuilder.copyOf(new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 64))
                    .name("§6Tout vendre").build(), e -> {
                SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(e.getWhoClicked()
                        .getUniqueId());
                if (user == null) {
                    e.getWhoClicked().sendMessage(Component.text("§cUne erreur est survenue lors de la " +
                            "récupération de votre profil."));
                    return;
                }
                double sellPrice = ShopsManager.INSTANCE.getSellPrice(item.getPureItemStack());
                if (sellPrice <= 0) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous ne pouvez pas vendre cet item !"));
                    return;
                }
                int amount = ShopsManager.INSTANCE.getAmountOf((Player) e.getWhoClicked(), item.getPureItemStack());
                if (amount <= 0) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas cet item dans votre inventaire !"));
                    return;
                }
                double total = sellPrice * amount;
                user.addMoney(total);
                for (int i = 0; i < e.getWhoClicked().getInventory().getSize(); i++) {
                    ItemStack itemStack = e.getWhoClicked().getInventory().getItem(i);
                    if (itemStack == null) continue;
                    if (itemStack.isSimilar(item.getPureItemStack())) {
                        e.getWhoClicked().getInventory().setItem(i, null);
                    }
                }
                e.getWhoClicked().sendMessage(Component.text("§aVous avez vendu §e" + amount + " " +
                        item.getPureItemStack().getType().name() + "§a pour §e" +
                        NumberFormat.getInstance().format(total) + "$§a."));
                e.getWhoClicked().closeInventory();
            });
        }
    }
}
