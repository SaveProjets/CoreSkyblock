package fr.farmeurimmo.skylyblock.common;

import fr.farmeurimmo.skylyblock.common.islands.Island;
import fr.farmeurimmo.skylyblock.common.islands.IslandPerms;
import fr.farmeurimmo.skylyblock.common.islands.IslandRanks;
import fr.farmeurimmo.skylyblock.purpur.IslandsManager;
import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import fr.farmeurimmo.skylyblock.utils.LocationTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandsDataManager {

    public static IslandsDataManager INSTANCE;
    private final Map<UUID, Island> cache = new HashMap<>();

    public IslandsDataManager() {
        INSTANCE = this;

        CompletableFuture.runAsync(this::loadAllIslands);

        Bukkit.getScheduler().runTaskTimerAsynchronously(SkylyBlock.INSTANCE, () -> {
            for (Island island : cache.values()) {
                if (island.needUpdate()) {
                    island.update();
                }
            }
        }, 0, 20 * 60 * 3);
    }

    public Map<UUID, Island> getCache() {
        return cache;
    }

    public void loadAllIslands() {
        long startTime = System.currentTimeMillis();
        int count = 0;
        try {
            PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                    "SELECT * FROM islands");
            statement.executeQuery();

            ResultSet result = statement.getResultSet();
            while (result.next()) {
                UUID islandUUID = UUID.fromString(result.getString("uuid"));

                CompletableFuture.runAsync(() -> loadIsland(islandUUID)).thenRun(() -> {
                    Bukkit.getScheduler().callSyncMethod(SkylyBlock.INSTANCE, () -> {
                        Island island = cache.get(islandUUID);
                        if (island != null) {
                            IslandsManager.INSTANCE.loadIsland(island);
                        }
                        return null;
                    });
                });
            }

            System.out.println("Loaded " + count + " islands in " + (System.currentTimeMillis() - startTime) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadIsland(UUID uuid) {
        try {
            PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                    "SELECT * FROM islands WHERE uuid = ?");
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
                double bankCrystals = result.getDouble("bank_crystals");
                boolean isPublic = result.getBoolean("is_public");
                double level = result.getDouble("level");
                double levelExp = result.getDouble("exp");

                PreparedStatement membersStatement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                        "SELECT * FROM island_members WHERE island_uuid = ?");
                membersStatement.setString(1, uuid.toString());
                membersStatement.executeQuery();

                ResultSet membersResult = membersStatement.getResultSet();
                Map<UUID, IslandRanks> members = new HashMap<>();
                while (membersResult.next()) {
                    UUID memberUUID = UUID.fromString(membersResult.getString("uuid"));
                    IslandRanks rank = IslandRanks.getById(membersResult.getInt("rank_id"));
                    members.put(memberUUID, rank);
                }

                PreparedStatement permsStatement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                        "SELECT * FROM island_ranks_permissions WHERE island_uuid = ?");
                permsStatement.setString(1, uuid.toString());
                permsStatement.executeQuery();

                ResultSet permsResult = permsStatement.getResultSet();
                Map<IslandRanks, ArrayList<IslandPerms>> perms = new HashMap<>();
                while (permsResult.next()) {
                    IslandRanks rank = IslandRanks.getById(permsResult.getInt("rank_id"));
                    IslandPerms perm = IslandPerms.getById(permsResult.getInt("permission_id"));

                    if (perms.containsKey(rank)) {
                        perms.get(rank).add(perm);
                    } else {
                        ArrayList<IslandPerms> permsList = new ArrayList<>();
                        permsList.add(perm);
                        perms.put(rank, permsList);
                    }
                }

                PreparedStatement bannedStatement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                        "SELECT * FROM island_banneds WHERE island_uuid = ?");
                bannedStatement.setString(1, uuid.toString());
                bannedStatement.executeQuery();

                ResultSet bannedResult = bannedStatement.getResultSet();
                ArrayList<UUID> bannedPlayers = new ArrayList<>();
                while (bannedResult.next()) {
                    UUID bannedUUID = UUID.fromString(bannedResult.getString("uuid"));
                    bannedPlayers.add(bannedUUID);
                }

                Location spawn = LocationTranslator.fromString(locationString);

                Island island = new Island(uuid, name, spawn, members, perms, upgradeSize, upgradeMembers, upgradeGenerator,
                        bankMoney, bankCrystals, bannedPlayers, isPublic, level, levelExp);

                Bukkit.getScheduler().callSyncMethod(SkylyBlock.INSTANCE, () -> {
                    cache.put(uuid, island);
                    return null;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveIsland(Island island) {
        try {
            PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                    "INSERT INTO islands (uuid, name, spawn, upgrade_size, upgrade_members, upgrade_generator, " +
                            "bank_money, bank_crystals, is_public, level, exp, created_at, updated_at) VALUES (?, " +
                            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            statement.setString(1, island.getIslandUUID().toString());
            statement.setString(2, island.getName());
            statement.setString(3, LocationTranslator.fromLocation(island.getSpawn()));
            statement.setInt(4, island.getMaxSize());
            statement.setInt(5, island.getMaxMembers());
            statement.setInt(6, island.getGeneratorLevel());
            statement.setDouble(7, island.getBankMoney());
            statement.setDouble(8, island.getBankCrystals());
            statement.setBoolean(9, island.isPublic());
            statement.setDouble(10, island.getLevel());
            statement.setDouble(11, island.getLevelExp());
            statement.executeUpdate();

            PreparedStatement membersStatement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                    "INSERT INTO island_members (island_uuid, uuid, rank_id, created_at, updated_at) VALUES (?," +
                            " ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            for (Map.Entry<UUID, IslandRanks> entry : island.getMembers().entrySet()) {
                membersStatement.setString(1, island.getIslandUUID().toString());
                membersStatement.setString(2, entry.getKey().toString());
                membersStatement.setInt(3, entry.getValue().getId());
                membersStatement.executeUpdate();
            }

            PreparedStatement permsStatement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                    "INSERT INTO island_ranks_permissions (island_uuid, rank_id, permission_id, created_at, updated_at) " +
                            "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            for (Map.Entry<IslandRanks, ArrayList<IslandPerms>> entry : island.getPerms().entrySet()) {
                for (IslandPerms perm : entry.getValue()) {
                    permsStatement.setString(1, island.getIslandUUID().toString());
                    permsStatement.setInt(2, entry.getKey().getId());
                    permsStatement.setInt(3, perm.getId());
                    permsStatement.executeUpdate();
                }
            }

            PreparedStatement bannedStatement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                    "INSERT INTO island_banneds (island_uuid, uuid, created_at, updated_at) VALUES (?, ?, " +
                            "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            for (UUID banned : island.getBannedPlayers()) {
                bannedStatement.setString(1, island.getIslandUUID().toString());
                bannedStatement.setString(2, banned.toString());
                bannedStatement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(Island island, boolean membersModified, boolean permsModified, boolean bannedModified) {
        try {
            PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                    "UPDATE islands SET name = ?, spawn = ?, upgrade_size = ?, upgrade_members = ?, upgrade_generator = ?, " +
                            "bank_money = ?, bank_crystals = ?, is_public = ?, level = ?, exp = ?, " +
                            "updated_at = CURRENT_TIMESTAMP WHERE uuid = ?");
            statement.setString(1, island.getName());
            statement.setString(2, LocationTranslator.fromLocation(island.getSpawn()));
            statement.setInt(3, island.getMaxSize());
            statement.setInt(4, island.getMaxMembers());
            statement.setInt(5, island.getGeneratorLevel());
            statement.setDouble(6, island.getBankMoney());
            statement.setDouble(7, island.getBankCrystals());
            statement.setBoolean(8, island.isPublic());
            statement.setDouble(9, island.getLevel());
            statement.setDouble(10, island.getLevelExp());
            statement.setString(11, island.getIslandUUID().toString());
            statement.executeUpdate();

            if (membersModified) {
                PreparedStatement membersStatement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                        "UPDATE island_members SET rank_id = ?, updated_at = CURRENT_TIMESTAMP " +
                                "WHERE island_uuid = ? AND uuid = ?");
                for (Map.Entry<UUID, IslandRanks> entry : island.getMembers().entrySet()) {
                    membersStatement.setInt(1, entry.getValue().getId());
                    membersStatement.setString(2, island.getIslandUUID().toString());
                    membersStatement.setString(3, entry.getKey().toString());
                    membersStatement.executeUpdate();
                }
            }

            if (permsModified) {
                PreparedStatement permsStatement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                        "UPDATE island_ranks_permissions SET permission_id = ?, updated_at = CURRENT_TIMESTAMP " +
                                "WHERE island_uuid = ? AND rank_id = ?");
                for (Map.Entry<IslandRanks, ArrayList<IslandPerms>> entry : island.getPerms().entrySet()) {
                    for (IslandPerms perm : entry.getValue()) {
                        permsStatement.setInt(1, perm.getId());
                        permsStatement.setString(2, island.getIslandUUID().toString());
                        permsStatement.setInt(3, entry.getKey().getId());
                        permsStatement.executeUpdate();
                    }
                }
            }

            if (bannedModified) {
                PreparedStatement bannedStatement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                        "INSERT INTO island_banneds (island_uuid, uuid, created_at, updated_at) VALUES (?, ?, " +
                                "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
                for (UUID banned : island.getBannedPlayers()) {
                    bannedStatement.setString(1, island.getIslandUUID().toString());
                    bannedStatement.setString(2, banned.toString());
                    bannedStatement.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
