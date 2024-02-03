package fr.farmeurimmo.skylyblock.purpur.islands;

import fr.farmeurimmo.skylyblock.common.IslandsDataManager;
import fr.farmeurimmo.skylyblock.common.islands.Island;
import fr.farmeurimmo.skylyblock.purpur.worlds.WorldManager;
import net.kyori.adventure.text.Component;
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

        Island island = new Island(islandId, new Location(Bukkit.getWorld(worldName), -0.5, 80.1, -0.5,
                -50, 5), owner);
        CompletableFuture.supplyAsync(() -> IslandsDataManager.INSTANCE.saveIsland(island)).thenAccept(result -> {
            if (!result) {
                player.sendMessage(Component.text("§cUne erreur est survenue lors de la création de votre île."));
                player.sendMessage(Component.text("§cVeuillez réessayer plus tard."));
                return;
            }
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                WorldManager.INSTANCE.cloneAndLoad(worldName, "island_template_1");
                IslandsDataManager.INSTANCE.getCache().put(islandId, island);
                Player ownerPlayer = plugin.getServer().getPlayer(owner);
                if (ownerPlayer == null) return null;
                World w = Bukkit.getWorld(worldName);
                if (w == null) return null;
                w.setSpawnLocation(new Location(w, 0, 80, 0, -38, 5));
                ownerPlayer.teleportAsync(w.getSpawnLocation());
                ownerPlayer.sendMessage("§b[SkylyBlock] §aVotre île a été créée en " + (System.currentTimeMillis() - startTime) + "ms");
                return null;
            });
        });
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

    public void teleportToIsland(Island island, Player p) {
        if (island == null) {
            p.sendMessage(Component.text(""));
            return;
        }
        //p.sendMessage();
    }
}
