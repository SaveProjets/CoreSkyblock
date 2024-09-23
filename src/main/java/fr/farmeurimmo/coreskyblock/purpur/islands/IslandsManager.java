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
import fr.farmeurimmo.coreskyblock.purpur.islands.listeners.IslandsProtectionItemsAdderListener;
import fr.farmeurimmo.coreskyblock.purpur.islands.listeners.IslandsProtectionListener;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.*;
import fr.farmeurimmo.coreskyblock.purpur.worlds.WorldsManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandRanksManager;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class IslandsManager {

    public static final long UNLOAD_TIME = 1_000 * 30;
    public static final long TICK_SAVE = 20 * 60 * 2;
    public static IslandsManager INSTANCE;
    public final Map<String, Integer> serversData = new HashMap<>();
    public final Map<UUID, String> awaitingResponseFromServer = new HashMap<>();
    public final Map<UUID, Long> awaitingResponseFromServerTime = new HashMap<>(); //for unload
    public final Map<UUID, UUID> teleportToIsland = new HashMap<>();
    public final Map<UUID, ArrayList<Long>> teleportTry = new HashMap<>();
    public final Map<UUID, ArrayList<UUID>> wantToTeleport = new HashMap<>();
    private final JavaPlugin plugin;
    private final ArrayList<UUID> isBypass = new ArrayList<>();
    private final ArrayList<UUID> isSpying = new ArrayList<>();
    private final ArrayList<UUID> deleteConfirmation = new ArrayList<>();
    private final Map<UUID, Long> connecting = new HashMap<>();

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
        new IslandsBlocksLimiterManager();
        new IslandsEffectsManager();

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
                    if (island.isLoaded()) {
                        askOthersServersForMembersOnline(island);
                    }
                }
            }, 0, 20 * 30);

            Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, () -> {
                for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
                    if (island.isLoaded()) {
                        World world = getIslandWorld(island.getIslandUUID());
                        if (world != null) {
                            WorldsManager.INSTANCE.saveWorldAsync(world);
                        }
                    }
                }
            }, 0, TICK_SAVE);

            Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, () -> {
                for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
                    if (island.isLoaded()) {
                        IslandsEffectsManager.INSTANCE.setEffects(island);
                    }
                }
            }, 0, 20 * 18);

            WorldsManager.INSTANCE.loadAsync("island_template_1", true);

            CoreSkyblock.INSTANCE.getServer().getPluginManager().registerEvents(new IslandsProtectionListener(), plugin);
            CoreSkyblock.INSTANCE.getServer().getPluginManager().registerEvents(new IslandsProtectionItemsAdderListener(), plugin);

            serversData.put(CoreSkyblock.SERVER_NAME, getIslandsLoaded() + getTheoreticalMaxPlayersOnline() + getActualPlayersOnline());

            Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, () -> serversData.put(CoreSkyblock.SERVER_NAME,
                    getIslandsLoaded() + getTheoreticalMaxPlayersOnline() + getActualPlayersOnline()), 0, 20);

            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () ->
                        JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:space:" +
                                CoreSkyblock.SERVER_NAME + ":" + (getIslandsLoaded() + getTheoreticalMaxPlayersOnline() +
                                getActualPlayersOnline())), 0, 20);
                return null;
            });
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
                    Bukkit.getScheduler().runTaskLaterAsynchronously(CoreSkyblock.INSTANCE, () -> checkIfIslandIsLoaded(islandUUID), 20);
                    return;
                } catch (Exception ignored) {
                }
            } else {
                try {
                    assert islandUUIDString != null;
                    UUID islandUUID = UUID.fromString(islandUUIDString);
                    loadFromRedis(islandUUID.toString());
                    Bukkit.getScheduler().runTaskLaterAsynchronously(CoreSkyblock.INSTANCE, () -> checkIfIslandIsLoaded(islandUUID), 20);
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
                Bukkit.getScheduler().runTaskLaterAsynchronously(CoreSkyblock.INSTANCE, () -> checkIfIslandIsLoaded(finalIslandUUID), 20);
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
            } else if (server.equalsIgnoreCase(CoreSkyblock.SERVER_NAME)) {
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    World world = Bukkit.getWorld(getIslandWorldName(islandUUID));
                    if (world != null) {
                        island.setLoaded(true);
                        island.setReadOnly(true);
                        island.getSpawn().setWorld(world);
                        applyTimeAndWeather(world, island);
                        IslandsSizeManager.INSTANCE.updateWorldBorder(island);
                    }
                    return null;
                });
            } else {
                island.setReadOnly(true);
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

    public void createIsland(UUID owner, String ownerName) {
        UUID islandId = UUID.randomUUID();

        String serverToLoad = getServerToLoadIsland();

        Player ownerPlayer = plugin.getServer().getPlayer(owner);
        if (serverToLoad != null) {
            if (serverToLoad.equalsIgnoreCase(CoreSkyblock.SERVER_NAME)) {
                create(owner, ownerName, islandId, true);
                return;
            }

            if (ownerPlayer != null) {
                ownerPlayer.sendMessage(Component.text("§6Création de votre île en cours..."));
            }

            JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:remote_create:" + serverToLoad + ":"
                    + owner + ":" + ownerName + ":" + islandId);
            awaitingResponseFromServer.put(islandId, serverToLoad);
        } else {
            if (ownerPlayer != null) {
                ownerPlayer.sendMessage(Component.text("§cAucun serveur disponible pour créer votre île. Veuillez réessayer dans quelques instants."));
            }

            return;
        }

        Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> {
            if (awaitingResponseFromServer.containsKey(islandId)) {
                awaitingResponseFromServer.remove(islandId);
                if (ownerPlayer != null) {
                    ownerPlayer.sendMessage(Component.text("§cUne erreur est survenue lors de la création de votre île. Veuillez réessayer dans quelques instants."));
                }
            }
        }, 20 * 10);
    }

    public void setIslandLoadedAt(UUID uuid) {
        JedisManager.INSTANCE.sendToRedis("coreskyblock:island:" + uuid + ":loaded", CoreSkyblock.SERVER_NAME);

        JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:loaded:" + uuid + ":" + CoreSkyblock.SERVER_NAME);
    }

    public void create(UUID owner, String ownerName, UUID islandUUID, boolean sameServer) {
        String worldName = getIslandWorldName(islandUUID);

        Island island = new Island(islandUUID, new Location(Bukkit.getWorld(worldName), 0.5, 80.1, 0.5,
                40, 0), owner, ownerName);
        CompletableFuture.supplyAsync(() -> IslandsDataManager.INSTANCE.saveIsland(island)).thenAccept(result -> {
            if (!result) {
                JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:remote_create_response:" +
                        CoreSkyblock.SERVER_NAME + ":" + owner + ":error");
                return;
            }
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                SlimeWorld slimeWorld = WorldsManager.INSTANCE.cloneAndLoad(worldName, "island_template_1", false);
                if (slimeWorld == null) {
                    JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:remote_create_response:" +
                            CoreSkyblock.SERVER_NAME + ":" + owner + ":error");
                    IslandsDataManager.INSTANCE.deleteIsland(islandUUID);
                    return null;
                }
                island.setLoaded(true);
                island.setReadOnly(false);
                IslandsDataManager.INSTANCE.getCache().put(islandUUID, island);
                World w = Bukkit.getWorld(worldName);
                if (w == null) return null;
                w.setAutoSave(false);

                island.getSpawn().setWorld(w);
                w.setSpawnLocation(island.getSpawn());
                applyTimeAndWeather(w, island);
                IslandsSizeManager.INSTANCE.updateWorldBorder(island);

                IslandsBlocksLimiterManager.INSTANCE.initializeIsland(island);

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
                    ownerPlayer.sendMessage(Component.text("§aVotre île a été créée avec succès !"));
                } else {
                    AtomicInteger iterations = new AtomicInteger(0);
                    Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, (task -> {
                        Player ownerPlayer = plugin.getServer().getPlayer(owner);
                        if (ownerPlayer != null) {
                            task.cancel();
                            ownerPlayer.teleportAsync(w.getSpawnLocation()).thenRun(() ->
                                    ownerPlayer.sendMessage(Component.text("§aVous avez été téléporté sur votre île.")));
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
            setIslandLoadedAt(island.getIslandUUID());

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
                island.setReadOnly(false);

                IslandsBlocksLimiterManager.INSTANCE.initializeIsland(island);
                return null;
            });
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
            if (connecting.containsKey(p.getUniqueId())) {
                long time = connecting.get(p.getUniqueId());
                if (System.currentTimeMillis() - time > 10 * 1_000) {
                    connecting.remove(p.getUniqueId());
                } else {
                    p.sendMessage(Component.text("§cVotre téléportation est en cours, veuillez patienter..."));
                    return;
                }
            }
            connecting.put(p.getUniqueId(), System.currentTimeMillis());
            Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> connecting.remove(p.getUniqueId()), 9 * 10L);

            JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:teleport:" + p.getUniqueId() + ":"
                    + island.getIslandUUID() + ":" + serverWhereIslandIsLoaded);

            CoreSkyblock.INSTANCE.sendToServer(p, serverWhereIslandIsLoaded);
        } else {
            p.sendMessage(Component.text("§cNous traitons votre requête, veuillez patienter un instant..."));

            wantToTeleport.putIfAbsent(island.getIslandUUID(), new ArrayList<>());
            if (!wantToTeleport.get(island.getIslandUUID()).contains(p.getUniqueId())) {
                wantToTeleport.get(island.getIslandUUID()).add(p.getUniqueId());
            }

            if (teleportTry.containsKey(p.getUniqueId())) {
                ArrayList<Long> tries = teleportTry.get(p.getUniqueId());
                tries.add(System.currentTimeMillis());
                if (tries.size() > 3) {
                    teleportTry.remove(p.getUniqueId());
                    p.sendMessage(Component.text("§cUne erreur est survenue lors de la téléportation. Veuillez réessayer dans quelques instants."));
                    return;
                }
            } else {
                ArrayList<Long> tries = new ArrayList<>();
                tries.add(System.currentTimeMillis());
                teleportTry.put(p.getUniqueId(), tries);
            }

            checkIfIslandIsLoaded(island.getIslandUUID());
            Bukkit.getScheduler().runTaskLaterAsynchronously(CoreSkyblock.INSTANCE, () -> teleportToIsland(island, p), 20 * 10L);
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
            unload(island, true, true);
        }
    }

    public void askOthersServersForMembersOnline(Island island) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (island.getMembers().containsKey(p.getUniqueId())) {
                return;
            }
        }

        if (!IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID()).getPlayers().isEmpty()) {
            awaitingResponseFromServerTime.remove(island.getIslandUUID());
            return;
        }

        if (CoreSkyblock.INSTANCE.isOneOfThemOnline(new ArrayList<>(island.getMembers().keySet()))) {
            awaitingResponseFromServerTime.remove(island.getIslandUUID());
            return;
        }

        if (awaitingResponseFromServerTime.containsKey(island.getIslandUUID())) return;

        awaitingResponseFromServerTime.put(island.getIslandUUID(), System.currentTimeMillis());

        Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, (task) -> {

            if (CoreSkyblock.INSTANCE.isOneOfThemOnline(new ArrayList<>(island.getMembers().keySet()))) {
                awaitingResponseFromServerTime.remove(island.getIslandUUID());
                task.cancel();
                return;
            }

            if (!IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID()).getPlayers().isEmpty()) {
                awaitingResponseFromServerTime.remove(island.getIslandUUID());
                task.cancel();
                return;
            }

            if (awaitingResponseFromServerTime.containsKey(island.getIslandUUID())) {
                long time = awaitingResponseFromServerTime.get(island.getIslandUUID());
                if (System.currentTimeMillis() - time > UNLOAD_TIME) {
                    awaitingResponseFromServerTime.remove(island.getIslandUUID());
                    unload(island, true, false);
                    task.cancel();
                }
            } else task.cancel();
        }, 0, 20 * 10);
    }

    public void unload(Island island, boolean async, boolean delete) {
        if (island.isLoaded()) {
            if (async) CompletableFuture.runAsync(() -> actForUnload(island, delete));
            else actForUnload(island, delete);

            World w = Bukkit.getWorld(getIslandWorldName(island.getIslandUUID()));
            if (w != null) {
                try {
                    w.getPlayers().forEach(p -> {
                        p.sendMessage(Component.text("§aTéléportation au spawn car votre île a été " + (delete ? "supprimée." : "déchargée.")));
                        String server = CoreSkyblock.INSTANCE.getASpawnServer();
                        if (server == null) {
                            p.sendMessage(Component.text("§cErreur, aucun serveur de spawn disponible !"));
                            p.teleport(CoreSkyblock.SPAWN);
                        } else {
                            if (CoreSkyblock.INSTANCE.isEnabled()) CoreSkyblock.INSTANCE.sendToServer(p, server);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            WorldsManager.INSTANCE.unload(getIslandWorldName(island.getIslandUUID()), true);
            island.setLoaded(false);
            island.sendMessageToAll("§cVotre île a été déchargée.");
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

    public Set<ChunkSnapshot> getSnapshots(Island island) {
        double minX = -IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());
        double minZ = -IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());
        double maxX = IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());
        double maxZ = IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());

        World world = IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID());

        Set<ChunkSnapshot> chunks = new HashSet<>();

        for (int x = (int) minX; x < (maxX + 16); x += 16) {
            for (int z = (int) minZ; z < (maxZ + 16); z += 16) {
                Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                if (!chunk.isLoaded()) {
                    if (chunk.load(false)) {
                        chunks.add(chunk.getChunkSnapshot(true, false, false));
                        chunk.unload();
                    }
                } else {
                    chunks.add(chunk.getChunkSnapshot(true, false, false));
                }
            }
        }

        return chunks;
    }

    public void invitationLogic(Island island, UUID emitterUUID, String emitterName, UUID receiverUUID, String receiverName) {
        IslandRanks rank = island.getMembers().get(emitterUUID);
        if (rank == null) {
            sendToPlayerOrViaRedis(Bukkit.getPlayer(emitterUUID), emitterUUID, "§cVous n'êtes pas membre de l'île.");
            return;
        }

        Player p = Bukkit.getPlayer(emitterUUID);
        if (!island.hasPerms(rank, IslandPerms.INVITES, emitterUUID)) {
            sendToPlayerOrViaRedis(p, emitterUUID, "§cVous n'avez pas la permission d'inviter des joueurs.");
            return;
        }
        if (island.getMembers().containsKey(receiverUUID)) {
            sendToPlayerOrViaRedis(p, emitterUUID, "§cLe joueur est déjà membre de l'île.");
            return;
        }
        if (island.getBannedPlayers().contains(receiverUUID)) {
            sendToPlayerOrViaRedis(p, emitterUUID, "§cLe joueur est banni de l'île.");
            return;
        }
        if (IslandsMaxMembersManager.INSTANCE.isFull(island.getMaxMembers(), island.getMembers().size())) {
            sendToPlayerOrViaRedis(p, emitterUUID, "§cL'île est pleine.");
            return;
        }
        if (island.isInvited(receiverUUID)) {
            sendToPlayerOrViaRedis(p, emitterUUID, "§cLe joueur a déjà été invité.");
            return;
        }

        if (!island.isReadOnly()) island.addInvite(receiverUUID);

        Player target = Bukkit.getPlayer(receiverUUID);
        Component toSend = Component.text("§aVous avez été invité à rejoindre l'île de " +
                        emitterName + ". " + "Elle expire dans 1 minute.\n")
                .append(Component.text("§2[Cliquez sur ce message pour accepter l'invitation.]")
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/is accept " + emitterName))
                        .hoverEvent(Component.text("§aAccepter l'invitation")));
        if (target != null) {
            target.sendMessage(toSend);
        } else {
            CompletableFuture.runAsync(() -> JedisManager.INSTANCE.publishToRedis("coreskyblock",
                    "island:to_player_chat:" + receiverUUID + ":" + CoreSkyblock.SERVER_NAME + ":" +
                            "\n§aVous avez été invité à rejoindre l'île de " + emitterName +
                            ". §cElle expire dans 1 minute.\n§6Faites §e/is accept " + emitterName +
                            "§6 pour accepter l'invitation.\n"));
        }

        sendToPlayerOrViaRedis(p, emitterUUID, "§a" + receiverName + " a été invité à rejoindre l'île.");

        island.sendMessage("§a" + receiverName + " a été invité à rejoindre l'île.", IslandPerms.INVITES);
    }

    public void sendToPlayerOrViaRedis(Player p, UUID uuid, String message) {
        if (p != null) {
            p.sendMessage(Component.text(message));
        } else {
            CompletableFuture.runAsync(() -> JedisManager.INSTANCE.publishToRedis("coreskyblock",
                    "island:to_player_chat:" + uuid + ":" + CoreSkyblock.SERVER_NAME + ":" + message));
        }
    }
}
