package fr.farmeurimmo.skylyblock.purpur.island;

import fr.farmeurimmo.skylyblock.common.islands.Island;
import fr.farmeurimmo.skylyblock.purpur.core.worlds.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;

public class IslandsManager {

    public static IslandsManager INSTANCE;
    private final JavaPlugin plugin;
    private final ArrayList<Island> islands = new ArrayList<>();

    public IslandsManager(JavaPlugin plugin) {
        INSTANCE = this;
        this.plugin = plugin;

        ConsoleCommandSender console = plugin.getServer().getConsoleSender();
        console.sendMessage("§b[SkylyBlock] §7Démarrage de la partie île...");

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des managers...");

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des commandes...");

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des événements...");

        console.sendMessage("§b[SkylyBlock] §7Démarrage des tâches...");

        console.sendMessage("§b[SkylyBlock] §aDémarrage de la partie hub terminé");
    }

    public void disable() {
        WorldManager.INSTANCE.unload("island_template_1", false);
    }

    public void createIsland(UUID owner) {
        long startTime = System.currentTimeMillis();
        UUID islandId = UUID.randomUUID();
        String worldName = "island_" + islandId;

        Player player = plugin.getServer().getPlayer(owner);
        if (player == null) return;
        player.sendMessage("§b[SkylyBlock] §aCréation de votre île...");

        WorldManager.INSTANCE.cloneAndLoad(worldName, "island_template_1");
        islands.add(new Island(islandId, new Location(Bukkit.getWorld(worldName), -0.5, 80.1, -0.5,
                -50, 5), owner));

        Player ownerPlayer = plugin.getServer().getPlayer(owner);
        if (ownerPlayer == null) return;
        World w = Bukkit.getWorld(worldName);
        if (w == null) return;
        w.setSpawnLocation(new Location(w, 0, 80, 0, -38, 5));
        ownerPlayer.teleportAsync(w.getSpawnLocation());
        ownerPlayer.sendMessage("§b[SkylyBlock] §aVotre île a été créée en " + (System.currentTimeMillis() - startTime) + "ms");
    }

    public Island getIslandOf(UUID uuid) {
        for (Island island : islands) {
            if (island.getMembers().containsKey(uuid)) return island;
        }
        return null;
    }
}
