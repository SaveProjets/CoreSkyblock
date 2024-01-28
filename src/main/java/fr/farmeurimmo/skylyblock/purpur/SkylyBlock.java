package fr.farmeurimmo.skylyblock.purpur;

import com.grinderwolf.swm.api.SlimePlugin;
import fr.farmeurimmo.skylyblock.common.DatabaseManager;
import fr.farmeurimmo.skylyblock.common.JedisManager;
import fr.farmeurimmo.skylyblock.common.SkyblockUsersManager;
import fr.farmeurimmo.skylyblock.purpur.chests.ChestsCmd;
import fr.farmeurimmo.skylyblock.purpur.chests.ChestsListener;
import fr.farmeurimmo.skylyblock.purpur.chests.ChestsManager;
import fr.farmeurimmo.skylyblock.purpur.cmds.BuildSpawnCmd;
import fr.farmeurimmo.skylyblock.purpur.cmds.IslandCmd;
import fr.farmeurimmo.skylyblock.purpur.cmds.SpawnCmd;
import fr.farmeurimmo.skylyblock.purpur.eco.MoneyCmd;
import fr.farmeurimmo.skylyblock.purpur.events.ChatReactionManager;
import fr.farmeurimmo.skylyblock.purpur.featherfly.FeatherFlyCmd;
import fr.farmeurimmo.skylyblock.purpur.featherfly.FeatherFlyListener;
import fr.farmeurimmo.skylyblock.purpur.featherfly.FeatherFlyManager;
import fr.farmeurimmo.skylyblock.purpur.listeners.ChatListener;
import fr.farmeurimmo.skylyblock.purpur.listeners.ChatReactionListener;
import fr.farmeurimmo.skylyblock.purpur.listeners.PlayerListener;
import fr.farmeurimmo.skylyblock.purpur.listeners.SpawnProtectionListener;
import fr.farmeurimmo.skylyblock.purpur.minions.MinionsCmd;
import fr.farmeurimmo.skylyblock.purpur.minions.MinionsListener;
import fr.farmeurimmo.skylyblock.purpur.minions.MinionsManager;
import fr.farmeurimmo.skylyblock.purpur.scoreboard.ScoreboardManager;
import fr.farmeurimmo.skylyblock.purpur.shop.ShopsManager;
import fr.farmeurimmo.skylyblock.purpur.shop.cmds.ShopCmd;
import fr.farmeurimmo.skylyblock.purpur.trade.*;
import fr.farmeurimmo.skylyblock.purpur.worlds.WorldManager;
import fr.farmeurimmo.skylyblock.utils.InventorySyncUtils;
import fr.mrmicky.fastinv.FastInvManager;
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

public final class SkylyBlock extends JavaPlugin {

    public static final String SPAWN_WORLD_NAME = "spawn";
    public static final boolean SPAWN_IN_READ_ONLY = true;
    public static SkylyBlock INSTANCE;
    public static Location SPAWN = new Location(Bukkit.getWorld(SPAWN_WORLD_NAME), 0.5, 80, 0.5, 180, 0);
    public static String SERVER_NAME;
    public ConsoleCommandSender console;
    public SlimePlugin slimePlugin;
    public ArrayList<UUID> buildModePlayers = new ArrayList<>();

    @Override
    public void onLoad() {
        INSTANCE = this;
        console = getServer().getConsoleSender();
        console.sendMessage("§b[SkylyBlock] §7Chargement des mondes...");
        slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        new WorldManager();

        console.sendMessage("§b[SkylyBlock] §7Chargement du spawn...");
        WorldManager.INSTANCE.loadOrCreate(SPAWN_WORLD_NAME, SPAWN_IN_READ_ONLY);
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        saveResource("old-shop.yml", true);

        console.sendMessage("§b[SkylyBlock] §7Démarrage du plugin SkylyBlock...");

        SERVER_NAME = System.getenv("SERVER_NAME");
        if (SERVER_NAME == null) SERVER_NAME = "dev";

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des dépendances...");
        FastInvManager.register(INSTANCE);
        new InventorySyncUtils();

        SPAWN.setWorld(Bukkit.getWorld(SPAWN_WORLD_NAME));

        console.sendMessage("§b[SkylyBlock] §7Démarrage des managers...");
        new DatabaseManager("jdbc:mysql://tools-databases-mariadb-1:3306/skyblock", "skyblock",
                "VNGsQzbUnYvw5Fpo");

        new SkyblockUsersManager();
        new IslandsManager(INSTANCE);

        new ScoreboardManager();
        new FeatherFlyManager();
        new ChatReactionManager();
        new ChestsManager();
        new MinionsManager();
        new TradesManager();

        new ShopsManager();

        console.sendMessage("§b[SkylyBlock] §7Connexion à redis...");
        new JedisManager();

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des listeners...");
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new FeatherFlyListener(), this);
        getServer().getPluginManager().registerEvents(new ChatReactionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new ChestsListener(), this);
        getServer().getPluginManager().registerEvents(new MinionsListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(), this);

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des commandes...");
        getCommand("featherfly").setExecutor(new FeatherFlyCmd());
        getCommand("money").setExecutor(new MoneyCmd());
        getCommand("is").setExecutor(new IslandCmd());
        getCommand("chests").setExecutor(new ChestsCmd());
        getCommand("minions").setExecutor(new MinionsCmd());
        getCommand("trade").setExecutor(new TradeCmd());
        getCommand("tradeaccept").setExecutor(new TradeAcceptCmd());
        getCommand("tradedeny").setExecutor(new TradeDenyCmd());
        getCommand("tradecancel").setExecutor(new TradeCancelCmd());
        getCommand("buildspawn").setExecutor(new BuildSpawnCmd());
        getCommand("spawn").setExecutor(new SpawnCmd());
        getCommand("shop").setExecutor(new ShopCmd());

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des tâches...");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::clockSendPlayerConnectedToRedis, 0, 20 * 3);
        clockForBuildMode();

        console.sendMessage("§b[SkylyBlock] §aDémarrage du plugin SkylyBlock terminé en " + (System.currentTimeMillis() - startTime) + "ms");
    }

    @Override
    public void onDisable() {
        console.sendMessage("§6Arrêt du plugin SkylyBlock");

        WorldManager.INSTANCE.unload(SPAWN_WORLD_NAME, !SPAWN_IN_READ_ONLY);
        WorldManager.INSTANCE.unload("island_template_1", false);

        JedisManager.INSTANCE.onDisable();

        console.sendMessage("§aArrêt du plugin SkylyBlock terminé");
    }

    public void optimizeSpawn() {
        World w = Bukkit.getWorld(SPAWN_WORLD_NAME);
        if (w == null) return;
        w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        w.setTime(6000);
        w.setStorm(false);
        w.setThundering(false);
        w.setWeatherDuration(0);
        w.setThunderDuration(0);
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        w.setGameRule(GameRule.DO_FIRE_TICK, false);
        w.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        w.setGameRule(GameRule.DO_TILE_DROPS, false);
        w.setGameRule(GameRule.DO_MOB_LOOT, false);
        w.setGameRule(GameRule.FALL_DAMAGE, false);
        w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        w.setGameRule(GameRule.DO_INSOMNIA, false);
        w.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        w.setGameRule(GameRule.DROWNING_DAMAGE, false);
        w.setGameRule(GameRule.FIRE_DAMAGE, false);
        w.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        w.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        w.setGameRule(GameRule.MOB_GRIEFING, false);
        w.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }

    public void clockSendPlayerConnectedToRedis() {
        ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        StringBuilder sb = new StringBuilder();
        for (Player player : players) {
            sb.append(player.getUniqueId()).append(":").append(player.getName()).append(",");
        }
        if (!sb.isEmpty()) sb.deleteCharAt(sb.length() - 1);
        JedisManager.INSTANCE.sendToRedis("skylyblock:players:" + SERVER_NAME, sb.toString());
    }

    public boolean isASpawn(World world) {
        return SPAWN_WORLD_NAME.equals(world.getName());
    }

    public void clockForBuildMode() {
        getServer().getScheduler().runTaskTimerAsynchronously(INSTANCE, () -> {
            ArrayList<UUID> toRemove = new ArrayList<>();
            for (UUID uuid : buildModePlayers) {
                Player p = getServer().getPlayer(uuid);
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
