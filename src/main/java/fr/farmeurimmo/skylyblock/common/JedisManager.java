package fr.farmeurimmo.skylyblock.common;

import fr.farmeurimmo.skylyblock.purpur.core.SkylyBlock;
import fr.farmeurimmo.skylyblock.purpur.island.IslandsManager;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class JedisManager {

    public static JedisManager INSTANCE;
    private final ServerType serverType;
    JedisPool pool;
    private String REDIS_PASSWORD = "1234";
    private String REDIS_HOST = "192.168.1.53";

    public JedisManager(ServerType serverType) {
        this.serverType = serverType;
        INSTANCE = this;
        String tmpHost = System.getenv("REDIS_HOST");
        String tmpPass = System.getenv("REDIS_PASSWORD");
        if (tmpHost != null && tmpPass != null) {
            REDIS_HOST = tmpHost;
            REDIS_PASSWORD = tmpPass;
            pool = new JedisPool(REDIS_HOST, 6379);
            return;
        }
        pool = new JedisPool(REDIS_HOST, 6379);

        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println("Message received. Channel: " + channel + ", Msg: " + message);
                if (channel.equalsIgnoreCase("skylyblock")) {
                    String[] args = message.split(":");
                    if (serverType != ServerType.SKYBLOCK_ISLAND) return;
                    if (args[0].equalsIgnoreCase("island")) {
                        if (args[1].equalsIgnoreCase("create")) {
                            Bukkit.getScheduler().callSyncMethod(SkylyBlock.INSTANCE, () -> {
                                UUID owner = UUID.fromString(args[2]);
                                IslandsManager.INSTANCE.createIsland(owner);
                                return null;
                            });
                        }
                    }
                }
            }
        };

        new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.auth(REDIS_PASSWORD);
                jedis.subscribe(jedisPubSub, "skylyblock");
            }
        }).start();

        //publishToRedis("skylyblock", "test");
        //publishToRedis("skylyblock", "test2");
    }

    public void onDisable() {
        pool.close();
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
