package fr.farmeurimmo.coreskyblock.purpur.blocks.elevators;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class ElevatorsListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        if (ElevatorsManager.INSTANCE.isAnElevator(e.getItemInHand())) {
            p.sendMessage(Component.text("§aAscenseur placé."));
            e.getBlockPlaced().setMetadata("elevator", new FixedMetadataValue(CoreSkyblock.INSTANCE, true));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().hasMetadata("elevator")) {
            e.getPlayer().sendMessage(Component.text("§cAscenseur cassé."));
            ElevatorsManager.INSTANCE.giveElevator(e.getPlayer());
        }
    }

    //when a player is jumping
    @EventHandler
    public void onPlayerJump(PlayerJumpEvent e) {
        Player p = e.getPlayer();

        Block b = p.getLocation().add(0, -1, 0).getBlock();
        if (b.hasMetadata("elevator")) {
            if (ElevatorsManager.INSTANCE.usingElevator.contains(p.getUniqueId())) return;

            for (int y = b.getLocation().getBlockY() + 1; y < 320; y++) {
                Block b2 = b.getWorld().getBlockAt(b.getLocation().getBlockX(), y, b.getLocation().getBlockZ());
                if (b2.hasMetadata("elevator")) {
                    if (b2.getLocation().add(0, 1, 0).getBlock().getType().isSolid()) {
                        p.sendMessage(Component.text("§cUn obstacle vous empêche de monter à un étage."));
                        continue;
                    }
                    p.teleport(b2.getLocation().add(0.5, 1, 0.5));
                    p.sendMessage(Component.text("§aVous avez été téléporté à l'étage supérieur."));

                    ElevatorsManager.INSTANCE.addUsingElevator(p);
                    break;
                }
            }
        }
    }

    //when a player is sneaking
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();

        Block b = p.getLocation().add(0, -1, 0).getBlock();
        if (b.hasMetadata("elevator")) {
            if (ElevatorsManager.INSTANCE.usingElevator.contains(p.getUniqueId())) return;

            for (int y = b.getLocation().getBlockY() - 1; y > -64; y--) {
                Block b2 = b.getWorld().getBlockAt(b.getLocation().getBlockX(), y, b.getLocation().getBlockZ());
                if (b2.hasMetadata("elevator")) {
                    if (b2.getLocation().add(0, 1, 0).getBlock().getType().isSolid()) {
                        p.sendMessage(Component.text("§cUn obstacle vous empêche de descendre à un étage."));
                        continue;
                    }
                    p.teleport(b2.getLocation().add(0.5, 1, 0.5));
                    p.sendMessage(Component.text("§aVous avez été téléporté à l'étage inférieur."));

                    ElevatorsManager.INSTANCE.addUsingElevator(p);
                    break;
                }
            }
        }
    }
}
