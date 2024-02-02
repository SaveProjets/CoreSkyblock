package fr.farmeurimmo.skylyblock.purpur;

import fr.farmeurimmo.skylyblock.common.IslandsDataManager;
import fr.farmeurimmo.skylyblock.common.islands.Island;
import fr.farmeurimmo.skylyblock.purpur.worlds.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandsManager {

    public static IslandsManager INSTANCE;
    private final JavaPlugin plugin;

    public IslandsManager(JavaPlugin plugin) {
        INSTANCE = this;
        this.plugin = plugin;

        new IslandsDataManager();
    }

    public void createIsland(UUID owner) {
        long startTime = System.currentTimeMillis();
        UUID islandId = UUID.randomUUID();
        String worldName = getIslandWorldName(islandId);

        Player player = plugin.getServer().getPlayer(owner);
        if (player == null) return;
        player.sendMessage("§b[SkylyBlock] §aCréation de votre île...");

        WorldManager.INSTANCE.cloneAndLoad(worldName, "island_template_1");
        Island island = new Island(islandId, new Location(Bukkit.getWorld(worldName), -0.5, 80.1, -0.5,
                -50, 5), owner);
        CompletableFuture.runAsync(() -> IslandsDataManager.INSTANCE.saveIsland(island));
        IslandsDataManager.INSTANCE.getCache().put(islandId, island);

        Player ownerPlayer = plugin.getServer().getPlayer(owner);
        if (ownerPlayer == null) return;
        World w = Bukkit.getWorld(worldName);
        if (w == null) return;
        w.setSpawnLocation(new Location(w, 0, 80, 0, -38, 5));
        ownerPlayer.teleportAsync(w.getSpawnLocation());
        ownerPlayer.sendMessage("§b[SkylyBlock] §aVotre île a été créée en " + (System.currentTimeMillis() - startTime) + "ms");
    }

    public String getIslandWorldName(UUID islandUUID) {
        return "island_" + islandUUID;
    }

    public Island getIslandOf(UUID uuid) {
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (island.getMembers().containsKey(uuid)) return island;
        }
        return null;
    }

    public void loadIsland(Island island) {
        WorldManager.INSTANCE.load(getIslandWorldName(island.getIslandUUID()), false);

        Location spawn = island.getSpawn();
        if (spawn != null) {
            spawn.setWorld(Bukkit.getWorld(getIslandWorldName(island.getIslandUUID())));
        }
    }

    public boolean isAnIsland(World world) {
        return world.getName().startsWith("island_");
    }
}
