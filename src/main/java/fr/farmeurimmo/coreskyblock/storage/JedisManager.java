package fr.farmeurimmo.coreskyblock.storage;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class JedisManager {

    public static JedisManager INSTANCE;
    JedisPool pool;
    private String REDIS_PASSWORD = "tK3u6BEuiGeABAU00wVUidguBjtyzk4ffj9E3Qmb";
    private String REDIS_HOST = "tools-databases-redis-minecraft-1";

    public JedisManager() {
        INSTANCE = this;
        String tmpHost = CoreSkyblock.INSTANCE.getConfig().getString("redis.host");
        String tmpPass = CoreSkyblock.INSTANCE.getConfig().getString("redis.password");
        if (tmpHost != null && tmpPass != null) {
            REDIS_HOST = tmpHost;
            REDIS_PASSWORD = tmpPass;
        }
        pool = new JedisPool(REDIS_HOST, 6379);

        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equalsIgnoreCase("coreskyblock")) {
                    String[] args = message.split(":");
                    if (args[0].equalsIgnoreCase("island")) {
                        if (args[1].equalsIgnoreCase("pubsub")) {
                            IslandsManager.INSTANCE.checkForDataIntegrity(args[2], null, true);
                            return;
                        }
                        if (args[1].equalsIgnoreCase("space")) {
                            String serverName = args[2];
                            if (CoreSkyblock.SERVER_NAME.equalsIgnoreCase(serverName)) {
                                return;
                            }
                            try {
                                int load = Integer.parseInt(args[3]);
                                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                                    IslandsManager.INSTANCE.serversData.put(serverName, load);
                                    return null;
                                });
                            } catch (Exception ignored) {
                            }
                            return;
                        }
                        if (args[1].equalsIgnoreCase("remote_create")) {
                            String serverName = args[2];
                            if (!CoreSkyblock.SERVER_NAME.equalsIgnoreCase(serverName)) {
                                return;
                            }
                            try {
                                UUID playerUUID = UUID.fromString(args[3]);
                                UUID islandUUID = UUID.fromString(args[4]);
                                IslandsManager.INSTANCE.create(playerUUID, islandUUID, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                                publishToRedis("coreskyblock", "island:remote_create_response:" + serverName
                                        + ":" + args[3] + ":error");
                            }
                            return;
                        }
                        if (args[1].equalsIgnoreCase("remote_create_response")) {
                            String serverName = args[2];
                            if (CoreSkyblock.SERVER_NAME.equalsIgnoreCase(serverName)) {
                                return;
                            }
                            try {
                                UUID playerUUID = UUID.fromString(args[3]);
                                Player p = CoreSkyblock.INSTANCE.getServer().getPlayer(playerUUID);
                                if (p == null) return;
                                if (message.contains("error")) {
                                    p.sendMessage(Component.text("§cUne erreur est survenue lors de la création de votre île."));
                                    return;
                                }
                                p.sendMessage("§aVotre île a bien été créée ! Téléportation en cours...");

                                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                                out.writeUTF("Connect");
                                out.writeUTF(serverName);
                                p.sendPluginMessage(CoreSkyblock.INSTANCE, "BungeeCord", out.toByteArray());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        if (args[1].equalsIgnoreCase("check_unload")) {
                            try {
                                UUID islandUUID = UUID.fromString(args[2]);

                                //the rest of args are the players to check
                                for (int i = 3; i < args.length; i++) {
                                    UUID playerUUID = UUID.fromString(args[i]);
                                    Player p = CoreSkyblock.INSTANCE.getServer().getPlayer(playerUUID);
                                    if (p != null) {
                                        JedisManager.INSTANCE.publishToRedis("coreskyblock",
                                                "island:check_unload_response:" + islandUUID + ":no");
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        if (args[1].equalsIgnoreCase("check_unload_response")) {
                            try {
                                UUID islandUUID = UUID.fromString(args[2]);
                                if (args[3].equalsIgnoreCase("no")) {
                                    IslandsManager.INSTANCE.awaitingResponseFromServerTime.remove(islandUUID);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        if (args[1].equalsIgnoreCase("remote_load")) {
                            String serverName = args[3];
                            if (!CoreSkyblock.SERVER_NAME.equalsIgnoreCase(serverName)) {
                                return;
                            }
                            try {
                                UUID islandUUID = UUID.fromString(args[2]);
                                Island island = IslandsDataManager.INSTANCE.getIsland(islandUUID);
                                if (island == null) {
                                    publishToRedis("coreskyblock", "island:remote_load_response:" + serverName
                                            + ":" + args[3] + ":error");
                                    return;
                                }
                                IslandsManager.INSTANCE.loadIsland(island);
                            } catch (Exception e) {
                                e.printStackTrace();
                                publishToRedis("coreskyblock", "island:remote_load_response:" + serverName
                                        + ":" + args[3] + ":error");
                            }
                            return;
                        }
                        if (args[1].equalsIgnoreCase("remote_load_response")) {
                            String serverName = args[2];
                            if (CoreSkyblock.SERVER_NAME.equalsIgnoreCase(serverName)) {
                                return;
                            }
                            try {
                                UUID islandUUID = UUID.fromString(args[3]);
                                if (message.contains("error")) {
                                    IslandsManager.INSTANCE.awaitingResponseFromServerTime.remove(islandUUID);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        if (args[1].equalsIgnoreCase("teleport")) {
                            try {
                                UUID playerUUID = UUID.fromString(args[2]);
                                UUID islandUUID = UUID.fromString(args[3]);
                                String serverName = args[4];
                                if (!CoreSkyblock.SERVER_NAME.equalsIgnoreCase(serverName)) {
                                    return;
                                }
                                AtomicInteger tries = new AtomicInteger(0);
                                Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
                                    if (tries.get() >= 40) {
                                        task.cancel();
                                        return;
                                    }
                                    Player p = CoreSkyblock.INSTANCE.getServer().getPlayer(playerUUID);
                                    if (p == null) {
                                        tries.getAndIncrement();
                                        return;
                                    }
                                    Island island = IslandsDataManager.INSTANCE.getCache().get(islandUUID);
                                    if (island == null) {
                                        tries.getAndIncrement();
                                        return;
                                    }
                                    IslandsManager.INSTANCE.teleportToIsland(island, p);
                                    task.cancel();
                                }, 0, 5);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                }
            }
        };

        new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.auth(REDIS_PASSWORD);
                jedis.subscribe(jedisPubSub, "coreskyblock");
            }
        }).start();
    }

    public void onDisable() {
        try {
            pool.close();
        } catch (Exception ignored) {
        }
    }

    public void sendToRedis(String arg0, String data) {
        try (Jedis jedis = pool.getResource()) {
            jedis.auth(REDIS_PASSWORD);
            jedis.set(arg0, data);
        }
    }

    public String getFromRedis(String arg0) {
        try (Jedis jedis = pool.getResource()) {
            jedis.auth(REDIS_PASSWORD);
            return jedis.get(arg0);
        }
    }

    public void removeFromRedis(String arg0) {
        try (Jedis jedis = pool.getResource()) {
            jedis.auth(REDIS_PASSWORD);
            if (jedis.get(arg0) != null) {
                jedis.del(arg0);
            }
        }
    }

    public void publishToRedis(String arg0, String data) {
        try (Jedis jedis = pool.getResource()) {
            jedis.auth(REDIS_PASSWORD);
            jedis.publish(arg0, data);
        }
    }
}
