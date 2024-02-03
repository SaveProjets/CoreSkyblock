package fr.farmeurimmo.skylyblock.purpur.minions;

import fr.farmeurimmo.skylyblock.purpur.islands.IslandsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MinionsListener implements Listener {

    @EventHandler
    public void pistonExtend(BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks()) {
            for (Entity entity : b.getWorld().getNearbyEntities(b.getLocation(), 1, 1, 1)) {
                if (entity.hasMetadata("minion")) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void pistonRetract(BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks()) {
            for (Entity entity : b.getWorld().getNearbyEntities(b.getLocation(), 1, 1, 1)) {
                if (entity.hasMetadata("minion")) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!e.getEntity().hasMetadata("minion")) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (!e.getRightClicked().hasMetadata("minion")) return;

        e.setCancelled(true);

        Player p = e.getPlayer();

        if (!IslandsManager.INSTANCE.isAnIsland(p.getWorld())) {
            p.sendMessage(Component.text("§b[SkylyBlock] §cVous ne pouvez pas utiliser de minions en dehors de votre île !"));
            return;
        }

        Minion minion;
        //FIXME: MINIONS
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (!e.getItem().hasItemMeta()) return;
        if (!e.getItem().getItemMeta().hasDisplayName()) return;
        if (!e.getItem().isUnbreakable()) return;

        Player p = e.getPlayer();

        ItemStack item = e.getItem();

        if (!MinionsManager.INSTANCE.isAMinion(item)) return;

        e.setCancelled(true);

        if (!IslandsManager.INSTANCE.isAnIsland(p.getWorld())) {
            p.sendMessage(Component.text("§b[SkylyBlock] §cVous ne pouvez pas utiliser les minions en dehors de votre île !"));
            return;
        }

        p.sendMessage(Component.text("§b[SkylyBlock] §cLes minions sont désactivés pour le moment !"));
    }
}
