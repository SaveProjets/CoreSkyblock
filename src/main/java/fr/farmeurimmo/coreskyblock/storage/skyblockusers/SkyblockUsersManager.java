package fr.farmeurimmo.coreskyblock.storage.skyblockusers;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.DatabaseManager;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SkyblockUsersManager {

    private static final String CREATE_SKYBLOCK_USERS_TABLE = "CREATE TABLE IF NOT EXISTS skyblock_users " +
            "(uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(255), money DOUBLE, adventure_exp DOUBLE, adventure_level " +
            "DOUBLE, fly_time INT, created_at TIMESTAMP, updated_at TIMESTAMP)";
    public static SkyblockUsersManager INSTANCE;
    private final Map<UUID, SkyblockUser> cache = new HashMap<>();

    public SkyblockUsersManager() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () -> {
            List<UUID> toRemoveModified = new ArrayList<>();
            getCachedUsers().forEach((uuid, user) -> {
                if (user.isModified()) {
                    upsertUser(user);
                    toRemoveModified.add(uuid);
                }
            });
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                toRemoveModified.forEach(uuid -> cache.get(uuid).setModified(false));
                return null;
            });
        }, 0, 20 * 60 * 5);

        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_SKYBLOCK_USERS_TABLE)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //FIXME: redis pub/sub

    public SkyblockUser loadUser(UUID uuid, String name) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "SELECT * FROM skyblock_users WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                SkyblockUser user = new SkyblockUser(uuid, name, resultSet.getDouble("money"),
                        resultSet.getDouble("adventure_exp"), resultSet.getDouble("adventure_level"),
                        resultSet.getInt("fly_time"));
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    cache.put(uuid, user);
                    return null;
                });
                return user;
            } else {
                SkyblockUser user = new SkyblockUser(uuid, name, 0, 0, 0, 0);
                upsertUser(user);
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    cache.put(uuid, user);
                    return null;
                });
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void upsertUser(SkyblockUser user) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT INTO skyblock_users (uuid, name, money, adventure_exp, adventure_level, fly_time, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                        "ON DUPLICATE KEY UPDATE money = VALUES(money), adventure_exp = VALUES(adventure_exp), " +
                        "adventure_level = VALUES(adventure_level), fly_time = VALUES(fly_time), updated_at = CURRENT_TIMESTAMP")) {
            statement.setString(1, user.getUuid().toString());
            statement.setString(2, user.getName());
            statement.setDouble(3, user.getMoney());
            statement.setDouble(4, user.getAdventureExp());
            statement.setDouble(5, user.getAdventureLevel());
            statement.setInt(6, user.getFlyTime());

            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, SkyblockUser> getCachedUsers() {
        return cache;
    }
}
