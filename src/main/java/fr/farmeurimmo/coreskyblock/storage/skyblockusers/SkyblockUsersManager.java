package fr.farmeurimmo.coreskyblock.storage.skyblockusers;

import com.google.gson.JsonObject;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.DatabaseManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SkyblockUsersManager {

    private static final String CREATE_SKYBLOCK_USERS_TABLE = "CREATE TABLE IF NOT EXISTS skyblock_users " +
            "(uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(255), money DOUBLE DEFAULT 0, adventure_exp DOUBLE DEFAULT 0,"
            + " adventure_level DOUBLE DEFAULT 0, fly_time INT DEFAULT 0, current_prestige_level INT DEFAULT 0, " +
            "last_prestige_level_claimed INT DEFAULT 0, current_premium_prestige_level INT DEFAULT 0, " +
            "last_premium_prestige_level_claimed INT DEFAULT 0, own_premium_prestige BOOLEAN DEFAULT FALSE, " +
            "last_special_books TEXT DEFAULT '', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP "
            + "DEFAULT CURRENT_TIMESTAMP)";
    public static SkyblockUsersManager INSTANCE;
    private final Map<UUID, SkyblockUser> cache = new HashMap<>();
    private final LinkedHashMap<String, Double> baltop = new LinkedHashMap<>();

    public SkyblockUsersManager() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () -> {
            LinkedHashMap<String, Double> baltop = getBaltop();

            List<UUID> toRemoveModified = new ArrayList<>();
            getCachedUsers().forEach((uuid, user) -> {
                if (user.isModified()) {
                    upsertUser(user);
                    toRemoveModified.add(uuid);
                }
            });
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                toRemoveModified.forEach(uuid -> cache.get(uuid).setModified(false));
                this.baltop.clear();
                this.baltop.putAll(baltop);
                return null;
            });
        }, 0, 20 * 60 * 3 - 20);

        CompletableFuture.runAsync(() -> {
            LinkedHashMap<String, Double> baltop = getBaltop();
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                this.baltop.putAll(baltop);
                return null;
            });
        });

        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_SKYBLOCK_USERS_TABLE)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onDisable() {
        cache.values().forEach(user -> {
            if (user.isModified()) {
                upsertUser(user);
            }
        });
    }

    public SkyblockUser loadUser(UUID uuid, String name) {
        String redisData = JedisManager.INSTANCE.getFromRedis("coreskyblock:user:" + uuid);
        if (redisData != null) {
            JsonObject jsonObject = CoreSkyblock.INSTANCE.gson.fromJson(redisData, JsonObject.class);
            SkyblockUser user = SkyblockUser.fromJson(jsonObject);
            if (user != null) {
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    cache.put(uuid, user);
                    return null;
                });
                return user;
            }
        }

        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM skyblock_users WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                SkyblockUser user = new SkyblockUser(uuid, name, resultSet.getDouble("money"),
                        resultSet.getDouble("adventure_exp"), resultSet.getDouble("adventure_level"),
                        resultSet.getInt("fly_time"), resultSet.getInt("current_prestige_level"),
                        resultSet.getInt("last_prestige_level_claimed"), resultSet.getInt("current_premium_prestige_level"),
                        resultSet.getInt("last_premium_prestige_level_claimed"), resultSet.getBoolean("own_premium_prestige"),
                        resultSet.getString("last_special_books"));
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    cache.put(uuid, user);
                    return null;
                });
                return user;
            } else {
                SkyblockUser user = new SkyblockUser(uuid, name);
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    cache.put(uuid, user);
                    return null;
                });
                upsertUser(user);
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void upsertUser(SkyblockUser user) {
        JedisManager.INSTANCE.sendToRedis("coreskyblock:user:" + user.getUuid(),
                CoreSkyblock.INSTANCE.gson.toJson(user.toJson()));

        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO skyblock_users (uuid, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = ?, money = ?, " +
                             "adventure_exp = ?, adventure_level = ?, fly_time = ?, current_prestige_level = ?, " +
                             "last_prestige_level_claimed = ?, current_premium_prestige_level = ?, " +
                             "last_premium_prestige_level_claimed = ?, own_premium_prestige = ?, last_special_books = ?," +
                             "updated_at = CURRENT_TIMESTAMP")) {
            statement.setString(1, user.getUuid().toString());
            statement.setString(2, user.getName());

            statement.setString(3, user.getName());
            statement.setDouble(4, user.getMoney());
            statement.setDouble(5, user.getAdventureExp());
            statement.setDouble(6, user.getAdventureLevel());
            statement.setInt(7, user.getFlyTime());
            statement.setInt(8, user.getCurrentPrestigeLevel());
            statement.setInt(9, user.getLastPrestigeLevelClaimed());
            statement.setInt(10, user.getCurrentPremiumPrestigeLevel());
            statement.setInt(11, user.getLastPremiumPrestigeLevelClaimed());
            statement.setBoolean(12, user.ownPremiumPrestige());
            statement.setString(13, user.getLastSpecialBooks());

            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, SkyblockUser> getCachedUsers() {
        return cache;
    }

    private LinkedHashMap<String, Double> getBaltop() {
        LinkedHashMap<String, Double> baltop = new LinkedHashMap<>();
        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT name, money FROM skyblock_users WHERE money > 0 ORDER BY money DESC LIMIT 100");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                baltop.put(resultSet.getString("name"), resultSet.getDouble("money"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baltop;
    }

    public final LinkedHashMap<String, Double> getMoneyTop() {
        return baltop;
    }

    public void unloadUser(SkyblockUser user) {
        Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
            cache.remove(user.getUuid());
            return null;
        });
    }
}
