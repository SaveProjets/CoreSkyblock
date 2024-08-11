package fr.farmeurimmo.coreskyblock.purpur.items.sacs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CustomSacsListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (SacsManager.INSTANCE.isASacs(e.getItemInHand())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;

        if (SacsManager.INSTANCE.isASacs(e.getItem())) {
            SacsType sacsType = SacsManager.INSTANCE.getSacsType(e.getItem());
            if (sacsType == null) return;
            e.setUseInteractedBlock(PlayerInteractEvent.Result.DENY);
            new SacInv(e.getItem(), sacsType).open(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerAttemptPickup(PlayerAttemptPickupItemEvent e) {
        if (e.isCancelled()) return;
        SacsType sacsType = SacsType.getByMaterial(e.getItem().getItemStack().getType());

        if (sacsType != null) {
            Player p = e.getPlayer();
            for (ItemStack itemStack : p.getInventory().getStorageContents()) {
                if (itemStack == null) continue;
                if (!itemStack.hasItemMeta()) continue;
                if (!itemStack.hasLore()) continue;
                if (!itemStack.isUnbreakable()) continue;

                if (SacsManager.INSTANCE.isASacs(itemStack, sacsType)) {
                    int amount = SacsManager.INSTANCE.getAmount(itemStack, sacsType);

                    if (amount == -1) continue;

                    int newAmount = amount + Objects.requireNonNull(CoreSkyblock.INSTANCE.roseStackerAPI.getStackedItem(e.getItem())).getStackSize();

                    if (newAmount > SacsManager.MAX_AMOUNT) {
                        amount -= newAmount - SacsManager.MAX_AMOUNT;
                        newAmount = SacsManager.MAX_AMOUNT;
                        e.getItem().getItemStack().setAmount(amount);
                    } else {
                        e.setCancelled(true);
                        e.getItem().remove();
                    }

                    SacsManager.INSTANCE.setAmount(itemStack, sacsType, newAmount);
                }
            }
        }
    }
}
