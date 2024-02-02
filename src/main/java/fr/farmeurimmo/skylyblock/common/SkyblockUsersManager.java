package fr.farmeurimmo.skylyblock.common;

import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkyblockUsersManager {

    public static SkyblockUsersManager INSTANCE;
    private final Map<UUID, SkyblockUser> cache = new HashMap<>();

    public SkyblockUsersManager() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskTimerAsynchronously(SkylyBlock.INSTANCE, () -> getCachedUsers().forEach((uuid, user) -> {
            if (user.isModified()) {
                updateUserSync(user);
                user.setModified(false);
            }
        }), 0, 20 * 60 * 5);
    }

    //FIXME: redis pub/sub

    public CompletableFuture<Void> loadUser(UUID uuid, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (cache.containsKey(uuid)) return null;
                PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                        "SELECT * FROM skyblock_users WHERE uuid = ?");
                statement.setString(1, uuid.toString());
                statement.execute();

                ResultSet resultSet = statement.getResultSet();
                if (resultSet.next()) {
                    return new SkyblockUser(uuid, name, resultSet.getDouble("money"),
                            resultSet.getDouble("adventure_exp"), resultSet.getDouble("adventure_level"),
                            resultSet.getInt("fly_time"));
                } else {
                    SkyblockUser user = new SkyblockUser(uuid, name, 0, 0, 0, 0);
                    createUser(user);
                    return user;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).thenAccept(user -> Bukkit.getScheduler().callSyncMethod(SkylyBlock.INSTANCE, () -> {
            cache.put(uuid, user);
            return null;
        }));
    }

    public void createUser(SkyblockUser user) {
        try {
            PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                    "INSERT INTO skyblock_users (uuid, name, money, adventure_exp, adventure_level, fly_time, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            statement.setString(1, user.getUuid().toString());
            statement.setString(2, user.getName());
            statement.setDouble(3, user.getMoney());
            statement.setDouble(4, user.getAdventureExp());
            statement.setDouble(5, user.getAdventureLevel());
            statement.setInt(6, user.getFlyTime());

            statement.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public void updateUserSync(SkyblockUser user) {
        try {
            PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                    "UPDATE skyblock_users SET money = ?, adventure_exp = ?, adventure_level = ?, fly_time = ?, " +
                            "updated_at = CURRENT_TIMESTAMP WHERE uuid = ?");
            statement.setDouble(1, user.getMoney());
            statement.setDouble(2, user.getAdventureExp());
            statement.setDouble(3, user.getAdventureLevel());
            statement.setInt(4, user.getFlyTime());
            statement.setString(5, user.getUuid().toString());
            statement.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public CompletableFuture<SkyblockUser> updateUserFromDatabase(SkyblockUser user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                        "SELECT * FROM skyblock_users WHERE uuid = ?");
                statement.setString(1, user.getUuid().toString());
                statement.execute();

                ResultSet resultSet = statement.getResultSet();
                if (resultSet.next()) {
                    user.setMoney(resultSet.getDouble("money"));
                    user.setAdventureExp(resultSet.getDouble("adventure_exp"));
                    user.setAdventureLevel(resultSet.getDouble("adventure_level"));
                    user.setFlyTime(resultSet.getInt("fly_time"));
                } else {
                    createUser(user);
                }
                Bukkit.getScheduler().callSyncMethod(SkylyBlock.INSTANCE, () -> {
                    cache.put(user.getUuid(), user);
                    return null;
                });

                return user;
            } catch (Exception ignored) {
            }
            return null;
        });
    }

    public Map<UUID, SkyblockUser> getCachedUsers() {
        return cache;
    }
}
