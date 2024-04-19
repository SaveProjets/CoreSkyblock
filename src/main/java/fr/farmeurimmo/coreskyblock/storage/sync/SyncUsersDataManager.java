package fr.farmeurimmo.coreskyblock.storage.sync;

import fr.farmeurimmo.coreskyblock.storage.DatabaseManager;
import fr.farmeurimmo.coreskyblock.utils.InventorySyncUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SyncUsersDataManager {

    private static final String CREATE_INVENTORIES_TABLE = "CREATE TABLE IF NOT EXISTS skyblock_users_inventories " +
            "(uuid VARCHAR(36) PRIMARY KEY, inventory TEXT, health DOUBLE, food INT, exp FLOAT, level INT, potions TEXT, " +
            "created_at TIMESTAMP, updated_at TIMESTAMP, foreign key (uuid) references skyblock_users(uuid) " +
            "on delete cascade)";
    public static SyncUsersDataManager INSTANCE;

    public SyncUsersDataManager() {
        INSTANCE = this;

        try {
            createTable(DatabaseManager.INSTANCE.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(SyncUsersDataManager.CREATE_INVENTORIES_TABLE)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void executeUpdate(Connection connection, Object... parameters) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO skyblock_users_inventories " +
                "(uuid, inventory, health, food, exp, level, potions, created_at, updated_at) VALUES (?, ?, ?, ?, ?, " +
                "?, ?, NOW(), NOW()) ON DUPLICATE KEY UPDATE inventory = ?, health = ?, food = ?, exp = ?, level = ?, " +
                "potions = ?, updated_at = NOW()")) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveInventory(SyncUser syncUser) {
        try {
            String potions = InventorySyncUtils.INSTANCE.potionEffectsToStringJson(syncUser.getPotionEffects());
            executeUpdate(DatabaseManager.INSTANCE.getConnection(),
                    syncUser.getUuid().toString(), syncUser.getInventory(), syncUser.getHealth(), syncUser.getFood(),
                    syncUser.getExp(), syncUser.getLevel(), potions, syncUser.getInventory(), syncUser.getHealth(),
                    syncUser.getFood(), syncUser.getExp(), syncUser.getLevel(), potions);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SyncUser getInventory(UUID uuid) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "SELECT * FROM skyblock_users_inventories WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.execute();

            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                return new SyncUser(uuid, resultSet.getString("inventory"), resultSet.getDouble("health"),
                        resultSet.getInt("food"), resultSet.getFloat("exp"), resultSet.getInt("level"),
                        InventorySyncUtils.INSTANCE.jsonToPotionEffects(resultSet.getString("potions")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
