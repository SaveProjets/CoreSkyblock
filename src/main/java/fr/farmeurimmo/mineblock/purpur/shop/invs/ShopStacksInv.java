package fr.farmeurimmo.mineblock.purpur.shop.invs;

import fr.farmeurimmo.mineblock.common.skyblockusers.SkyblockUser;
import fr.farmeurimmo.mineblock.common.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.mineblock.purpur.shop.ShopsManager;
import fr.farmeurimmo.mineblock.purpur.shop.objects.ShopItem;
import fr.farmeurimmo.mineblock.purpur.shop.objects.ShopPage;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;

public class ShopStacksInv extends FastInv {

    private final static int[] SLOTS_AVAILABLE = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28,
            29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    public ShopStacksInv(ShopItem item, ShopPage shopPage, int page) {
        super(54, "§8Boutique - Quantité d'achat en stacks");

        update(shopPage, item, page);
    }

    private void update(ShopPage shopPage, ShopItem item, int page) {
        setItems(new int[]{9, 0, 1, 7, 8, 17, 52, 53, 44, 36, 45, 46}, ItemBuilder.copyOf(new ItemStack(
                Material.LIME_STAINED_GLASS_PANE)).name("§6").build());

        int currentStacks = 2;
        for (int j : SLOTS_AVAILABLE) {
            int finalCurrentStacks = currentStacks;
            setItem(j, item.getItemStackForStackBuy(finalCurrentStacks), e -> {
                if (ShopsManager.INSTANCE.getSpaceAvailableFor((Player) e.getWhoClicked(), item.getPureItemStack()) >=
                        (finalCurrentStacks * item.getPureItemStack().getMaxStackSize())) {
                    SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(((Player) e.getWhoClicked())
                            .getUniqueId());
                    if (user == null) {
                        e.getWhoClicked().sendMessage(Component.text("§cUne erreur est survenue lors de la " +
                                "récupération de votre profil."));
                        return;
                    }
                    if (user.getMoney() < (finalCurrentStacks * item.price())) {
                        e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez d'argent pour " +
                                "acheter " + finalCurrentStacks + " stacks de " + item.getName() + "."));
                        return;
                    }
                    user.setMoney(user.getMoney() - (finalCurrentStacks * item.price()));
                    ItemStack itemStack = item.getPureItemStack();
                    itemStack.setAmount(itemStack.getMaxStackSize());
                    for (int i = 0; i < finalCurrentStacks; i++) {
                        e.getWhoClicked().getInventory().addItem(itemStack);
                    }
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage(Component.text("§aVous avez acheté §ex" + finalCurrentStacks +
                            " stacks de " + item.getName() + "§a pour §e" + NumberFormat.getInstance()
                            .format(finalCurrentStacks * item.price()) + "$."));
                } else {
                    e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas assez de place dans votre " +
                            "inventaire pour acheter " + finalCurrentStacks + " stacks de " + item.getName() + "."));
                }
            });
            currentStacks += 1;
            if (currentStacks > 22) currentStacks++;
        }

        setItem(0, ItemBuilder.copyOf(new ItemStack(Material.IRON_DOOR)).name("§6Retour").build(), e -> {
            new ShopAmountInv(item, true, shopPage, page).open((Player) e.getWhoClicked());
        });
    }
}
