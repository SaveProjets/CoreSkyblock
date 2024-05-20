package fr.farmeurimmo.coreskyblock.storage.agriculture;

import fr.farmeurimmo.coreskyblock.storage.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AgricultureCycleDataManager {

    public static final String CREATE_AGRICULTURE_CYCLES_TABLE = "CREATE TABLE IF NOT EXISTS agriculture_cycles " +
            "(id INT PRIMARY KEY, start_time LONG, end_time LONG, created_at TIMESTAMP, updated_at TIMESTAMP)";
    public static AgricultureCycleDataManager INSTANCE;
    // table avec pour nom "agriculture_cycles" et les colonnes suivantes :
    // - id INT PRIMARY KEY → numéro saison
    // - start_time LONG → temps de début de la saison à l'instant T (en millisecondes)
    // - end_time LONG → temps de fin de la saison à l'instant T (en millisecondes)
    // - created_at TIMESTAMP → date de création de la saison
    // - updated_at TIMESTAMP → date de mise à jour de la saison

    public AgricultureCycleDataManager() {
        INSTANCE = this;

        try {
            createTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_AGRICULTURE_CYCLES_TABLE)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeUpdate(Object... parameters) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement("INSERT INTO agriculture_cycles " +
                "(id, start_time, end_time, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW()) " +
                "ON DUPLICATE KEY UPDATE start_time = ?, end_time = ?, updated_at = NOW()")) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSeason(AgricultureCycleSeason season) {
        executeUpdate(season.id(), season.startTime(), season.endTime(), season.startTime(), season.endTime());
    }

    public AgricultureCycleSeason getCurrentSeason() {
        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM agriculture_cycles ORDER BY id DESC LIMIT 1")) {
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new AgricultureCycleSeason(resultSet.getInt("id"), resultSet.getLong("start_time"), resultSet.getLong("end_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
