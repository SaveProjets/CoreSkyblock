package fr.farmeurimmo.skylyblock.common;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    public static DatabaseManager INSTANCE;
    private final String host;
    private final String user;
    private final String password;
    private Connection connection;

    public DatabaseManager(String host, String user, String password) {
        INSTANCE = this;

        this.host = host;
        this.user = user;
        this.password = password;
    }

    public void startConnection() throws Exception {
        try {
            connection = getConnection();
            System.out.println("Successfully connected to the database !");
        } catch (SQLException e) {
            throw new Exception("Unable to connect to the database");
        }
        String tableName = "skyblock_users";
        connection.prepareStatement("CREATE DATABASE IF NOT EXISTS skylyblock").executeUpdate();
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (uuid VARCHAR(36) primary key, " +
                " money DOUBLE, adventureExp DOUBLE, adventureLevel DOUBLE, flyTime INT, hasteLevel INT, speedLevel INT," +
                "jumpLevel INT, hasteActive BOOL, speedActive BOOL, jumpActive BOOL)").executeUpdate();
        connection.prepareStatement("USE " + tableName).executeUpdate();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(host, user, password);
    }

    public CompletableFuture<Void> createUser(SkyblockUser skyblockUser) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT IGNORE INTO users (uuid, money, adventureExp, adventureLevel, flyTime, hasteLevel, " +
                                "speedLevel, jumpLevel, hasteActive, speedActive, jumpActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                statement.setString(1, skyblockUser.getUuid().toString());
                statement.setDouble(2, skyblockUser.getMoney());
                statement.setDouble(3, skyblockUser.getAdventureExp());
                statement.setDouble(4, skyblockUser.getAdventureLevel());
                statement.setInt(5, skyblockUser.getFlyTime());
                statement.setInt(6, skyblockUser.getHasteLevel());
                statement.setInt(7, skyblockUser.getSpeedLevel());
                statement.setInt(8, skyblockUser.getJumpLevel());
                statement.setBoolean(9, skyblockUser.isHasteActive());
                statement.setBoolean(10, skyblockUser.isSpeedActive());
                statement.setBoolean(11, skyblockUser.isJumpActive());

                statement.execute();
                future.complete(null);
            } catch (SQLException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public CompletableFuture<Void> updateUser(SkyblockUser skyblockUser) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE users SET money = ?, adventureExp = ?, adventureLevel = ?, flyTime = ?, hasteLevel = ?, " +
                                "speedLevel = ?, jumpLevel = ?, hasteActive = ?, speedActive = ?, jumpActive = ? WHERE uuid = ?");
                statement.setDouble(1, skyblockUser.getMoney());
                statement.setDouble(2, skyblockUser.getAdventureExp());
                statement.setDouble(3, skyblockUser.getAdventureLevel());
                statement.setInt(4, skyblockUser.getFlyTime());
                statement.setInt(5, skyblockUser.getHasteLevel());
                statement.setInt(6, skyblockUser.getSpeedLevel());
                statement.setInt(7, skyblockUser.getJumpLevel());
                statement.setBoolean(8, skyblockUser.isHasteActive());
                statement.setBoolean(9, skyblockUser.isSpeedActive());
                statement.setBoolean(10, skyblockUser.isJumpActive());
                statement.setString(11, skyblockUser.getUuid().toString());

                statement.execute();
                future.complete(null);
            } catch (SQLException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public CompletableFuture<SkyblockUser> getUser(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
                statement.setString(1, uuid);

                statement.execute();
                ResultSet resultSet = statement.getResultSet();

                if (resultSet.next()) {

                    /*return new SkyblockUser(UUID.fromString(uuid),
                            resultSet.getDouble("money"),
                            resultSet.getDouble("adventureExp"),
                            resultSet.getDouble("adventureLevel"),
                            resultSet.getInt("flyTime"),
                            resultSet.getInt("hasteLevel"),
                            resultSet.getInt("speedLevel"),
                            resultSet.getInt("jumpLevel"),
                            resultSet.getBoolean("hasteActive"),
                            resultSet.getBoolean("speedActive"),
                            resultSet.getBoolean("jumpActive"));*/
                    return null;
                } else {
                    return null;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}
