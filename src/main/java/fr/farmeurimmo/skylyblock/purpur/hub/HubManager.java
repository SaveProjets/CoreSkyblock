package fr.farmeurimmo.skylyblock.purpur.hub;

import fr.farmeurimmo.skylyblock.purpur.hub.cmds.BuildHubCmd;
import fr.farmeurimmo.skylyblock.purpur.hub.listeners.HubListener;
import fr.farmeurimmo.skylyblock.purpur.hub.listeners.HubPlayerListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;

public class HubManager {

    public static HubManager INSTANCE;
    public static Location SPAWN = new Location(Bukkit.getWorld("hub"), 0.5, 80, 0.5, 0, 0);
    private final JavaPlugin plugin;
    public ArrayList<UUID> buildModePlayers = new ArrayList<>();

    public HubManager(JavaPlugin plugin) {
        INSTANCE = this;
        this.plugin = plugin;

        ConsoleCommandSender console = plugin.getServer().getConsoleSender();
        console.sendMessage("§b[SkylyBlock] §7Démarrage de la partie hub...");
        SPAWN.setWorld(Bukkit.getWorld("hub"));

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des managers...");

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des commandes...");
        plugin.getCommand("buildhub").setExecutor(new BuildHubCmd());

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des événements...");
        plugin.getServer().getPluginManager().registerEvents(new HubPlayerListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new HubListener(), plugin);

        console.sendMessage("§b[SkylyBlock] §7Démarrage des tâches...");
        clockForBuildMode();

        console.sendMessage("§b[SkylyBlock] §7Optimisation du serveur...");
        optimizeServer();

        console.sendMessage("§b[SkylyBlock] §aDémarrage de la partie hub terminé");
    }

    public void optimizeServer() {
        for (World world : plugin.getServer().getWorlds()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setTime(6000);
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(0);
            world.setThunderDuration(0);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
            world.setGameRule(GameRule.DO_TILE_DROPS, false);
            world.setGameRule(GameRule.DO_MOB_LOOT, false);
            world.setGameRule(GameRule.FALL_DAMAGE, false);
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            world.setGameRule(GameRule.DO_INSOMNIA, false);
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
            world.setGameRule(GameRule.DROWNING_DAMAGE, false);
            world.setGameRule(GameRule.FIRE_DAMAGE, false);
            world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
            world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }
    }

    public void clockForBuildMode() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            ArrayList<UUID> toRemove = new ArrayList<>();
            for (UUID uuid : buildModePlayers) {
                Player p = plugin.getServer().getPlayer(uuid);
                if (p == null) {
                    toRemove.add(uuid);
                    continue;
                }
                p.sendActionBar(Component.text("§c§lVous êtes en mode construction."));
            }
            buildModePlayers.removeAll(toRemove);
        }, 0, 20);
    }
}
