package fr.farmeurimmo.coreskyblock.purpur.blocks.elevators;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class ElevatorsListener implements Listener {

    @EventHandler
    public void onBlockPlace(CustomBlockPlaceEvent e) {
        if (ElevatorsManager.INSTANCE.isAnElevator(e.getNamespacedID())) {
            e.getPlayer().sendMessage(Component.text("§aAscenseur placé."));
        }
    }

    @EventHandler
    public void onBlockBreak(CustomBlockBreakEvent e) {
        if (ElevatorsManager.INSTANCE.isAnElevator(CustomBlock.byAlreadyPlaced(e.getBlock()))) {
            e.getPlayer().sendMessage(Component.text("§cAscenseur cassé."));
        }
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent e) {
        Player p = e.getPlayer();

        CustomBlock b1 = CustomBlock.byAlreadyPlaced(p.getLocation().add(0, -1, 0).getBlock());
        if (ElevatorsManager.INSTANCE.isAnElevator(b1)) {
            if (ElevatorsManager.INSTANCE.usingElevator.contains(p.getUniqueId())) return;

            int yPrev = b1.getBlock().getLocation().getBlockY() + ElevatorsManager.MAX_ELEVATOR_HEIGHT;
            if (yPrev > 320) yPrev = 320;
            for (int y = b1.getBlock().getLocation().getBlockY() + 1; y < yPrev; y++) {
                CustomBlock customBlock = CustomBlock.byAlreadyPlaced(b1.getBlock().getWorld().getBlockAt(
                        b1.getBlock().getLocation().getBlockX(), y, b1.getBlock().getLocation().getBlockZ()));

                if (customBlock == null) continue;

                if (customBlock.getBlock().getLocation().add(0, 1, 0).getBlock().getType().isSolid()) {
                    p.sendMessage(Component.text("§cUn obstacle vous empêche de monter à un étage."));
                    continue;
                }
                if (customBlock.getBlock().getLocation().add(0, 2, 0).getBlock().getType().isSolid()) {
                    p.sendMessage(Component.text("§cUn obstacle vous empêche de monter à un étage."));
                    continue;
                }
                Location loc = customBlock.getBlock().getLocation().add(0.5, 1, 0.5);
                e.setCancelled(true);
                loc.setDirection(p.getLocation().getDirection());
                p.teleportAsync(loc);

                ElevatorsManager.INSTANCE.addUsingElevator(p);
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();

        CustomBlock b1 = CustomBlock.byAlreadyPlaced(p.getLocation().add(0, -1, 0).getBlock());
        if (ElevatorsManager.INSTANCE.isAnElevator(b1)) {
            if (ElevatorsManager.INSTANCE.usingElevator.contains(p.getUniqueId())) return;

            int yPrev = b1.getBlock().getLocation().getBlockY() - ElevatorsManager.MAX_ELEVATOR_HEIGHT;
            if (yPrev < -64) yPrev = -64;
            for (int y = b1.getBlock().getLocation().getBlockY() - 1; y > yPrev; y--) {
                CustomBlock customBlock = CustomBlock.byAlreadyPlaced(b1.getBlock().getWorld().getBlockAt(
                        b1.getBlock().getLocation().getBlockX(), y, b1.getBlock().getLocation().getBlockZ()));

                if (customBlock == null) continue;

                if (customBlock.getBlock().getLocation().add(0, 1, 0).getBlock().getType().isSolid()) {
                    p.sendMessage(Component.text("§cUn obstacle vous empêche de descendre à un étage."));
                    continue;
                }
                if (customBlock.getBlock().getLocation().add(0, 2, 0).getBlock().getType().isSolid()) {
                    p.sendMessage(Component.text("§cUn obstacle vous empêche de descendre à un étage."));
                    continue;
                }

                if (!e.isSneaking()) return;

                Location loc = customBlock.getBlock().getLocation().add(0.5, 1, 0.5);
                loc.setDirection(p.getLocation().getDirection());
                p.teleportAsync(loc);

                ElevatorsManager.INSTANCE.addUsingElevator(p);
                break;
            }
        }
    }
}
