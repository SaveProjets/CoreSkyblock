package fr.farmeurimmo.coreskyblock.storage.auctions;

import fr.farmeurimmo.coreskyblock.purpur.auctions.AuctionItem;
import fr.farmeurimmo.coreskyblock.storage.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionHouseDataManager {

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS auctions (" +
            "uuid VARCHAR(36) NOT NULL, " +
            "owner VARCHAR(36) NOT NULL, " +
            "owner_name VARCHAR(16) NOT NULL, " +
            "price DOUBLE NOT NULL, " +
            "item TEXT NOT NULL, " +
            "created_at BIGINT NOT NULL, " +
            "PRIMARY KEY (uuid), " +
            "FOREIGN KEY (owner) REFERENCES skyblock_users(uuid) ON DELETE CASCADE" +
            ")";
    public static AuctionHouseDataManager INSTANCE;


    public AuctionHouseDataManager() {
        INSTANCE = this;

        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(CREATE_TABLE)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<AuctionItem> loadAuctions() {
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM auctions")) {
                statement.executeQuery();

                ArrayList<AuctionItem> auctionItems = new ArrayList<>();

                ResultSet resultSet = statement.getResultSet();
                while (resultSet.next()) {
                    auctionItems.add(new AuctionItem(
                            UUID.fromString(resultSet.getString("uuid")),
                            UUID.fromString(resultSet.getString("owner")),
                            resultSet.getString("owner_name"),
                            resultSet.getDouble("price"),
                            AuctionItem.itemFromBase64(resultSet.getString("item")),
                            resultSet.getLong("created_at")
                    ));
                }

                return auctionItems;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertItem(AuctionItem item) {
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO auctions (uuid, owner, " +
                    "owner_name, price, item, created_at) VALUES (?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, item.itemUUID().toString());
                statement.setString(2, item.ownerUUID().toString());
                statement.setString(3, item.ownerName());
                statement.setDouble(4, item.price());
                statement.setObject(5, item.itemToBase64());
                statement.setLong(6, item.createdAt());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public AuctionItem loadItem(UUID uuid) {
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM auctions WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                statement.executeQuery();

                ResultSet resultSet = statement.getResultSet();
                if (resultSet.next()) {
                    return new AuctionItem(
                            UUID.fromString(resultSet.getString("uuid")),
                            UUID.fromString(resultSet.getString("owner")),
                            resultSet.getString("owner_name"),
                            resultSet.getDouble("price"),
                            AuctionItem.itemFromBase64(resultSet.getString("item")),
                            resultSet.getLong("created_at")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteItem(UUID uuid) {
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM auctions WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
