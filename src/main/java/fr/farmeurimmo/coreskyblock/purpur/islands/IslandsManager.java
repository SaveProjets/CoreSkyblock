package fr.farmeurimmo.coreskyblock.purpur.islands;

import com.infernalsuite.aswm.api.world.SlimeWorld;
import fr.farmeurimmo.coreskyblock.common.islands.Island;
import fr.farmeurimmo.coreskyblock.common.islands.IslandRanksManager;
import fr.farmeurimmo.coreskyblock.common.islands.IslandSettings;
import fr.farmeurimmo.coreskyblock.common.islands.IslandsDataManager;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.listeners.IslandsProtectionListener;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsGeneratorManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsMaxMembersManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsSizeManager;
import fr.farmeurimmo.coreskyblock.purpur.worlds.WorldsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandsManager {

    public static IslandsManager INSTANCE;
    private final JavaPlugin plugin;
    private final ArrayList<UUID> isBypass = new ArrayList<>();
    private final ArrayList<UUID> deleteConfirmation = new ArrayList<>();

    public IslandsManager(JavaPlugin plugin) {
        INSTANCE = this;
        this.plugin = plugin;

        new IslandsDataManager();

        new IslandRanksManager();
        new IslandsGeneratorManager();
        new IslandsSizeManager();
        new IslandsMaxMembersManager();

        new IslandsBlocksValues();
        new IslandsBankManager();
        new IslandsChatManager();
        new IslandsLevelCalculator();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
                boolean gotUpdate = false;
                if (island.isLoadTimeout() && island.isLoaded()) {
                    WorldsManager.INSTANCE.unload(getIslandWorldName(island.getIslandUUID()), true);
                    island.setLoaded(false);
                    gotUpdate = true;
                }
                if (island.needUpdate()) {
                    island.update(true);
                }
                if (island.isLoaded() && !gotUpdate) {
                    World world = Bukkit.getWorld(getIslandWorldName(island.getIslandUUID()));
                    if (world != null) {
                        world.save();
                    }
                }
            }
        }, 0, 20 * 60 * 5);

        WorldsManager.INSTANCE.loadAsync("island_template_1", true);

        CoreSkyblock.INSTANCE.getServer().getPluginManager().registerEvents(new IslandsProtectionListener(), plugin);
    }

    public Island getIslandByLoc(World world) {
        if (world == null) return null;
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (island.getSpawn() != null && island.getSpawn().getWorld() != null &&
                    island.getSpawn().getWorld().equals(world)) {
                return island;
            }
        }
        return null;
    }

    public void checkLoadedIsland(Player p) {
        Island island = getIslandOf(p.getUniqueId());
        if (island != null) {
            if (!island.isLoaded()) {
                loadIsland(island);
            }
        }
    }

    public void checkUnloadIsland(Player p) {
        Island island = getIslandOf(p.getUniqueId());
        if (island != null) {
            if (island.isLoaded()) {
                island.setLoadTimeout(System.currentTimeMillis());
            }
        }
    }

    public void onDisable() {
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (island.isLoaded()) {
                WorldsManager.INSTANCE.unload(getIslandWorldName(island.getIslandUUID()), true);
                island.setLoaded(false);
            }
            if (island.needUpdate()) {
                island.update(false);
            }
        }
    }

    public boolean isBypassing(UUID uuid) {
        return isBypass.contains(uuid);
    }

    public void setBypass(UUID uuid, boolean bypass) {
        if (bypass) {
            isBypass.add(uuid);
        } else {
            isBypass.remove(uuid);
        }
    }

    public void createIsland(UUID owner) {
        long startTime = System.currentTimeMillis();
        UUID islandId = UUID.randomUUID();
        String worldName = getIslandWorldName(islandId);

        Player player = plugin.getServer().getPlayer(owner);
        if (player == null) return;
        player.sendMessage(Component.text("§b[CoreSkyblock] §aCréation de votre île..."));

        Island island = new Island(islandId, new Location(Bukkit.getWorld(worldName), 0.5, 80.1, 0.5,
                40, 0), owner);
        CompletableFuture.supplyAsync(() -> IslandsDataManager.INSTANCE.saveIsland(island)).thenAccept(result -> {
            if (!result) {
                player.sendMessage(Component.text("§cUne erreur est survenue lors de la création de votre île."));
                player.sendMessage(Component.text("§cVeuillez réessayer plus tard."));
                return;
            }
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                SlimeWorld slimeWorld = WorldsManager.INSTANCE.cloneAndLoad(worldName, "island_template_1");
                if (slimeWorld == null) {
                    player.sendMessage(Component.text("§cUne erreur est survenue lors de la création de votre île."));
                    player.sendMessage(Component.text("§cVeuillez réessayer plus tard."));
                    return null;
                }
                IslandsDataManager.INSTANCE.getCache().put(islandId, island);
                World w = Bukkit.getWorld(worldName);
                if (w == null) return null;
                island.setLoaded(true);
                island.getSpawn().setWorld(w);
                w.setSpawnLocation(island.getSpawn());
                applyTimeAndWeather(w, island);
                IslandsSizeManager.INSTANCE.updateWorldBorder(island);
                Player ownerPlayer = plugin.getServer().getPlayer(owner);
                if (ownerPlayer == null) return null;
                ownerPlayer.teleportAsync(w.getSpawnLocation());
                ownerPlayer.sendMessage(Component.text("§b[CoreSkyblock] §aVotre île a été créée en " +
                        (System.currentTimeMillis() - startTime) + "ms"));
                return null;
            });
        });
    }

    public String getIslandWorldName(UUID islandUUID) {
        return "island_" + islandUUID;
    }

    public World getIslandWorld(UUID islandUUID) {
        return Bukkit.getWorld(getIslandWorldName(islandUUID));
    }

    public Island getIslandOf(UUID uuid) {
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (island.getMembers().containsKey(uuid)) return island;
        }
        return null;
    }

    public void applyTimeAndWeather(World world, Island island) {
        if (world != null) {
            if (!island.hasSettingActivated(IslandSettings.TIME_DEFAULT)) {
                for (IslandSettings setting : island.getSettings()) {
                    if (setting.name().contains("TIME") && island.hasSettingActivated(setting)) {
                        world.setTime(setting.getTime());
                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                        break;
                    }
                }
            } else {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            }
            if (!island.hasSettingActivated(IslandSettings.WEATHER_DEFAULT)) {
                for (IslandSettings setting : island.getSettings()) {
                    if (setting.name().contains("WEATHER") && island.hasSettingActivated(setting)) {
                        world.setStorm(setting == IslandSettings.WEATHER_RAIN);
                        world.setThundering(setting == IslandSettings.WEATHER_RAIN);
                        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                        break;
                    }
                }
            } else {
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
                world.setStorm(false);
                world.setThundering(false);
            }
        }
    }

    public void loadIsland(Island island) {
        if (island.isLoaded()) return;
        WorldsManager.INSTANCE.loadAsync(getIslandWorldName(island.getIslandUUID()), false).thenRun(() -> {
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                Location spawn = island.getSpawn();
                World w = Bukkit.getWorld(getIslandWorldName(island.getIslandUUID()));
                if (spawn != null) {
                    spawn.setWorld(w);
                }
                applyTimeAndWeather(w, island);
                island.setLoaded(true);
                IslandsSizeManager.INSTANCE.updateWorldBorder(island);

                island.sendMessageToAll("§aVotre île a été chargée.");
                return null;
            });
        });
    }

    public boolean isAnIsland(World world) {
        return world.getName().startsWith("island_");
    }

    public void teleportToIsland(Island island, Player p) {
        if (island == null) {
            p.sendMessage(Component.text("§cVous n'avez pas d'île."));
            return;
        }
        long startTime = System.currentTimeMillis();
        p.sendMessage(Component.text("§aTéléportation en cours..."));
        p.teleportAsync(island.getSpawn()).thenAccept(result -> {
            if (result) {
                p.sendMessage(Component.text("§aVous avez été téléporté sur votre île. ("
                        + (System.currentTimeMillis() - startTime) + "ms)"));
            } else {
                p.sendMessage(Component.text("§cUne erreur est survenue lors de la téléportation. ("
                        + (System.currentTimeMillis() - startTime) + "ms)"));
            }
        });
        IslandsSizeManager.INSTANCE.updateWorldBorder(island);
    }

    public void checkForPlayerOnAccessibilityChange(Island island) {
        if (island != null) {
            if (island.isPublic()) return;
            World world = Bukkit.getWorld(getIslandWorldName(island.getIslandUUID()));
            if (world != null) {
                ArrayList<Player> players = new ArrayList<>(island.getOnlineMembers());
                for (Player p : world.getPlayers()) {
                    if (players.contains(p)) continue;
                    p.teleportAsync(CoreSkyblock.SPAWN);
                    p.sendMessage(Component.text("§cCette île est maintenant privée."));
                }
            }
        }
    }

    public ArrayList<UUID> getDeleteConfirmation() {
        return deleteConfirmation;
    }

    public void deleteIsland(Island island) {
        if (island == null) return;
        if (island.isLoaded()) {
            World world = Bukkit.getWorld(getIslandWorldName(island.getIslandUUID()));
            if (world != null) {
                for (Player p : world.getPlayers()) {
                    p.teleportAsync(CoreSkyblock.SPAWN).thenRun(() -> p.sendMessage(Component
                            .text("§cVous avez été téléporté au spawn car votre île a été supprimée.")));
                }
            }
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    WorldsManager.INSTANCE.unload(getIslandWorldName(island.getIslandUUID()), true), 20);
            island.setLoaded(false);
        }
        CompletableFuture.runAsync(() -> IslandsDataManager.INSTANCE.deleteIsland(island.getIslandUUID()));
    }
}
