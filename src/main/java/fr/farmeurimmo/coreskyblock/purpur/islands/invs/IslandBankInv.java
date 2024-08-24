package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsCooldownManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.bank.IslandsBankManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IslandBankInv extends FastInv {

    public IslandBankInv(Island island) {
        super(36, "§8Banque de l'île");

        setItem(31, CommonItemStacks.getCommonBack(), e ->
                new IslandInv(island).open((Player) e.getWhoClicked()));

        CommonItemStacks.applyCommonPanes(Material.RED_STAINED_GLASS_PANE, getInventory());

        setItem(13, ItemBuilder.copyOf(new ItemStack(Material.RAW_GOLD)).name("§6Argent de l'île")
                .lore("", "§aClic droit pour déposer de l'argent", "§cClic gauche pour retirer de l'argent").build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            long cooldown = IslandsCooldownManager.INSTANCE.getCooldownLeft(island.getIslandUUID(), "island-bank");
            if (cooldown >= 0) {
                e.getWhoClicked().sendMessage(Component.text("§cVous devez attendre " + cooldown +
                        " secondes avant de pouvoir réutiliser la banque de votre île."));
                return;
            }
            if (e.isLeftClick()) {
                e.getWhoClicked().sendMessage(Component.text("§aMerci d'entrer le montant que vous " +
                        "souhaitez §cretirer§a. Tapez §2cancel§a pour annuler."));
                IslandsBankManager.INSTANCE.addAwaitingAmount(e.getWhoClicked().getUniqueId(), false);
                e.getWhoClicked().closeInventory();
            } else {
                e.getWhoClicked().sendMessage(Component.text("§aMerci d'entrer le montant que vous " +
                        "souhaitez §2déposer§a. Tapez §2cancel§a pour annuler."));
                IslandsBankManager.INSTANCE.addAwaitingAmount(e.getWhoClicked().getUniqueId(), true);
                e.getWhoClicked().closeInventory();
            }
        });
    }
}
