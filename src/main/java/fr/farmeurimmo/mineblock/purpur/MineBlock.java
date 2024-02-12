package fr.farmeurimmo.mineblock.purpur;

import com.grinderwolf.swm.api.SlimePlugin;
import fr.farmeurimmo.mineblock.common.DatabaseManager;
import fr.farmeurimmo.mineblock.common.JedisManager;
import fr.farmeurimmo.mineblock.common.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.mineblock.purpur.chat.ChatDisplayManager;
import fr.farmeurimmo.mineblock.purpur.chests.ChestsCmd;
import fr.farmeurimmo.mineblock.purpur.chests.ChestsListener;
import fr.farmeurimmo.mineblock.purpur.chests.ChestsManager;
import fr.farmeurimmo.mineblock.purpur.cmds.BuildSpawnCmd;
import fr.farmeurimmo.mineblock.purpur.cmds.base.*;
import fr.farmeurimmo.mineblock.purpur.eco.MoneyCmd;
import fr.farmeurimmo.mineblock.purpur.events.ChatReactionManager;
import fr.farmeurimmo.mineblock.purpur.featherfly.FeatherFlyCmd;
import fr.farmeurimmo.mineblock.purpur.featherfly.FeatherFlyListener;
import fr.farmeurimmo.mineblock.purpur.featherfly.FeatherFlyManager;
import fr.farmeurimmo.mineblock.purpur.islands.IslandCmd;
import fr.farmeurimmo.mineblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.mineblock.purpur.listeners.ChatListener;
import fr.farmeurimmo.mineblock.purpur.listeners.ChatReactionListener;
import fr.farmeurimmo.mineblock.purpur.listeners.PlayerListener;
import fr.farmeurimmo.mineblock.purpur.listeners.SpawnProtectionListener;
import fr.farmeurimmo.mineblock.purpur.minions.MinionsCmd;
import fr.farmeurimmo.mineblock.purpur.minions.MinionsListener;
import fr.farmeurimmo.mineblock.purpur.minions.MinionsManager;
import fr.farmeurimmo.mineblock.purpur.scoreboard.ScoreboardManager;
import fr.farmeurimmo.mineblock.purpur.shop.ShopsManager;
import fr.farmeurimmo.mineblock.purpur.shop.cmds.SellAllCmd;
import fr.farmeurimmo.mineblock.purpur.shop.cmds.ShopCmd;
import fr.farmeurimmo.mineblock.purpur.silos.SiloCmd;
import fr.farmeurimmo.mineblock.purpur.silos.SilosListener;
import fr.farmeurimmo.mineblock.purpur.silos.SilosManager;
import fr.farmeurimmo.mineblock.purpur.tpa.TpaCmd;
import fr.farmeurimmo.mineblock.purpur.tpa.TpasManager;
import fr.farmeurimmo.mineblock.purpur.trade.*;
import fr.farmeurimmo.mineblock.purpur.worlds.WorldsManager;
import fr.farmeurimmo.mineblock.utils.InventorySyncUtils;
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

public final class MineBlock extends JavaPlugin {

    public static final String SPAWN_WORLD_NAME = "spawn";
    public static final boolean SPAWN_IN_READ_ONLY = true;
    public static MineBlock INSTANCE;
    public static Location SPAWN = new Location(Bukkit.getWorld(SPAWN_WORLD_NAME), 0.5, 80, 0.5, 180, 0);
    public static String SERVER_NAME;
    public static Location ENCHANTING_TABLE_LOCATION;
    public ConsoleCommandSender console;
    public SlimePlugin slimePlugin;
    public ArrayList<UUID> buildModePlayers = new ArrayList<>();

    @Override
    public void onLoad() {
        INSTANCE = this;
        console = getServer().getConsoleSender();
        console.sendMessage("§b[MineBlock] §7Chargement des mondes...");
        slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        new WorldsManager();

        console.sendMessage("§b[MineBlock] §7Chargement du spawn...");
        WorldsManager.INSTANCE.loadOrCreate(SPAWN_WORLD_NAME, SPAWN_IN_READ_ONLY);
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        saveResource("old-shop.yml", true);
        saveResource("config.yml", false);

        console.sendMessage("§b[MineBlock] §7Démarrage du plugin MineBlock...");

        SERVER_NAME = System.getenv("SERVER_NAME");
        if (SERVER_NAME == null) SERVER_NAME = "dev";

        console.sendMessage("§b[MineBlock] §7Enregistrement des dépendances...");
        FastInvManager.register(INSTANCE);
        new InventorySyncUtils();

        SPAWN.setWorld(Bukkit.getWorld(SPAWN_WORLD_NAME));

        console.sendMessage("§b[MineBlock] §7Connexion à la base de donnée...");
        try {
            String host = getConfig().getString("mysql.host");
            String user = getConfig().getString("mysql.user");
            String password = getConfig().getString("mysql.password");
            String database = getConfig().getString("mysql.database");
            int port = getConfig().getInt("mysql.port");
            new DatabaseManager("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.shutdown();
            return;
        }

        console.sendMessage("§b[MineBlock] §7Démarrage des managers...");
        new SkyblockUsersManager();
        new IslandsManager(INSTANCE);

        new ScoreboardManager();
        new FeatherFlyManager();
        new ChatReactionManager();
        new ChestsManager();
        new MinionsManager();
        new TradesManager();
        new TpasManager();

        new ShopsManager();
        new SilosManager();

        new ChatDisplayManager();

        console.sendMessage("§b[MineBlock] §7Connexion à redis...");
        new JedisManager();

        console.sendMessage("§b[MineBlock] §7Enregistrement des listeners...");
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new FeatherFlyListener(), this);
        getServer().getPluginManager().registerEvents(new ChatReactionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new ChestsListener(), this);
        getServer().getPluginManager().registerEvents(new MinionsListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new SilosListener(), this);

        console.sendMessage("§b[MineBlock] §7Enregistrement des commandes...");
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
        getCommand("sellall").setExecutor(new SellAllCmd());
        getCommand("trash").setExecutor(new TrashCmd());
        getCommand("craft").setExecutor(new CraftCmd());
        getCommand("enchantement").setExecutor(new EnchantementCmd());
        getCommand("anvil").setExecutor(new AnvilCmd());
        getCommand("furnace").setExecutor(new FurnaceCmd());
        getCommand("silo").setExecutor(new SiloCmd());
        getCommand("feed").setExecutor(new FeedCmd());
        getCommand("near").setExecutor(new NearCmd());
        getCommand("fix").setExecutor(new FixCmd());
        getCommand("kits").setExecutor(new KitsCmd());
        getCommand("tpa").setExecutor(new TpaCmd());

        console.sendMessage("§b[MineBlock] §7Enregistrement des tâches...");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::clockSendPlayerConnectedToRedis, 0, 20 * 3);
        clockForBuildMode();
        setupEnchantingTable();
        optimizeSpawn();

        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.setSpawnLocation(SPAWN);
        }

        console.sendMessage("§b[MineBlock] §aDémarrage du plugin MineBlock terminé en " + (System.currentTimeMillis() - startTime) + "ms");
    }

    public void setupEnchantingTable() {
        World w = Bukkit.getWorld(SPAWN_WORLD_NAME);
        if (w == null) return;
        ENCHANTING_TABLE_LOCATION = new Location(w, 0, 318, 0);
        /*Location loc = SPAWN.clone();
        loc.setY(loc.getWorld().getMaxHeight() - 2);
        Block block = loc.getBlock();
        block.setType(Material.ENCHANTING_TABLE);
        ENCHANTING_TABLE_LOCATION = block.getLocation();
        for (int x = -2; x <= 2; x++) {
            //if (x == 0 || x == 1 || x == -1) continue;
            for (int z = -2; z <= 2; z++) {
                if (z == 0 || z == 1 || z == -1) continue;
                for (int y = 0; y <= 2; y++) {
                    Block b = block.getRelative(x, y, z);
                    if (b.getType().isAir()) {
                        b.setType(Material.BOOKSHELF);
                    }
                }
            }
        }*/
    }

    @Override
    public void onDisable() {
        console.sendMessage("§6Arrêt du plugin MineBlock");

        WorldsManager.INSTANCE.unload(SPAWN_WORLD_NAME, !SPAWN_IN_READ_ONLY);
        WorldsManager.INSTANCE.unload("island_template_1", false);

        IslandsManager.INSTANCE.onDisable();

        DatabaseManager.INSTANCE.closeConnection();

        JedisManager.INSTANCE.onDisable();

        console.sendMessage("§aArrêt du plugin MineBlock terminé");
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
        w.setGameRule(GameRule.KEEP_INVENTORY, true);
    }

    public void clockSendPlayerConnectedToRedis() {
        ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        StringBuilder sb = new StringBuilder();
        for (Player player : players) {
            sb.append(player.getUniqueId()).append(":").append(player.getName()).append(",");
        }
        if (!sb.isEmpty()) sb.deleteCharAt(sb.length() - 1);
        //FIXME: redis
        //JedisManager.INSTANCE.sendToRedis("MineBlock:players:" + SERVER_NAME, sb.toString());
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
