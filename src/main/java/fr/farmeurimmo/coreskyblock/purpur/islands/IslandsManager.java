package fr.farmeurimmo.coreskyblock.purpur.islands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.bank.IslandsBankManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.chat.IslandsChatManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.levels.IslandsBlocksValues;
import fr.farmeurimmo.coreskyblock.purpur.islands.levels.IslandsLevelCalculator;
import fr.farmeurimmo.coreskyblock.purpur.islands.listeners.IslandsProtectionListener;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsGeneratorManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsMaxMembersManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsSizeManager;
import fr.farmeurimmo.coreskyblock.purpur.worlds.WorldsManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandRanksManager;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandSettings;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class IslandsManager {

    public static IslandsManager INSTANCE;
    public final Map<String, String> serversData = new HashMap<>();
    public final Map<UUID, String> awaitingResponseFromServer = new HashMap<>();
    public final Map<UUID, Long> awaitingResponseFromServerTime = new HashMap<>(); //for unload
    private final JavaPlugin plugin;
    private final ArrayList<UUID> isBypass = new ArrayList<>();
    private final ArrayList<UUID> deleteConfirmation = new ArrayList<>();
    private final Gson gson = new Gson();

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
        new IslandsCooldownManager();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
                if (island.isLoaded()) {
                    askOthersServersForMembersOnline(island);
                }
                if (island.needUpdate()) {
                    island.update(true);
                }
                // ASWM already saves the world
                /*if (island.isLoaded() && !gotUpdate) {
                    World world = Bukkit.getWorld(getIslandWorldName(island.getIslandUUID()));
                    if (world != null) {
                        world.save();
                    }
                }*/
            }
        }, 0, 20 * 60 * 5);

        WorldsManager.INSTANCE.loadAsync("island_template_1", true);

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () ->
                JedisManager.INSTANCE.publishToRedis("coreskyblock",
                        "island:space:" + CoreSkyblock.SERVER_NAME + ":" + getIslandsLoaded() + ":" + getTheoreticalMaxPlayersOnline() + ":" +
                                getActualPlayersOnline()), 0, 20);

        CoreSkyblock.INSTANCE.getServer().getPluginManager().registerEvents(new IslandsProtectionListener(), plugin);
    }

    private static int getLoadFactor(String[] data) {
        int islandsLoaded = Integer.parseInt(data[0]); // Assuming the number of islands loaded is the first data point
        int maxTheoreticalPlayers = Integer.parseInt(data[1]); // Assuming the max theoretical players is the second data point
        int onlinePlayers = Integer.parseInt(data[2]); // Assuming the online players is the third data point

        // Calculate the load factor. This is a simple sum, but you can replace this with any formula you want
        return islandsLoaded + maxTheoreticalPlayers + onlinePlayers;
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

    public Island getIslandByUUID(UUID uuid) {
        return IslandsDataManager.INSTANCE.getCache().get(uuid);
    }

    public int getIslandsLoaded() {
        int islandsLoaded = 0;
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (island.isLoaded()) {
                islandsLoaded++;
            }
        }
        return islandsLoaded;
    }

    public int getTheoreticalMaxPlayersOnline() {
        int maxPlayers = 0;
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (island.isLoaded()) {
                maxPlayers += IslandsMaxMembersManager.INSTANCE.getMaxMembersFromLevel(island.getMaxMembers());
            }
        }
        return maxPlayers;
    }

    public int getActualPlayersOnline() {
        int players = 0;
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (island.isLoaded()) {
                players += island.getOnlineMembers().size();
            }
        }
        return players;
    }

    public String getServerToLoadIsland() {
        String serverToLoad = null;
        int minLoadFactor = Integer.MAX_VALUE;
        for (Map.Entry<String, String> entry : serversData.entrySet()) {
            String[] data = entry.getValue().split(":");
            int loadFactor = getLoadFactor(data);

            if (loadFactor < minLoadFactor) {
                minLoadFactor = loadFactor;
                serverToLoad = entry.getKey();
            }
        }
        return serverToLoad;
    }

    public void checkForDataIntegrity(@Nullable String islandUUIDString, @Nullable UUID playerUUID, boolean forceLoad) {
        Island islandInLocalCache = getIslandOf(playerUUID);

        if (islandInLocalCache != null && islandInLocalCache.isLoaded()) return;

        CompletableFuture.runAsync(() -> {
            if (playerUUID != null) {
                String islandUUIDOfPlayerString = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:members:" + playerUUID);

                try {
                    UUID islandUUID = UUID.fromString(islandUUIDOfPlayerString);
                    loadFromRedis(islandUUID.toString());
                    checkIfIslandIsLoaded(islandUUID);
                    return;
                } catch (Exception ignored) {
                }
            } else {
                try {
                    UUID islandUUID = UUID.fromString(islandUUIDString);
                    loadFromRedis(islandUUID.toString());
                    checkIfIslandIsLoaded(islandUUID);
                    return;
                } catch (Exception ignored) {
                }
            }

            UUID islandUUID = null;
            if (playerUUID != null) {
                islandUUID = IslandsDataManager.INSTANCE.getIslandByMember(playerUUID);

                if (islandUUID == null) return;
            } else {
                try {
                    islandUUID = UUID.fromString(islandUUIDString);
                } catch (Exception ignored) {
                }
            }

            Island island = IslandsDataManager.INSTANCE.getIsland(islandUUID);

            if (island != null) {
                if (forceLoad) return;
                checkIfIslandIsLoaded(island.getIslandUUID());
                JedisManager.INSTANCE.sendToRedis("coreskyblock:island:" + island.getIslandUUID(), gson.toJson(island.toJson()));
                for (UUID member : island.getMembers().keySet()) {
                    JedisManager.INSTANCE.sendToRedis("coreskyblock:island:members:" + member, island.getIslandUUID().toString());
                }
            }
        });
    }

    public void loadFromRedis(String uuid) {
        String content = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:" + uuid);
        if (content == null) return;
        JsonObject json = new Gson().fromJson(content, JsonObject.class);
        Island island = Island.fromJson(json);
        if (island != null) {
            Island oldIsland = IslandsDataManager.INSTANCE.getCache().get(island.getIslandUUID());
            if (oldIsland != null) {
                oldIsland.sendMessageToAll("§cMise à jour des données read only de votre île.");
            }
            IslandsDataManager.INSTANCE.getCache().put(island.getIslandUUID(), island);
        }
    }

    public void checkIfIslandIsLoaded(UUID islandUUID) {
        Island island = getIslandByUUID(islandUUID);
        if (island != null && !island.isLoaded()) {
            String server = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:" + islandUUID + ":loaded");
            if (server == null) {

                server = getServerToLoadIsland();
                if (server == null || server.equalsIgnoreCase(CoreSkyblock.SERVER_NAME)) {
                    loadIsland(island);
                    return;
                }
                JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:remote_load:" + islandUUID + ":" + server);
            }
        }
    }

    public void onDisable() {
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            unload(island, false);
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
        UUID islandId = UUID.randomUUID();

        String serverToLoad = getServerToLoadIsland();
        if (serverToLoad == null) serverToLoad = CoreSkyblock.SERVER_NAME;

        if (serverToLoad.equalsIgnoreCase(CoreSkyblock.SERVER_NAME)) {
            create(owner, islandId, true);
            return;
        }

        JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:remote_create:" + serverToLoad + ":" + owner +
                ":" + islandId);
        awaitingResponseFromServer.put(islandId, serverToLoad);
    }

    public void setIslandLoadedAt(UUID uuid) {
        Island island = getIslandByUUID(uuid);
        if (island != null) {
            JedisManager.INSTANCE.sendToRedis("coreskyblock:island:" + island.getIslandUUID(), CoreSkyblock.SERVER_NAME);
        }
    }

    public void create(UUID owner, UUID islandUUID, boolean sameServer) {
        String worldName = getIslandWorldName(islandUUID);

        Island island = new Island(islandUUID, new Location(Bukkit.getWorld(worldName), 0.5, 80.1, 0.5,
                40, 0), owner);
        CompletableFuture.supplyAsync(() -> IslandsDataManager.INSTANCE.saveIsland(island)).thenAccept(result -> {
            if (!result) {
                JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:remote_create_response:" +
                        CoreSkyblock.SERVER_NAME + ":" + owner + ":error");
                return;
            }
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                SlimeWorld slimeWorld = WorldsManager.INSTANCE.cloneAndLoad(worldName, "island_template_1");
                if (slimeWorld == null) {
                    JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:remote_create_response:" +
                            CoreSkyblock.SERVER_NAME + ":" + owner + ":error");
                    IslandsDataManager.INSTANCE.deleteIsland(islandUUID);
                    return null;
                }
                island.setLoaded(true);
                IslandsDataManager.INSTANCE.getCache().put(islandUUID, island);
                World w = Bukkit.getWorld(worldName);
                if (w == null) return null;
                island.setLoaded(true);
                island.getSpawn().setWorld(w);
                w.setSpawnLocation(island.getSpawn());
                applyTimeAndWeather(w, island);
                IslandsSizeManager.INSTANCE.updateWorldBorder(island);
                CompletableFuture.runAsync(() -> {
                    JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:remote_create_response:" +
                            CoreSkyblock.SERVER_NAME + ":" + owner);
                    setIslandLoadedAt(islandUUID);
                    JedisManager.INSTANCE.sendToRedis("coreskyblock:island:" + islandUUID, gson.toJson(island.toJson()));
                    for (UUID member : island.getMembers().keySet()) {
                        JedisManager.INSTANCE.sendToRedis("coreskyblock:island:members:" + member, islandUUID.toString());
                    }
                });
                if (sameServer) {
                    Player ownerPlayer = plugin.getServer().getPlayer(owner);
                    if (ownerPlayer == null) return null;
                    ownerPlayer.teleportAsync(w.getSpawnLocation());
                    ownerPlayer.sendMessage(Component.text("§b[CoreSkyblock] §aVotre île a été créée avec succès !"));
                } else {
                    AtomicInteger iterations = new AtomicInteger(0);
                    Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, (task -> {
                        Player ownerPlayer = plugin.getServer().getPlayer(owner);
                        if (ownerPlayer != null) {
                            ownerPlayer.sendMessage(Component.text("§b[CoreSkyblock] §aTéléportation sur votre île..."));
                            ownerPlayer.teleportAsync(w.getSpawnLocation()).thenRun(() ->
                                    ownerPlayer.sendMessage(Component.text("§b[CoreSkyblock] §aVous avez été téléporté sur votre île.")));
                            task.cancel();
                        }
                        iterations.getAndIncrement();
                        //This method is called 4 times per second, so 40 iterations is 10 seconds of waiting
                        //If the player is not online after 10 seconds, we stop trying to teleport him
                        if (iterations.get() > 40) {
                            task.cancel();
                        }
                    }), 0, 5);
                }
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
            JedisManager.INSTANCE.sendToRedis("coreskyblock:island:" + island.getIslandUUID() + ":loaded", CoreSkyblock.SERVER_NAME);
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                island.setLoaded(true);
                Location spawn = island.getSpawn();
                World w = Bukkit.getWorld(getIslandWorldName(island.getIslandUUID()));
                if (spawn != null) {
                    spawn.setWorld(w);
                }
                applyTimeAndWeather(w, island);
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
        if (p == null) return;
        if (island == null) {
            p.sendMessage(Component.text("§cVous n'avez pas d'île."));
            return;
        }
        if (island.isLoaded()) {
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
            return;
        }
        String serverWhereIslandIsLoaded = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:" + island.getIslandUUID() + ":loaded");
        if (serverWhereIslandIsLoaded != null) {
            p.sendMessage(Component.text("§aTéléportation sur votre île..."));
            JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:teleport:" + p.getUniqueId() + ":"
                    + island.getIslandUUID() + ":" + serverWhereIslandIsLoaded);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(serverWhereIslandIsLoaded);
            p.sendPluginMessage(CoreSkyblock.INSTANCE, "BungeeCord", out.toByteArray());
        } else {
            p.sendMessage(Component.text("§cVotre île est en cours de chargement. Veuillez réessayer dans quelques secondes."));
            Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () ->
                    checkIfIslandIsLoaded(island.getIslandUUID()), 40);
        }
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
        CompletableFuture.runAsync(() -> {
            IslandsDataManager.INSTANCE.deleteIsland(island.getIslandUUID());
            JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:" + island.getIslandUUID());
            for (UUID member : island.getMembers().keySet()) {
                JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:members:" + member);
            }
            JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:" + island.getIslandUUID() + ":loaded");
        });
    }

    public void askOthersServersForMembersOnline(Island island) {
        awaitingResponseFromServerTime.put(island.getIslandUUID(), System.currentTimeMillis());

        CompletableFuture.runAsync(() -> {

            StringBuilder message = new StringBuilder("island:check_unload:" + island.getIslandUUID());
            for (UUID member : island.getMembers().keySet()) {
                message.append(":").append(member);
            }

            JedisManager.INSTANCE.publishToRedis("coreskyblock", message.toString());

            Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
                if (awaitingResponseFromServerTime.containsKey(island.getIslandUUID())) {
                    long time = awaitingResponseFromServerTime.get(island.getIslandUUID());
                    if (System.currentTimeMillis() - time > 1_000 * 60 * 10) {
                        Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                            awaitingResponseFromServerTime.remove(island.getIslandUUID());
                            unload(island, true);
                            return null;
                        });
                    }
                }
            }, 0, 5);
        });
    }

    public void unload(Island island, boolean async) {
        if (island.isLoaded()) {
            WorldsManager.INSTANCE.unload(getIslandWorldName(island.getIslandUUID()), true);
            island.setLoaded(false);
            if (async) CompletableFuture.runAsync(() -> {
                JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:" + island.getIslandUUID() + ":loaded");
                for (UUID member : island.getMembers().keySet()) {
                    JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:members:" + member);
                }
            });
            else {
                JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:" + island.getIslandUUID() + ":loaded");
                for (UUID member : island.getMembers().keySet()) {
                    JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:members:" + member);
                }
            }
        }
    }
}
