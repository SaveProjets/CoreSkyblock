package fr.farmeurimmo.mineblock.purpur.islands.listeners;

import fr.farmeurimmo.mineblock.common.islands.Island;
import fr.farmeurimmo.mineblock.common.islands.IslandSettings;
import fr.farmeurimmo.mineblock.purpur.MineBlock;
import fr.farmeurimmo.mineblock.purpur.islands.IslandsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.WorldBorder;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class IslandsProtectionListener implements Listener {

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getBlock().getWorld())) return;
        WorldBorder border = e.getBlock().getWorld().getWorldBorder();
        if (e.getToBlock().getX() > border.getSize() / 2 || e.getToBlock().getZ() > border.getSize() / 2) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        WorldBorder border = e.getPlayer().getWorld().getWorldBorder();
        if (e.getTo().getX() > border.getSize() / 2 + 0.2 || e.getTo().getZ() > border.getSize() / 2 + 0.2) {
            e.setCancelled(true);
            e.getPlayer().teleportAsync(MineBlock.SPAWN).thenRun(() ->
                    e.getPlayer().sendMessage(Component.text("§cVous ne pouvez pas sortir de l'île.")));
            return;
        }
        if (e.getTo().getX() < -border.getSize() / 2 || e.getTo().getZ() < -border.getSize() / 2) {
            e.setCancelled(true);
            e.getPlayer().teleportAsync(MineBlock.SPAWN).thenRun(() ->
                    e.getPlayer().sendMessage(Component.text("§cVous ne pouvez pas sortir de l'île.")));
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerTeleportEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getTo().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getTo().getWorld());
        if (island != null && !island.isPublic()) {
            if (!island.getMembers().containsKey(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Component.text("§cCette île est privée, vous ne pouvez pas y accéder."));
            }
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getWorld())) return;
        if (e.getCause() == WeatherChangeEvent.Cause.PLUGIN) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getWorld());
        if (island != null && !island.hasSettingActivated(IslandSettings.WEATHER_DEFAULT)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getBlock().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getBlock().getWorld());
        if (island != null && !island.hasSettingActivated(IslandSettings.BLOCK_BURNING)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM)
            return;
        if (e.getEntityType() == org.bukkit.entity.EntityType.ARMOR_STAND) return;

        if (!IslandsManager.INSTANCE.isAnIsland(e.getEntity().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getEntity().getWorld());
        if (island != null && !island.hasSettingActivated(IslandSettings.MOB_SPAWNING)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLightningStrike(LightningStrikeEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getLightning().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getLightning().getWorld());
        if (island != null && !island.hasSettingActivated(IslandSettings.LIGHTNING_STRIKE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.PHANTOM) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySteal(EntityChangeBlockEvent e) {
        if (e.getEntity() instanceof org.bukkit.entity.Player) return;
        if (!IslandsManager.INSTANCE.isAnIsland(e.getBlock().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getBlock().getWorld());
        if (island != null && !island.hasSettingActivated(IslandSettings.MOB_GRIEFING)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getBlock().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getBlock().getWorld());
        if (island != null && !island.hasSettingActivated(IslandSettings.BLOCK_EXPLOSION)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getLocation().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getLocation().getWorld());
        if (island != null) {
            if (e.getEntity() instanceof TNTPrimed) {
                if (!island.hasSettingActivated(IslandSettings.BLOCK_EXPLOSION)) {
                    e.setYield(0);
                    e.blockList().clear();
                }
                return;
            }
            if (!island.hasSettingActivated(IslandSettings.MOB_GRIEFING)) {
                e.blockList().clear();
                e.setYield(0);
            }
        }
    }

    @EventHandler
    public void onBlockSpread(BlockIgniteEvent e) {
        if (e.getCause() != BlockIgniteEvent.IgniteCause.SPREAD) return;
        if (!IslandsManager.INSTANCE.isAnIsland(e.getBlock().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getBlock().getWorld());
        if (island != null && !island.hasSettingActivated(IslandSettings.BLOCK_BURNING)) {
            e.setCancelled(true);
        }
    }
}
