package fr.farmeurimmo.mineblock.common;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisManager {

    public static JedisManager INSTANCE;
    JedisPool pool;
    private String REDIS_PASSWORD = "tK3u6BEuiGeABAU00wVUidguBjtyzk4ffj9E3Qmb";
    private String REDIS_HOST = "tools-databases-redis-minecraft-1";

    public JedisManager() {
        INSTANCE = this;
        String tmpHost = System.getenv("REDIS_HOST");
        String tmpPass = System.getenv("REDIS_PASSWORD");
        if (tmpHost != null && tmpPass != null) {
            REDIS_HOST = tmpHost;
            REDIS_PASSWORD = tmpPass;
            pool = new JedisPool(REDIS_HOST, 6379);
            return;
        }
        /*pool = new JedisPool(REDIS_HOST, 6379);

        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println("Message received. Channel: " + channel + ", Msg: " + message);
                if (channel.equalsIgnoreCase("MineBlock")) {
                    String[] args = message.split(":");
                    if (args[0].equalsIgnoreCase("island")) {
                        if (args[1].equalsIgnoreCase("create")) {
                            Bukkit.getScheduler().callSyncMethod(MineBlock.INSTANCE, () -> {
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
                jedis.subscribe(jedisPubSub, "MineBlock");
            }
        }).start();*/

        //publishToRedis("MineBlock", "test");
        //publishToRedis("MineBlock", "test2");
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
