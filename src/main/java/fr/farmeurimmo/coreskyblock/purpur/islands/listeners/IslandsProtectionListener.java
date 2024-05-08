package fr.farmeurimmo.coreskyblock.purpur.islands.listeners;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.levels.IslandsBlocksValues;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandSettings;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.SpawnEggMeta;

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
        if (e.getTo().getY() < -64) {
            Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
            if (island != null) {
                IslandRanks rank = island.getPlayerRank(e.getPlayer().getUniqueId());
                if (rank == null || rank.isExternal()) {
                    e.setCancelled(true);
                    e.getPlayer().teleportAsync(CoreSkyblock.SPAWN).thenRun(() ->
                            e.getPlayer().sendMessage(Component.text("§cVous avez été téléporté au spawn car vous êtes tombé dans le vide.")));
                    return;
                }
                e.setCancelled(true);
                IslandsManager.INSTANCE.teleportToIsland(island, e.getPlayer());
                return;
            }
            e.setCancelled(true);
            e.getPlayer().teleportAsync(CoreSkyblock.SPAWN).thenRun(() ->
                    e.getPlayer().sendMessage(Component.text("§cVous avez été téléporté au spawn car vous êtes tombé dans le vide.")));
            return;
        }
        WorldBorder border = e.getPlayer().getWorld().getWorldBorder();
        if (e.getTo().getX() > border.getSize() / 2 + 0.2 || e.getTo().getZ() > border.getSize() / 2 + 0.2) {
            e.setCancelled(true);
            e.getPlayer().teleportAsync(CoreSkyblock.SPAWN).thenRun(() ->
                    e.getPlayer().sendMessage(Component.text("§cVous ne pouvez pas sortir de l'île.")));
            return;
        }
        if (e.getTo().getX() < -border.getSize() / 2 || e.getTo().getZ() < -border.getSize() / 2) {
            e.setCancelled(true);
            e.getPlayer().teleportAsync(CoreSkyblock.SPAWN).thenRun(() ->
                    e.getPlayer().sendMessage(Component.text("§cVous ne pouvez pas sortir de l'île.")));
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerTeleportEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getTo().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getTo().getWorld());
        if (island != null) {
            if (!island.isPublic()) {
                if (island.getCoops().containsKey(e.getPlayer().getUniqueId())) {
                    return;
                }
                if (!island.getMembers().containsKey(e.getPlayer().getUniqueId())) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(Component.text("§cCette île est privée, vous ne pouvez pas y accéder."));
                }
            }
            if (island.getBannedPlayers().contains(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Component.text("§cVous avez été banni de cette île."));
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
                preventExplosionOfValuesBlocks(e);
                return;
            }
            if (!island.hasSettingActivated(IslandSettings.MOB_GRIEFING)) {
                e.blockList().clear();
                e.setYield(0);
                return;
            }
            preventExplosionOfValuesBlocks(e);
        }
    }

    private void preventExplosionOfValuesBlocks(EntityExplodeEvent e) {
        IslandsBlocksValues.INSTANCE.getBlocksValues().forEach((block, value) -> {
            boolean shouldClear = false;
            for (Block b : e.blockList()) {
                if (b.getType() == block) {
                    shouldClear = true;
                    break;
                }
            }
            if (shouldClear) {
                e.blockList().clear();
                e.setYield(0);
                e.setCancelled(true);
            }
        });
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        if (e.getClickedBlock() == null) return;

        e.setUseItemInHand(PlayerInteractEvent.Result.ALLOW);
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island == null) return;

        Player p = e.getPlayer();
        IslandRanks rank = island.getPlayerRank(p.getUniqueId());
        Block block = e.getClickedBlock();
        if (e.getItem() != null) {
            if (e.getItem().getItemMeta() instanceof SpawnEggMeta) {
                if (!island.hasPerms(rank, IslandPerms.USE_SPAWN_EGG, p.getUniqueId())) {
                    e.setUseItemInHand(PlayerInteractEvent.Result.DENY);
                    e.setCancelled(true);
                    return;
                }
            }
        }
        if (block.getType() == Material.TRAPPED_CHEST) {
            if (!island.hasPerms(rank, IslandPerms.SECURED_CHEST, p.getUniqueId())) {
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setCancelled(true);
                return;
            }
        }
        if (block.getState() instanceof InventoryHolder) {
            if (!island.hasPerms(rank, IslandPerms.CONTAINER, p.getUniqueId())) {
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setCancelled(true);
                return;
            }
        }
        if (!island.hasPerms(rank, IslandPerms.INTERACT, p.getUniqueId())) {
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.BUILD, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.BREAK, p.getUniqueId())) {
                e.setCancelled(true);
            }
            if (!island.hasPerms(rank, IslandPerms.BREAK_ISLAND_LEVEL_BLOCKS, p.getUniqueId())) {
                if (IslandsBlocksValues.INSTANCE.getBlocksValues().containsKey(e.getBlock().getType())) {
                    e.setCancelled(true);
                }
            }
            if (e.getBlock().getType() == Material.SPAWNER) {
                if (!island.hasPerms(rank, IslandPerms.BREAK_SPAWNERS, p.getUniqueId())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!IslandsManager.INSTANCE.isAnIsland(e.getEntity().getWorld())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        World w = e.getWorld();
        if (!IslandsManager.INSTANCE.isAnIsland(w)) return;

        w.setGameRule(GameRule.KEEP_INVENTORY, true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.DROP_ITEMS, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemPickup(PlayerAttemptPickupItemEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.PICKUP_ITEMS, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!IslandsManager.INSTANCE.isAnIsland(e.getDamager().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getDamager().getWorld());
        if (island != null) {
            Player p = (Player) e.getDamager();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (e.getEntity() instanceof Player) {
                e.setCancelled(true);
                return;
            }
            if (e.getEntity() instanceof Animals) {
                if (!island.hasPerms(rank, IslandPerms.KILL_ANIMALS, p.getUniqueId())) {
                    e.setCancelled(true);
                }
                return;
            }
            if (e.getEntity() instanceof Mob) {
                if (!island.hasPerms(rank, IslandPerms.KILL_MOBS, p.getUniqueId())) {
                    e.setCancelled(true);
                }
                return;
            }
        }
    }

    @EventHandler
    public void interactAtEntity(PlayerInteractEntityEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (e.getRightClicked() instanceof Villager || e.getRightClicked() instanceof WanderingTrader) {
                if (!island.hasPerms(rank, IslandPerms.INTERACT_WITH_VILLAGERS, p.getUniqueId())) {
                    e.setCancelled(true);
                }
                return;
            }
            if (e.getRightClicked().isRidable()) {
                if (!island.hasPerms(rank, IslandPerms.INTERACT_WITH_MOUNTS, p.getUniqueId())) {
                    e.setCancelled(true);
                }
                return;
            }
            if (e.getRightClicked() instanceof ItemFrame) {
                if (!island.hasPerms(rank, IslandPerms.INTERACT_WITH_ITEM_FRAMES, p.getUniqueId())) {
                    e.setCancelled(true);
                }
                return;
            }
            if (e.getRightClicked() instanceof Animals) {
                if (e.getPlayer().getInventory().getItemInMainHand().getType().isEdible()) {
                    if (!island.hasPerms(rank, IslandPerms.FEED_ANIMALS, p.getUniqueId())) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onStartFishing(PlayerFishEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.FISH, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onThrow(PlayerEggThrowEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.USE_SPAWN_EGG, p.getUniqueId())) {
                e.setHatching(false);
            }
        }
    }

    @EventHandler
    public void onPlayerFlightToggle(PlayerToggleFlightEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getPlayer().getWorld())) return;
        if (e.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getPlayer().getWorld());
        if (island != null) {
            Player p = e.getPlayer();
            IslandRanks rank = island.getPlayerRank(p.getUniqueId());
            if (!island.hasPerms(rank, IslandPerms.FLY, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityMove(EntityMoveEvent e) {
        if (!IslandsManager.INSTANCE.isAnIsland(e.getEntity().getWorld())) return;
        if (e.getEntity() instanceof Player) return;
        if (e.getEntity().getLocation().getY() < -64) {
            e.getEntity().damage(50);
        }
    }
}
