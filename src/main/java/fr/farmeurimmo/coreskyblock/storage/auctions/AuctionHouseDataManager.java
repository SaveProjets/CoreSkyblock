package fr.farmeurimmo.coreskyblock.storage.auctions;

import fr.farmeurimmo.coreskyblock.storage.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class AuctionHouseDataManager {

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS auctions (" +
            "uuid VARCHAR(36) NOT NULL, " +
            "owner VARCHAR(36) NOT NULL, " +
            "owner_name VARCHAR(16) NOT NULL, " +
            "price DOUBLE NOT NULL, " +
            "item JSON NOT NULL, " +
            "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
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

        CompletableFuture.runAsync(this::loadAuctions);
    }

    public void loadAuctions() {
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM auctions")) {
                statement.executeQuery();

                // TODO: Load auctions
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
