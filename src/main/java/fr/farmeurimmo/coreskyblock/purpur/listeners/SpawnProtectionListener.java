package fr.farmeurimmo.coreskyblock.purpur.listeners;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SpawnProtectionListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!CoreSkyblock.INSTANCE.isASpawn(e.getLocation().getWorld())) return;
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (!CoreSkyblock.INSTANCE.isASpawn(e.getBlock().getWorld())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!CoreSkyblock.INSTANCE.isASpawn(e.getLocation().getWorld())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!CoreSkyblock.INSTANCE.isASpawn(e.getBlock().getWorld())) return;
        if (CoreSkyblock.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!CoreSkyblock.INSTANCE.isASpawn(e.getBlock().getWorld())) return;
        if (CoreSkyblock.INSTANCE.buildModePlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!CoreSkyblock.INSTANCE.isASpawn(e.getPlayer().getWorld())) return;
        if (e.getTo().getY() < -64) {
            e.setCancelled(true);
            e.getPlayer().teleportAsync(CoreSkyblock.SPAWN).thenRun(() ->
                    e.getPlayer().sendMessage(Component.text("§cVous avez été téléporté au spawn car vous êtes tombé dans le vide.")));
            return;
        }
    }
}
