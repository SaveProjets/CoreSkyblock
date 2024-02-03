package fr.farmeurimmo.skylyblock.common;

import fr.farmeurimmo.skylyblock.common.islands.Island;
import fr.farmeurimmo.skylyblock.common.islands.IslandPerms;
import fr.farmeurimmo.skylyblock.common.islands.IslandRanks;
import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
import fr.farmeurimmo.skylyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.skylyblock.utils.LocationTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
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

        CompletableFuture.allOf(futures).thenRun(() -> {
            Bukkit.getScheduler().callSyncMethod(SkylyBlock.INSTANCE, () -> {
                for (UUID islandUUID : islandUUIDs) {
                    Island island = cache.get(islandUUID);
                    if (island != null) {
                        IslandsManager.INSTANCE.loadIsland(island);
                    }
                }
                return null;
            });
            System.out.println("Loaded " + islandUUIDs.size() + " islands in " + (System.currentTimeMillis() - startTime) + "ms");
        });
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
                double bankCrystals = result.getDouble("bank_crystals");
                boolean isPublic = result.getBoolean("is_public");
                double level = result.getDouble("level");
                double levelExp = result.getDouble("exp");

                Map<UUID, IslandRanks> members = loadIslandMembers(uuid);

                Map<IslandRanks, ArrayList<IslandPerms>> perms = loadIslandPerms(uuid);

                ArrayList<UUID> bannedPlayers = new ArrayList<>(loadIslandBanned(uuid));

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

    public boolean saveIsland(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT INTO islands (uuid, name, spawn, upgrade_size, upgrade_members, upgrade_generator, " +
                        "bank_money, bank_crystals, is_public, level, exp, created_at, updated_at) VALUES (?, " +
                        "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (!saveIslandMembers(island.getIslandUUID(), island.getMembers())) {
            return false;
        }

        if (!saveIslandPerms(island.getIslandUUID(), island.getPerms())) {
            return false;
        }

        if (!saveIslandBanned(island.getIslandUUID(), island.getBannedPlayers())) {
            return false;
        }

        return true;
    }

    public void update(Island island, boolean membersModified, boolean permsModified, boolean bannedModified) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateIsland(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "UPDATE islands SET name = ?, spawn = ?, upgrade_size = ?, upgrade_members = ?, upgrade_generator = ?, " +
                        "bank_money = ?, bank_crystals = ?, is_public = ?, level = ?, exp = ?, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE uuid = ?")) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateIslandMembers(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "UPDATE island_members SET rank_id = ?, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE island_uuid = ? AND uuid = ?")) {
            for (Map.Entry<UUID, IslandRanks> entry : island.getMembers().entrySet()) {
                statement.setInt(1, entry.getValue().getId());
                statement.setString(2, island.getIslandUUID().toString());
                statement.setString(3, entry.getKey().toString());
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateIslandPerms(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "UPDATE island_ranks_permissions SET permission_id = ?, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE island_uuid = ? AND rank_id = ?")) {
            for (Map.Entry<IslandRanks, ArrayList<IslandPerms>> entry : island.getPerms().entrySet()) {
                for (IslandPerms perm : entry.getValue()) {
                    statement.setInt(1, perm.getId());
                    statement.setString(2, island.getIslandUUID().toString());
                    statement.setInt(3, entry.getKey().getId());
                    statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateIslandBanned(Island island) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT INTO island_banneds (island_uuid, uuid, created_at, updated_at) VALUES (?, ?, " +
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

    public Map<UUID, IslandRanks> loadIslandMembers(UUID islandUUID) {
        Map<UUID, IslandRanks> members = new HashMap<>();
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "SELECT * FROM island_members WHERE island_uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                UUID memberUUID = UUID.fromString(result.getString("uuid"));
                IslandRanks rank = IslandRanks.getById(result.getInt("rank_id"));
                members.put(memberUUID, rank);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return members;
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
        return perms;
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

    public boolean saveIslandMembers(UUID islandUUID, Map<UUID, IslandRanks> members) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "INSERT INTO island_members (island_uuid, uuid, rank_id, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
            for (Map.Entry<UUID, IslandRanks> entry : members.entrySet()) {
                statement.setString(1, islandUUID.toString());
                statement.setString(2, entry.getKey().toString());
                statement.setInt(3, entry.getValue().getId());
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

}
