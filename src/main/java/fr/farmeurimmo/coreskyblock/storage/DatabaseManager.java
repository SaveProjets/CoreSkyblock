package fr.farmeurimmo.coreskyblock.storage;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.shop.ShopType;
import fr.farmeurimmo.coreskyblock.purpur.shop.objects.ShopItem;
import fr.farmeurimmo.coreskyblock.purpur.shop.objects.ShopPage;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    public static DatabaseManager INSTANCE;
    private final String host;
    private final String user;
    private final String password;
    private Connection connection;

    public DatabaseManager(String host, String user, String password) throws Exception {
        INSTANCE = this;

        this.host = host;
        this.user = user;
        this.password = password;

        startConnection();
    }

    public void startConnection() throws Exception {
        try {
            connection = getConnection();
            CoreSkyblock.INSTANCE.console.sendMessage("§aSuccessfully connected to the database !");
        } catch (SQLException e) {
            throw new Exception("Unable to connect to the database");
        }
        String tableName = "shops";
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tableName + " (uuid VARCHAR(36) PRIMARY KEY, " +
                "shopType VARCHAR(20),itemName VARCHAR(255), material VARCHAR(30), buyPrice FLOAT,  sellPrice FLOAT)").executeUpdate();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName);
        statement.execute();
        ResultSet resultSet = statement.getResultSet();
        if (!resultSet.next()) {
            CoreSkyblock.INSTANCE.console.sendMessage("Table " + tableName + " is empty, inserting default values...");
            initTableFromConfig().join();
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(host, user, password);
    }

    public CompletableFuture<Void> initTableFromConfig() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                File file = new File(CoreSkyblock.INSTANCE.getDataFolder(), "old-shop.yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                for (String key : config.getKeys(false)) {
                    for (String key2 : config.getConfigurationSection(key).getKeys(false)) {
                        String itemName = config.getString(key + "." + key2 + ".name");
                        Material material = Material.valueOf(config.getString(key + "." + key2 + ".material"));
                        double buyPrice = config.getDouble(key + "." + key2 + ".buy");
                        double sellPrice = config.getDouble(key + "." + key2 + ".sell");
                        String shopType = key.toUpperCase();

                        PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO shops (uuid, shopType, itemName, material, buyPrice, sellPrice) VALUES (?, ?, ?, ?, ?, ?)");
                        statement.setString(1, UUID.randomUUID().toString());
                        statement.setString(2, shopType);
                        statement.setString(3, itemName);
                        statement.setString(4, material.name());
                        statement.setDouble(5, buyPrice);
                        statement.setDouble(6, sellPrice);

                        statement.execute();
                    }
                }

                CoreSkyblock.INSTANCE.console.sendMessage("Successfully inserted " + config.getKeys(false)
                        .size() + " shops in " + (System.currentTimeMillis() - startTime) + "ms");
                future.complete(null);
            } catch (SQLException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public CompletableFuture<ShopPage> getShopPage(ShopType shopType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM shops WHERE shopType = ?");
                statement.setString(1, shopType.name());
                statement.execute();

                ResultSet resultSet = statement.getResultSet();
                ShopPage shopPage = new ShopPage(shopType);
                while (resultSet.next()) {
                    String itemName = resultSet.getString("itemName");
                    Material material = Material.valueOf(resultSet.getString("material"));
                    float buyPrice = resultSet.getFloat("buyPrice");
                    float sellPrice = resultSet.getFloat("sellPrice");

                    shopPage.addItem(new ShopItem(itemName, material, buyPrice, sellPrice));
                }

                return shopPage;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}
