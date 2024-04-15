package fr.farmeurimmo.coreskyblock.storage.islands;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.chests.Chest;
import fr.farmeurimmo.coreskyblock.purpur.chests.ChestType;
import fr.farmeurimmo.coreskyblock.storage.DatabaseManager;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandSettings;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandWarpCategories;
import fr.farmeurimmo.coreskyblock.utils.InventorySyncUtils;
import fr.farmeurimmo.coreskyblock.utils.LocationTranslator;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
    private static final String CREATE_ISLAND_CHESTS_TABLE = "CREATE TABLE IF NOT EXISTS island_chests " +
            "(uuid VARCHAR(36) PRIMARY KEY, island_uuid VARCHAR(36), type_id INT, block VARCHAR(255), " +
            "item_to_buy_sell VARCHAR(255), price DOUBLE, is_sell BOOLEAN, active_sell_or_buy BOOLEAN, " +
            "amount_of_stacked_blocks BIGINT, created_at TIMESTAMP, updated_at TIMESTAMP, FOREIGN KEY(island_uuid) " +
            "REFERENCES islands(uuid) ON DELETE CASCADE)";
    private static final String CREATE_ISLAND_WARPS_TABLE = "CREATE TABLE IF NOT EXISTS island_warps " +
            "(uuid VARCHAR(36) PRIMARY KEY, island_uuid VARCHAR(36), name VARCHAR(255), description VARCHAR(1024), " +
            "categories VARCHAR(255), loc_tp VARCHAR(255), forward BIGINT, is_activated BOOL, material VARCHAR(96), " +
            "created_at TIMESTAMP, updated_at TIMESTAMP, FOREIGN KEY(island_uuid) REFERENCES islands(uuid) ON DELETE CASCADE)";
    public static IslandsDataManager INSTANCE;
    private final Map<UUID, Island> cache = new HashMap<>();

    public IslandsDataManager() {
        INSTANCE = this;

        // We no longer need to load all islands, we will load them when needed
        //CompletableFuture.runAsync(this::loadAllIslands);

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () -> {
            for (Island island : cache.values()) {
                if (island.needUpdate()) {
                    island.update(true);
                }
            }
        }, 0, 20 * 60 * 3);

        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            createTable(connection, CREATE_ISLANDS_TABLE);
            createTable(connection, CREATE_ISLAND_MEMBERS_TABLE);
            createTable(connection, CREATE_ISLAND_RANKS_PERMISSIONS_TABLE);
            createTable(connection, CREATE_ISLAND_BANNEDS_TABLE);
            createTable(connection, CREATE_ISLAND_SETTINGS_TABLE);
            createTable(connection, CREATE_ISLAND_CHESTS_TABLE);
            createTable(connection, CREATE_ISLAND_WARPS_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable(Connection connection, String createTableQuery) {
        try (PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void executeUpdate(Connection connection, String query, Object... parameters) {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, Island> getCache() {
        return cache;
    }

    /*public void loadAllIslands() {
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
    }*/

    public UUID getIslandByMember(UUID memberUUID) {
        try (PreparedStatement statement = DatabaseManager.INSTANCE.getConnection().prepareStatement(
                "SELECT island_uuid FROM island_members WHERE uuid = ?")) {
            statement.setString(1, memberUUID.toString());
            statement.executeQuery();

            ResultSet result = statement.getResultSet();
            if (result.next()) {
                return UUID.fromString(result.getString("island_uuid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Island getIsland(UUID uuid) {
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

                List<Chest> chests = loadIslandChests(uuid);

                Island island = new Island(uuid, name, spawn, members.left(), members.right(), perms, upgradeSize,
                        upgradeMembers, upgradeGenerator, bankMoney, bannedPlayers, isPublic, exp, settings, level,
                        chests, false);

                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    cache.put(uuid, island);
                    return null;
                });
                return island;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveIsland(Island island) {
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            saveIslandData(connection, island);

            saveIslandMembers(connection, island.getIslandUUID(), island.getMembers(), island.getMembersNames());

            saveIslandPerms(connection, island.getIslandUUID(), island.getRanksPermsReduced());

            // Not needed to save banned players because the island just got created
            //saveIslandBanned(connection, island.getIslandUUID(), island.getBannedPlayers());

            saveIslandSettings(connection, island.getIslandUUID(), island.getSettings());

            saveIslandChests(connection, island.getIslandUUID(), island.getChests());

            // Not needed to save warps because the island just got created

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void update(Island island, boolean membersModified, boolean permsModified, boolean bannedModified,
                       boolean settingsModified, boolean areChestsModified) {
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            updateIsland(connection, island);
            if (membersModified) {
                updateIslandMembers(connection, island);
            }
            if (permsModified) {
                updateIslandPerms(connection, island);
            }
            if (bannedModified) {
                updateIslandBanned(connection, island);
            }
            if (settingsModified) {
                updateIslandSettings(connection, island);
            }
            if (areChestsModified) {
                updateIslandChests(connection, island);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateIsland(Connection connection, Island island) {
        String query = "UPDATE islands SET name = ?, spawn = ?, upgrade_size = ?, upgrade_members = ?, upgrade_generator = ?, " +
                "bank_money = ?, is_public = ?, exp = ?, level = ?, updated_at = CURRENT_TIMESTAMP WHERE uuid = ?";
        executeUpdate(connection, query, island.getName(), LocationTranslator.fromLocation(island.getSpawn()),
                island.getMaxSize(), island.getMaxMembers(), island.getGeneratorLevel(), island.getBankMoney(),
                island.isPublic(), island.getExp(), island.getLevel(), island.getIslandUUID().toString());
    }

    public void updateIslandMembers(Connection connection, Island island) {
        for (Map.Entry<UUID, IslandRanks> entry : island.getMembers().entrySet()) {
            String query = "INSERT IGNORE INTO island_members (island_uuid, uuid, username, rank_id, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE rank_id = ?, " +
                    "updated_at = CURRENT_TIMESTAMP";
            executeUpdate(connection, query, island.getIslandUUID().toString(), entry.getKey().toString(),
                    island.getMemberName(entry.getKey()), entry.getValue().getId(), entry.getValue().getId());
        }
    }

    public void updateIslandPerms(Connection connection, Island island) {
        for (Map.Entry<IslandRanks, ArrayList<IslandPerms>> entry : island.getRanksPermsReduced().entrySet()) {
            for (IslandPerms perm : entry.getValue()) {
                String query = "INSERT IGNORE INTO island_ranks_permissions (island_uuid, rank_id, permission_id, " +
                        "created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                        "ON DUPLICATE KEY UPDATE rank_id = ?, permission_id = ?, updated_at = CURRENT_TIMESTAMP";
                executeUpdate(connection, query, island.getIslandUUID().toString(), entry.getKey().getId(), perm.getId(),
                        entry.getKey().getId(), perm.getId());
            }
        }
    }

    public void updateIslandBanned(Connection connection, Island island) {
        for (UUID banned : island.getBannedPlayers()) {
            String query = "INSERT IGNORE INTO island_banneds (island_uuid, uuid, created_at, updated_at) VALUES (?, ?, " +
                    "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            executeUpdate(connection, query, island.getIslandUUID().toString(), banned.toString());
        }
    }

    public void updateIslandSettings(Connection connection, Island island) {
        for (IslandSettings setting : IslandSettings.values()) {
            String query = "INSERT IGNORE INTO island_settings (island_uuid, setting_id, value, created_at, updated_at)" +
                    " VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE value = ?, " +
                    "updated_at = CURRENT_TIMESTAMP";
            executeUpdate(connection, query, island.getIslandUUID().toString(), setting.getId(), island.getSettings()
                    .contains(setting), island.getSettings().contains(setting));
        }
    }

    public void updateIslandChests(Connection connection, Island island) {
        for (Chest chest : island.getChests()) {
            String query = "INSERT IGNORE INTO island_chests (uuid, island_uuid, type_id, block, item_to_buy_sell, " +
                    "price, is_sell, " + "active_sell_or_buy, amount_of_stacked_blocks, created_at, updated_at) VALUES "
                    + "(?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE " +
                    "type_id = ?, block = ?, item_to_buy_sell = ?, price = ?, is_sell = ?, active_sell_or_buy = ?, " +
                    "amount_of_stacked_blocks = ?, updated_at = CURRENT_TIMESTAMP";
            executeUpdate(connection, query, chest.getUuid().toString(), island.getIslandUUID().toString(),
                    chest.getType().getId(), LocationTranslator.fromLocation(chest.getBlock()),
                    InventorySyncUtils.INSTANCE.itemStackToBase64(chest.getItemToBuySell()), chest.getPrice(),
                    chest.isSell(), chest.isActiveSellOrBuy(), chest.getAmountOfStackedBlocks(),
                    chest.getType().getId(), LocationTranslator.fromLocation(chest.getBlock()),
                    InventorySyncUtils.INSTANCE.itemStackToBase64(chest.getItemToBuySell()), chest.getPrice(),
                    chest.isSell(), chest.isActiveSellOrBuy(), chest.getAmountOfStackedBlocks());
        }
    }

    public void updateIslandWarp(IslandWarp islandWarp) {
        String query = "INSERT IGNORE INTO island_warps (uuid, island_uuid, name, description, categories, loc_tp, " +
                "forward, is_activated, material, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE name = ?, description = ?, categories = ?, " +
                "loc_tp = ?, forward = ?, is_activated = ?, material = ?, updated_at = CURRENT_TIMESTAMP";
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            executeUpdate(connection, query, islandWarp.getUuid().toString(), islandWarp.getIslandUUID().toString(),
                    islandWarp.getName(), islandWarp.getDescription(), islandWarp.getCategoriesString(),
                    LocationTranslator.fromLocation(islandWarp.getLocation()), islandWarp.getForwardedWarp(),
                    islandWarp.isActivated(), islandWarp.getMaterial().name(),
                    islandWarp.getName(), islandWarp.getDescription(), islandWarp.getCategoriesString(),
                    LocationTranslator.fromLocation(islandWarp.getLocation()), islandWarp.getForwardedWarp(),
                    islandWarp.isActivated(), islandWarp.getMaterial().name());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Pair<Map<UUID, IslandRanks>, Map<UUID, String>> loadIslandMembers(UUID islandUUID) {
        Map<UUID, IslandRanks> members = new HashMap<>();
        Map<UUID, String> membersNames = new HashMap<>();
        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM island_members WHERE island_uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    UUID memberUUID = UUID.fromString(result.getString("uuid"));
                    IslandRanks rank = IslandRanks.getById(result.getInt("rank_id"));
                    String username = result.getString("username");
                    members.put(memberUUID, rank);
                    membersNames.put(memberUUID, username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Pair.of(members, membersNames);
    }

    public Map<IslandRanks, ArrayList<IslandPerms>> loadIslandPerms(UUID islandUUID) {
        Map<IslandRanks, ArrayList<IslandPerms>> perms = new HashMap<>();
        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM island_ranks_permissions WHERE island_uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            try (ResultSet result = statement.executeQuery()) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Island.getRanksPermsFromReduced(perms);
    }

    public List<UUID> loadIslandBanned(UUID islandUUID) {
        List<UUID> bannedPlayers = new ArrayList<>();
        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM island_banneds WHERE island_uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    UUID bannedUUID = UUID.fromString(result.getString("uuid"));
                    bannedPlayers.add(bannedUUID);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bannedPlayers;
    }

    public List<IslandSettings> loadIslandSettings(UUID islandUUID) {
        List<IslandSettings> settings = new ArrayList<>();
        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM island_settings WHERE island_uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    IslandSettings setting = IslandSettings.getById(result.getInt("setting_id"));
                    if (result.getBoolean("value")) {
                        settings.add(setting);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return settings;
    }

    public List<Chest> loadIslandChests(UUID islandUUID) {
        List<Chest> chests = new ArrayList<>();
        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM island_chests WHERE island_uuid = ?")) {
            statement.setString(1, islandUUID.toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    UUID chestUUID = UUID.fromString(result.getString("uuid"));
                    int typeId = result.getInt("type_id");
                    String block = result.getString("block");
                    String jsonItem = result.getString("item_to_buy_sell");
                    ItemStack itemToBuySell = InventorySyncUtils.INSTANCE.itemStackFromBase64(jsonItem);
                    double price = result.getDouble("price");
                    boolean isSell = result.getBoolean("is_sell");
                    boolean activeSellOrBuy = result.getBoolean("active_sell_or_buy");
                    long amountOfStackedBlocks = result.getLong("amount_of_stacked_blocks");
                    chests.add(new Chest(chestUUID, islandUUID, ChestType.getById(typeId),
                            LocationTranslator.fromString(block), itemToBuySell, price, isSell, activeSellOrBuy,
                            amountOfStackedBlocks));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chests;
    }

    public List<IslandWarp> loadIslandsWarps() {
        List<IslandWarp> warps = new ArrayList<>();
        try (Connection connection = DatabaseManager.INSTANCE.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM island_warps")) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    try {
                        UUID uuid = UUID.fromString(result.getString("uuid"));
                        UUID islandUUID = UUID.fromString(result.getString("island_uuid"));
                        String name = result.getString("name");
                        String description = result.getString("description");
                        String categoriesString = result.getString("categories");
                        ArrayList<IslandWarpCategories> islandWarpCategories = new ArrayList<>();
                        if (categoriesString != null && !categoriesString.isEmpty()) {
                            islandWarpCategories = IslandWarp.getCategoriesFromString(categoriesString);
                        }
                        Location location = LocationTranslator.fromString(result.getString("loc_tp"));
                        boolean isActivated = result.getBoolean("is_activated");
                        long forwardedWarp = result.getLong("forward");
                        Material material = Material.getMaterial(result.getString("material"));
                        warps.add(new IslandWarp(uuid, islandUUID, name, description, islandWarpCategories, location,
                                isActivated, forwardedWarp, material));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warps;
    }

    public void saveIslandData(Connection connection, Island island) {
        String query = "INSERT INTO islands (uuid, name, spawn, upgrade_size, upgrade_members, upgrade_generator, " +
                "bank_money, is_public, exp, level, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        executeUpdate(connection, query, island.getIslandUUID().toString(), island.getName(),
                LocationTranslator.fromLocation(island.getSpawn()),
                island.getMaxSize(), island.getMaxMembers(), island.getGeneratorLevel(), island.getBankMoney(),
                island.isPublic(), island.getExp(), island.getLevel());
    }

    public void saveIslandMembers(Connection connection, UUID islandUUID, Map<UUID, IslandRanks> members, Map<UUID,
            String> membersNames) {
        for (Map.Entry<UUID, IslandRanks> entry : members.entrySet()) {
            String query = "INSERT INTO island_members (island_uuid, uuid, username, rank_id, created_at, " +
                    "updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            executeUpdate(connection, query, islandUUID.toString(), entry.getKey().toString(),
                    membersNames.get(entry.getKey()), entry.getValue().getId());
        }
    }

    public void saveIslandPerms(Connection connection, UUID islandUUID, Map<IslandRanks, ArrayList<IslandPerms>> perms) {
        for (Map.Entry<IslandRanks, ArrayList<IslandPerms>> entry : perms.entrySet()) {
            for (IslandPerms perm : entry.getValue()) {
                String query = "INSERT INTO island_ranks_permissions (island_uuid, rank_id, permission_id, " +
                        "created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                executeUpdate(connection, query, islandUUID.toString(), entry.getKey().getId(), perm.getId());
            }
        }
    }

    /*public void saveIslandBanned(Connection connection, UUID islandUUID, List<UUID> bannedPlayers) {
        for (UUID banned : bannedPlayers) {
            String query = "INSERT INTO island_banneds (island_uuid, uuid, created_at, updated_at) VALUES (?, ?, " +
                    "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            executeUpdate(connection, query, islandUUID.toString(), banned.toString());
        }
    }*/

    public void saveIslandSettings(Connection connection, UUID islandUuid, List<IslandSettings> settings) {
        for (IslandSettings setting : IslandSettings.values()) {
            String query = "INSERT INTO island_settings (island_uuid, setting_id, value, created_at, updated_at) " +
                    "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            executeUpdate(connection, query, islandUuid.toString(), setting.getId(), settings.contains(setting));
        }
    }

    public void saveIslandChests(Connection connection, UUID islandUUID, List<Chest> chests) {
        for (Chest chest : chests) {
            String query = "INSERT INTO island_chests (uuid, island_uuid, type_id, block, item_to_buy_sell, price, " +
                    "is_sell, active_sell_or_buy, amount_of_stacked_blocks, created_at, updated_at) VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            executeUpdate(connection, query, chest.getUuid().toString(), islandUUID.toString(), chest.getType().getId(),
                    LocationTranslator.fromLocation(chest.getBlock()),
                    InventorySyncUtils.INSTANCE.itemStackToBase64(chest.getItemToBuySell()), chest.getPrice(),
                    chest.isSell(), chest.isActiveSellOrBuy(), chest.getAmountOfStackedBlocks());
        }
    }

    public void deleteIsland(UUID islandUUID) {
        String query = "DELETE FROM islands WHERE uuid = ?";
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            executeUpdate(connection, query, islandUUID.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
            cache.remove(islandUUID);
            return null;
        });
    }

    public void deleteChest(UUID chestUUID) {
        String query = "DELETE FROM island_chests WHERE uuid = ?";
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            executeUpdate(connection, query, chestUUID.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteBanned(UUID islandUUID, UUID bannedUUID) {
        String query = "DELETE FROM island_banneds WHERE island_uuid = ? AND uuid = ?";
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            executeUpdate(connection, query, islandUUID.toString(), bannedUUID.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMember(UUID islandUUID, UUID memberUUID) {
        String query = "DELETE FROM island_members WHERE island_uuid = ? AND uuid = ?";
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            executeUpdate(connection, query, islandUUID.toString(), memberUUID.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteWarp(UUID warpUUID) {
        String query = "DELETE FROM island_warps WHERE uuid = ?";
        try (Connection connection = DatabaseManager.INSTANCE.getConnection()) {
            executeUpdate(connection, query, warpUUID.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
