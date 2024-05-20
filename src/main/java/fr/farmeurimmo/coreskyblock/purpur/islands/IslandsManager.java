package fr.farmeurimmo.coreskyblock.purpur.islands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import fr.farmeurimmo.coreskyblock.ServerType;
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
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandSettings;
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
    public final Map<String, Integer> serversData = new HashMap<>();
    public final Map<UUID, String> awaitingResponseFromServer = new HashMap<>();
    public final Map<UUID, Long> awaitingResponseFromServerTime = new HashMap<>(); //for unload
    public final Map<UUID, UUID> teleportToIsland = new HashMap<>();
    private final JavaPlugin plugin;
    private final ArrayList<UUID> isBypass = new ArrayList<>();
    private final ArrayList<UUID> isSpying = new ArrayList<>();
    private final ArrayList<UUID> deleteConfirmation = new ArrayList<>();

    public IslandsManager(JavaPlugin plugin) {
        INSTANCE = this;
        this.plugin = plugin;

        new IslandsDataManager();
        new IslandsWarpManager();

        new IslandRanksManager();
        new IslandsCoopsManager();
        new IslandsGeneratorManager();
        new IslandsSizeManager();
        new IslandsMaxMembersManager();

        new IslandsBlocksValues();
        new IslandsBankManager();
        new IslandsChatManager();
        new IslandsLevelCalculator();
        new IslandsCooldownManager();

        new IslandsTopManager();

        if (CoreSkyblock.SERVER_TYPE == ServerType.GAME) {

            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
                    if (island.needUpdate()) {
                        island.update(true);
                    }
                }
            }, 0, 20 * 30);

            Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, () -> {
                for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
                    if (island.isLoaded()) {
                        askOthersServersForMembersOnline(island);
                    }
                    if (island.isLoadTimeout()) {
                        unload(island, true, false);
                    }
                }
            }, 0, 20 * 30);

            Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, () -> {
                for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
                    if (island.isLoaded()) {
                        World world = getIslandWorld(island.getIslandUUID());
                        if (world != null) {
                            world.save();
                        }
                    }
                }
            }, 0, 20 * 60 * 3);

            WorldsManager.INSTANCE.loadAsync("island_template_1", true);

            Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () ->
                    JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:space:" +
                            CoreSkyblock.SERVER_NAME + ":" + (getIslandsLoaded() + getTheoreticalMaxPlayersOnline() +
                            getActualPlayersOnline())), 0, 20);
            Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, () -> serversData.put(CoreSkyblock.SERVER_NAME,
                    getIslandsLoaded() + getTheoreticalMaxPlayersOnline() + getActualPlayersOnline()), 0, 20);

            CoreSkyblock.INSTANCE.getServer().getPluginManager().registerEvents(new IslandsProtectionListener(), plugin);

            serversData.put(CoreSkyblock.SERVER_NAME, getIslandsLoaded() + getTheoreticalMaxPlayersOnline() + getActualPlayersOnline());
        }
    }

    public Island getIslandByLoc(World world) {
        if (world == null) return null;
        String worldName = world.getName();
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (!island.isLoaded()) return null;
            if (getIslandWorldName(island.getIslandUUID()).equalsIgnoreCase(worldName)) {
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
        for (Map.Entry<String, Integer> entry : serversData.entrySet()) {
            int loadFactor = entry.getValue();

            ServerType serverType = ServerType.getByName(entry.getKey());
            if (serverType == null) continue;
            if (serverType != ServerType.GAME) continue;

            if (loadFactor < minLoadFactor) {
                minLoadFactor = loadFactor;
                serverToLoad = entry.getKey();
            }
        }
        return serverToLoad;
    }

    /*public boolean isIslandLoadedHere(UUID uuid) {
        for (UUID uuidKey : IslandsDataManager.INSTANCE.getCache().keySet()) {
            if (uuidKey.equals(uuid)) return true;
        }
        return false;
    }*/

    public void checkForDataIntegrity(@Nullable String islandUUIDString, @Nullable UUID playerUUID, boolean forceLoad) {
        Island islandInLocalCache = getIslandOf(playerUUID);

        if (islandInLocalCache != null && islandInLocalCache.isLoaded()) return;

        CompletableFuture.runAsync(() -> {
            if (playerUUID != null) {
                String islandUUIDOfPlayerString = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:members:" + playerUUID);

                try {
                    UUID islandUUID = UUID.fromString(islandUUIDOfPlayerString);
                    loadFromRedis(islandUUID.toString());
                    Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> checkIfIslandIsLoaded(islandUUID), 2);
                    return;
                } catch (Exception ignored) {
                }
            } else {
                try {
                    assert islandUUIDString != null;
                    UUID islandUUID = UUID.fromString(islandUUIDString);
                    loadFromRedis(islandUUID.toString());
                    Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> checkIfIslandIsLoaded(islandUUID), 2);
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
                UUID finalIslandUUID = islandUUID;
                Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> checkIfIslandIsLoaded(finalIslandUUID), 2);
                JedisManager.INSTANCE.sendToRedis("coreskyblock:island:" + island.getIslandUUID(),
                        CoreSkyblock.INSTANCE.gson.toJson(island.toJson()));
                for (UUID member : island.getMembers().keySet()) {
                    JedisManager.INSTANCE.sendToRedis("coreskyblock:island:members:" + member, island.getIslandUUID().toString());
                }
            }
        });
    }

    public void loadFromRedis(String uuid) {
        String content = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:" + uuid);
        String server = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:" + uuid + ":loaded");
        if (content == null) return;
        if (CoreSkyblock.SERVER_NAME.equalsIgnoreCase(server)) {
            return;
        }
        JsonObject json = new Gson().fromJson(content, JsonObject.class);
        Island island = Island.fromJson(json);
        if (island != null) {
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                IslandsDataManager.INSTANCE.getCache().put(island.getIslandUUID(), island);
                return null;
            });
            if (server != null && !server.equalsIgnoreCase(CoreSkyblock.SERVER_NAME)) {
                island.setReadOnly(true);
                island.sendMessageToAllLocals("§cMise à jour des données read only de votre île.");
            }
        }
    }

    public void checkIfIslandIsLoaded(UUID islandUUID) {
        Island island = getIslandByUUID(islandUUID);
        if (island != null && !island.isLoaded()) {
            String server = JedisManager.INSTANCE.getFromRedis("coreskyblock:island:" + islandUUID + ":loaded");
            if (server == null) {

                server = getServerToLoadIsland();
                if (server == null && CoreSkyblock.SERVER_TYPE == ServerType.GAME) {
                    loadIsland(island);
                    return;
                }
                if (server == null) {
                    island.sendMessageToAll("\n§cAucun serveur disponible pour charger votre île.\n");
                    return;
                }
                if (server.equalsIgnoreCase(CoreSkyblock.SERVER_NAME)) {
                    loadIsland(island);
                    return;
                }
                JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:remote_load:" + islandUUID + ":" + server);
            } else {
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    World world = Bukkit.getWorld(getIslandWorldName(islandUUID));
                    if (world != null) {
                        island.setLoaded(true);
                        island.getSpawn().setWorld(world);
                        applyTimeAndWeather(world, island);
                        IslandsSizeManager.INSTANCE.updateWorldBorder(island);
                    }
                    return null;
                });
            }
        }
    }

    public void onDisable() {
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            unload(island, false, false);
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
            CompletableFuture.runAsync(() -> JedisManager.INSTANCE.sendToRedis("coreskyblock:island:bypass:" + uuid,
                    CoreSkyblock.SERVER_NAME));
        } else {
            isBypass.remove(uuid);
            CompletableFuture.runAsync(() -> JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:bypass:" + uuid));
        }
    }

    public boolean isSpying(UUID uuid) {
        return isSpying.contains(uuid);
    }

    public void setSpying(UUID uuid, boolean spy) {
        if (spy) {
            isSpying.add(uuid);
            CompletableFuture.runAsync(() -> JedisManager.INSTANCE.sendToRedis("coreskyblock:island:spy:" + uuid,
                    CoreSkyblock.SERVER_NAME));
        } else {
            isSpying.remove(uuid);
            CompletableFuture.runAsync(() -> JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:spy:" + uuid));
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
                w.setAutoSave(false);

                island.getSpawn().setWorld(w);
                w.setSpawnLocation(island.getSpawn());
                applyTimeAndWeather(w, island);
                IslandsSizeManager.INSTANCE.updateWorldBorder(island);
                CompletableFuture.runAsync(() -> {
                    setIslandLoadedAt(islandUUID);
                    island.update(false);
                    JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:remote_create_response:" +
                            CoreSkyblock.SERVER_NAME + ":" + owner);
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
                            task.cancel();
                            ownerPlayer.sendMessage(Component.text("§b[CoreSkyblock] §aTéléportation sur votre île..."));
                            ownerPlayer.teleportAsync(w.getSpawnLocation()).thenRun(() ->
                                    ownerPlayer.sendMessage(Component.text("§b[CoreSkyblock] §aVous avez été téléporté sur votre île.")));
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
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                island.setLoaded(true);
                Location spawn = island.getSpawn();
                World w = Bukkit.getWorld(getIslandWorldName(island.getIslandUUID()));
                if (w != null) {
                    w.setAutoSave(false);
                }
                if (spawn != null) {
                    spawn.setWorld(w);
                }
                applyTimeAndWeather(w, island);
                IslandsSizeManager.INSTANCE.updateWorldBorder(island);

                island.sendMessageToAll("§aVotre île a été chargée.");
                return null;
            });
            JedisManager.INSTANCE.sendToRedis("coreskyblock:island:" + island.getIslandUUID() + ":loaded", CoreSkyblock.SERVER_NAME);
        }).exceptionally(throwable -> {
            island.sendMessageToAll("§cUne erreur est survenue lors du chargement de votre île.");
            return null;
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
        if (serverWhereIslandIsLoaded != null && !serverWhereIslandIsLoaded.equalsIgnoreCase(CoreSkyblock.SERVER_NAME)) {
            JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:teleport:" + p.getUniqueId() + ":"
                    + island.getIslandUUID() + ":" + serverWhereIslandIsLoaded);

            CoreSkyblock.INSTANCE.sendToServer(p, serverWhereIslandIsLoaded);
        } else {
            p.sendMessage(Component.text("§cNous traitons votre requête, veuillez patienter un cours instant..."));
            checkIfIslandIsLoaded(island.getIslandUUID());
            Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> teleportToIsland(island, p), 50);
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
                    unload(island, true, true), 5);
        }
    }

    public void askOthersServersForMembersOnline(Island island) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (island.getMembers().containsKey(p.getUniqueId())) {
                return;
            }
        }

        awaitingResponseFromServerTime.put(island.getIslandUUID(), System.currentTimeMillis());

        CompletableFuture.runAsync(() -> {

            StringBuilder message = new StringBuilder("island:check_unload:" + island.getIslandUUID());
            for (UUID member : island.getMembers().keySet()) {
                message.append(":").append(member);
            }

            JedisManager.INSTANCE.publishToRedis("coreskyblock", message.toString());

            Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, (task) -> {
                if (awaitingResponseFromServerTime.containsKey(island.getIslandUUID())) {
                    long time = awaitingResponseFromServerTime.get(island.getIslandUUID());
                    if (System.currentTimeMillis() - time > 1_000 * 60 * 5) {
                        Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                            awaitingResponseFromServerTime.remove(island.getIslandUUID());
                            unload(island, true, false);
                            task.cancel();
                            return null;
                        });
                    }
                } else task.cancel();
            }, 0, 10);
        });
    }

    public void unload(Island island, boolean async, boolean delete) {
        if (island.isLoaded()) {
            WorldsManager.INSTANCE.unload(getIslandWorldName(island.getIslandUUID()), true);
            island.setLoaded(false);
            if (async) CompletableFuture.runAsync(() -> actForUnload(island, delete));
            else actForUnload(island, delete);
        }
    }

    private void actForUnload(Island island, boolean delete) {
        if (delete) {
            IslandsDataManager.INSTANCE.deleteIsland(island.getIslandUUID());
            JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:delete:" + island.getIslandUUID());
        }
        JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:" + island.getIslandUUID() + ":loaded");
        for (UUID member : island.getMembers().keySet()) {
            JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:members:" + member);
        }
    }

    public void sendPlayerIslandReadOnly(Player p) {
        p.sendMessage(Component.text("§c§lVeuillez éditer votre île sur le serveur où elle est chargée."));
    }

    public void coopNoResponse(UUID uuid) {
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (island.getCoops().containsKey(uuid)) {
                island.removeCoop(uuid);
                island.sendMessageToAll("§6" + Bukkit.getOfflinePlayer(uuid).getName() + " §7a été retiré des coops car il s'est déconnecté.");
            }
            if (island.getCoops().containsValue(uuid)) {
                ArrayList<UUID> toRemove = new ArrayList<>();
                for (Map.Entry<UUID, UUID> entry : island.getCoops().entrySet()) {
                    if (entry.getValue().equals(uuid)) {
                        toRemove.add(entry.getKey());
                    }
                }
                for (UUID coop : toRemove) {
                    island.removeCoop(coop);
                    island.sendMessageToAll("§6" + Bukkit.getOfflinePlayer(coop).getName() + " §7a été retiré des coops car " +
                            Bukkit.getOfflinePlayer(uuid).getName() + " s'est déconnecté.");
                }
            }
        }
    }
}
