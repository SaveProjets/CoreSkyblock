package fr.farmeurimmo.coreskyblock.common.islands;

import fr.farmeurimmo.coreskyblock.common.DatabaseManager;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.utils.LocationTranslator;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class IslandsDataManager {

    private static final String CREATE_ISLANDS_TABLE = "CREATE TABLE IF NOT EXISTS islands (uuid VARCHAR(36) " +
            "PRIMARY KEY, name VARCHAR(255), spawn VARCHAR(255), upgrade_size INT, upgrade_members INT, " +
            "upgrade_generator INT, bank_money DOUBLE, is_public BOOLEAN, exp DOUBLE, level FLOAT, " +
            "created_at TIMESTAMP, updated_at TIMESTAMP)";
    private static final String CREATE_ISLAND_MEMBERS_TABLE = "CREATE TABLE IF NOT EXISTS island_members (island_uuid "
            + "VARCHAR(36), username VARCHAR(36), uuid VARCHAR(36), rank_id INT, created_at TIMESTAMP, " +
            "updated_at TIMESTAMP, PRIMARY KEY(island_uuid, uuid), FOREIGN KEY(island_uuid) " +
            "REFERENCES islands(uuid) ON DELETE CASCADE)";
    private static final String CREATE_ISLAND_RANKS_PERMISSIONS_TABLE = "CREATE TABLE IF NOT EXISTS " +
            "island_ranks_permissions (island_uuid VARCHAR(36), rank_id INT, permission_id INT, created_at " +
            "TIMESTAMP, updated_at TIMESTAMP, PRIMARY KEY(island_uuid, permission_id), FOREIGN KEY(island_uuid) " +
            "REFERENCES islands(uuid) ON DELETE CASCADE)";
    private static final String CREATE_ISLAND_BANNEDS_TABLE = "CREATE TABLE IF NOT EXISTS island_banneds " +
            "(island_uuid VARCHAR(36), uuid VARCHAR(36), created_at TIMESTAMP, updated_at TIMESTAMP, " +
            "PRIMARY KEY(island_uuid, uuid), FOREIGN KEY(island_uuid) REFERENCES islands(uuid) ON DELETE CASCADE)";
    private static final String CREATE_ISLAND_SETTINGS_TABLE = "CREATE TABLE IF NOT EXISTS island_settings " +
            "(island_uuid VARCHAR(36), setting_id INT, value BOOLEAN, created_at TIMESTAMP, updated_at TIMESTAMP, " +
            "PRIMARY KEY(island_uuid, setting_id), FOREIGN KEY(island_uuid) REFERENCES islands(uuid) ON DELETE CASCADE)";
    public static IslandsDataManager INSTANCE;
    private final Map<UUID, Island> cache = new HashMap<>();

    public IslandsDataManager() {
        INSTANCE = this;

        CompletableFuture.runAsync(this::loadAllIslands);

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () -> {
            for (Island island : cache.values()) {
                if (island.needUpdate()) {
                    island.update(true);
                }
            }
        }, 0, 20 * 60 * 3);

        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(CREATE_ISLANDS_TABLE)) {
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(CREATE_ISLAND_MEMBERS_TABLE)) {
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(CREATE_ISLAND_RANKS_PERMISSIONS_TABLE)) {
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(CREATE_ISLAND_BANNEDS_TABLE)) {
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(CREATE_ISLAND_SETTINGS_TABLE)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Map<UUID, Island> getCache() {
        return cache;
    }

    public void loadAllIslands() {
        List<UUID> islandUUIDs = new ArrayList<>();
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement("SELECT * FROM islands")) {
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                UUID islandUUID = UUID.fromString(result.getString("uuid"));
                islandUUIDs.add(islandUUID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        CompletableFuture<?>[] futures = islandUUIDs.stream()
                .map(uuid -> CompletableFuture.runAsync(() -> loadIsland(uuid)))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures);
    }

    public void loadIsland(UUID uuid) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "SELECT * FROM islands WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.executeQuery();

            ResultSet result = statement.getResultSet();
            if (result.next()) {
                String name = result.getString("name");
                String locationString = result.getString("spawn");
                int upgradeSize = result.getInt("upgrade_size");
                int upgradeMembers = result.getInt("upgrade_members");
                int upgradeGenerator = result.getInt("upgrade_generator");
                double bankMoney = result.getDouble("bank_money");
                boolean isPublic = result.getBoolean("is_public");
                double exp = result.getDouble("exp");
                float level = result.getFloat("level");

                Pair<Map<UUID, IslandRanks>, Map<UUID, String>> members = loadIslandMembers(uuid);

                Map<IslandRanks, ArrayList<IslandPerms>> perms = loadIslandPerms(uuid);


                ArrayList<UUID> bannedPlayers = new ArrayList<>(loadIslandBanned(uuid));

                Location spawn = LocationTranslator.fromString(locationString);

                List<IslandSettings> settings = loadIslandSettings(uuid);

                Island island = new Island(uuid, name, spawn, members.left(), members.right(), perms, upgradeSize, upgradeMembers, upgradeGenerator,
                        bankMoney, bannedPlayers, isPublic, exp, settings, level);

                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    cache.put(uuid, island);
                    return null;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean saveIsland(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT INTO islands (uuid, name, spawn, upgrade_size, upgrade_members, upgrade_generator, " +
                        "bank_money, is_public, exp, level, created_at, updated_at) VALUES (?, " +
                        "?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            statement.setString(1, island.getIslandUUID().toString());
            statement.setString(2, island.getName());
            statement.setString(3, LocationTranslator.fromLocation(island.getSpawn()));
            statement.setInt(4, island.getMaxSize());
            statement.setInt(5, island.getMaxMembers());
            statement.setInt(6, island.getGeneratorLevel());
            statement.setDouble(7, island.getBankMoney());
            statement.setBoolean(8, island.isPublic());
            statement.setDouble(9, island.getExp());
            statement.setFloat(10, island.getLevel());
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (!saveIslandMembers(island.getIslandUUID(), island.getMembers(), island.getMembersNames())) {
            return false;
        }

        if (!saveIslandPerms(island.getIslandUUID(), island.getRanksPermsReduced())) {
            return false;
        }

        if (!saveIslandBanned(island.getIslandUUID(), island.getBannedPlayers())) {
            return false;
        }
        if (!saveIslandSettings(island.getIslandUUID(), island.getSettings())) {
            return false;
        }

        return true;
    }

    public void update(Island island, boolean membersModified, boolean permsModified, boolean bannedModified,
                       boolean settingsModified) {
        try {
            updateIsland(island);
            if (membersModified) {
                updateIslandMembers(island);
            }
            if (permsModified) {
                updateIslandPerms(island);
            }
            if (bannedModified) {
                updateIslandBanned(island);
            }
            if (settingsModified) {
                updateIslandSettings(island);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateIsland(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "UPDATE islands SET name = ?, spawn = ?, upgrade_size = ?, upgrade_members = ?, upgrade_generator = ?, " +
                        "bank_money = ?, is_public = ?, exp = ?, level = ?, updated_at = CURRENT_TIMESTAMP WHERE uuid = ?")) {
            statement.setString(1, island.getName());
            statement.setString(2, LocationTranslator.fromLocation(island.getSpawn()));
            statement.setInt(3, island.getMaxSize());
            statement.setInt(4, island.getMaxMembers());
            statement.setInt(5, island.getGeneratorLevel());
            statement.setDouble(6, island.getBankMoney());
            statement.setBoolean(7, island.isPublic());
            statement.setDouble(8, island.getExp());
            statement.setFloat(9, island.getLevel());
            statement.setString(10, island.getIslandUUID().toString());
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateIslandMembers(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT IGNORE INTO island_members (island_uuid, uuid, username, rank_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            for (Map.Entry<UUID, IslandRanks> entry : island.getMembers().entrySet()) {
                statement.setString(1, island.getIslandUUID().toString());
                statement.setString(2, entry.getKey().toString());
                statement.setString(3, island.getMemberName(entry.getKey()));
                statement.setInt(4, entry.getValue().getId());
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "UPDATE island_members SET rank_id = ?, updated_at = CURRENT_TIMESTAMP, " +
                        "username = ? WHERE island_uuid = ? AND uuid = ?")) {
            for (Map.Entry<UUID, IslandRanks> entry : island.getMembers().entrySet()) {
                statement.setInt(1, entry.getValue().getId());
                statement.setString(2, island.getMemberName(entry.getKey()));
                statement.setString(3, island.getIslandUUID().toString());
                statement.setString(4, entry.getKey().toString());
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateIslandPerms(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT IGNORE INTO island_ranks_permissions (island_uuid, rank_id, permission_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            for (Map.Entry<IslandRanks, ArrayList<IslandPerms>> entry : island.getRanksPermsReduced().entrySet()) {
                for (IslandPerms perm : entry.getValue()) {
                    statement.setString(1, island.getIslandUUID().toString());
                    statement.setInt(2, entry.getKey().getId());
                    statement.setInt(3, perm.getId());
                    statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "UPDATE island_ranks_permissions SET rank_id = ?, updated_at = CURRENT_TIMESTAMP WHERE " +
                        "island_uuid = ? AND permission_id = ?")) {
            for (Map.Entry<IslandRanks, ArrayList<IslandPerms>> entry : island.getRanksPermsReduced().entrySet()) {
                for (IslandPerms perm : entry.getValue()) {
                    statement.setInt(1, entry.getKey().getId());
                    statement.setString(2, island.getIslandUUID().toString());
                    statement.setInt(3, perm.getId());
                    statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateIslandBanned(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT IGNORE INTO island_banneds (island_uuid, uuid, created_at, updated_at) VALUES (?, ?, " +
                        "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            for (UUID banned : island.getBannedPlayers()) {
                statement.setString(1, island.getIslandUUID().toString());
                statement.setString(2, banned.toString());
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateIslandSettings(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT IGNORE INTO island_settings (island_uuid, setting_id, value, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            for (IslandSettings setting : IslandSettings.values()) {
                statement.setString(1, island.getIslandUUID().toString());
                statement.setInt(2, setting.getId());
                statement.setBoolean(3, island.getSettings().contains(setting));
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "UPDATE island_settings SET value = ?, updated_at = CURRENT_TIMESTAMP WHERE island_uuid = ? AND setting_id = ?")) {
            for (IslandSettings setting : IslandSettings.values()) {
                statement.setBoolean(1, island.getSettings().contains(setting));
                statement.setString(2, island.getIslandUUID().toString());
                statement.setInt(3, setting.getId());
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pair<Map<UUID, IslandRanks>, Map<UUID, String>> loadIslandMembers(UUID islandUUID) {
        Map<UUID, IslandRanks> members = new HashMap<>();
        Map<UUID, String> membersNames = new HashMap<>();
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "SELECT * FROM island_members WHERE island_uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                UUID memberUUID = UUID.fromString(result.getString("uuid"));
                IslandRanks rank = IslandRanks.getById(result.getInt("rank_id"));
                String username = result.getString("username");
                members.put(memberUUID, rank);
                membersNames.put(memberUUID, username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Pair.of(members, membersNames);
    }

    public Map<IslandRanks, ArrayList<IslandPerms>> loadIslandPerms(UUID islandUUID) {
        Map<IslandRanks, ArrayList<IslandPerms>> perms = new HashMap<>();
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "SELECT * FROM island_ranks_permissions WHERE island_uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                IslandRanks rank = IslandRanks.getById(result.getInt("rank_id"));
                IslandPerms perm = IslandPerms.getById(result.getInt("permission_id"));
                if (perms.containsKey(rank)) {
                    perms.get(rank).add(perm);
                } else {
                    ArrayList<IslandPerms> permsList = new ArrayList<>();
                    permsList.add(perm);
                    perms.put(rank, permsList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Island.getRanksPermsFromReduced(perms);
    }

    public List<UUID> loadIslandBanned(UUID islandUUID) {
        List<UUID> bannedPlayers = new ArrayList<>();
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "SELECT * FROM island_banneds WHERE island_uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                UUID bannedUUID = UUID.fromString(result.getString("uuid"));
                bannedPlayers.add(bannedUUID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bannedPlayers;
    }

    public List<IslandSettings> loadIslandSettings(UUID islandUUID) {
        List<IslandSettings> settings = new ArrayList<>();
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "SELECT * FROM island_settings WHERE island_uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                IslandSettings setting = IslandSettings.getById(result.getInt("setting_id"));
                if (result.getBoolean("value")) {
                    settings.add(setting);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return settings;
    }

    public boolean saveIslandMembers(UUID islandUUID, Map<UUID, IslandRanks> members, Map<UUID, String> membersNames) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT INTO island_members (island_uuid, uuid, username, rank_id, created_at, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            for (Map.Entry<UUID, IslandRanks> entry : members.entrySet()) {
                statement.setString(1, islandUUID.toString());
                statement.setString(2, entry.getKey().toString());
                statement.setString(3, membersNames.get(entry.getKey()));
                statement.setInt(4, entry.getValue().getId());
                statement.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveIslandPerms(UUID islandUUID, Map<IslandRanks, ArrayList<IslandPerms>> perms) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT INTO island_ranks_permissions (island_uuid, rank_id, permission_id, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            for (Map.Entry<IslandRanks, ArrayList<IslandPerms>> entry : perms.entrySet()) {
                for (IslandPerms perm : entry.getValue()) {
                    statement.setString(1, islandUUID.toString());
                    statement.setInt(2, entry.getKey().getId());
                    statement.setInt(3, perm.getId());
                    statement.executeUpdate();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveIslandBanned(UUID islandUUID, List<UUID> bannedPlayers) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT INTO island_banneds (island_uuid, uuid, created_at, updated_at) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            for (UUID banned : bannedPlayers) {
                statement.setString(1, islandUUID.toString());
                statement.setString(2, banned.toString());
                statement.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveIslandSettings(UUID islandUuid, List<IslandSettings> settings) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT INTO island_settings (island_uuid, setting_id, value, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            for (IslandSettings setting : IslandSettings.values()) {
                statement.setString(1, islandUuid.toString());
                statement.setInt(2, setting.getId());
                statement.setBoolean(3, settings.contains(setting));
                statement.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteIsland(UUID islandUUID) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "DELETE FROM islands WHERE uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
            cache.remove(islandUUID);
            return null;
        });
    }
}
