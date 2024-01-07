package fr.farmeurimmo.skylyblock.common;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

public class JedisManager {

    public static JedisManager INSTANCE;
    JedisPool pool;
    private String REDIS_PASSWORD = "1234";
    private String REDIS_HOST = "192.168.1.53";

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
        pool = new JedisPool(REDIS_HOST, 6379);

        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println("Message received. Channel: " + channel + ", Msg: " + message);
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
