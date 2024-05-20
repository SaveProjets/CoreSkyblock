package fr.farmeurimmo.coreskyblock.purpur;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.infernalsuite.aswm.api.SlimePlugin;
import fr.farmeurimmo.coreskyblock.ServerType;
import fr.farmeurimmo.coreskyblock.purpur.agriculture.AgricultureCycleManager;
import fr.farmeurimmo.coreskyblock.purpur.chat.ChatDisplayManager;
import fr.farmeurimmo.coreskyblock.purpur.chests.ChestsCmd;
import fr.farmeurimmo.coreskyblock.purpur.chests.ChestsListener;
import fr.farmeurimmo.coreskyblock.purpur.chests.ChestsManager;
import fr.farmeurimmo.coreskyblock.purpur.cmds.BuildSpawnCmd;
import fr.farmeurimmo.coreskyblock.purpur.cmds.base.*;
import fr.farmeurimmo.coreskyblock.purpur.eco.MoneyCmd;
import fr.farmeurimmo.coreskyblock.purpur.events.ChatReactionManager;
import fr.farmeurimmo.coreskyblock.purpur.featherfly.FeatherFlyCmd;
import fr.farmeurimmo.coreskyblock.purpur.featherfly.FeatherFlyListener;
import fr.farmeurimmo.coreskyblock.purpur.featherfly.FeatherFlyManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.cmds.IslandCmd;
import fr.farmeurimmo.coreskyblock.purpur.listeners.ChatListener;
import fr.farmeurimmo.coreskyblock.purpur.listeners.ChatReactionListener;
import fr.farmeurimmo.coreskyblock.purpur.listeners.PlayerListener;
import fr.farmeurimmo.coreskyblock.purpur.listeners.SpawnProtectionListener;
import fr.farmeurimmo.coreskyblock.purpur.minions.MinionsCmd;
import fr.farmeurimmo.coreskyblock.purpur.minions.MinionsListener;
import fr.farmeurimmo.coreskyblock.purpur.minions.MinionsManager;
import fr.farmeurimmo.coreskyblock.purpur.prestige.PrestigeCmd;
import fr.farmeurimmo.coreskyblock.purpur.prestige.PrestigesManager;
import fr.farmeurimmo.coreskyblock.purpur.scoreboard.ScoreboardManager;
import fr.farmeurimmo.coreskyblock.purpur.shop.ShopsManager;
import fr.farmeurimmo.coreskyblock.purpur.shop.cmds.SellAllCmd;
import fr.farmeurimmo.coreskyblock.purpur.shop.cmds.ShopCmd;
import fr.farmeurimmo.coreskyblock.purpur.silos.SiloCmd;
import fr.farmeurimmo.coreskyblock.purpur.silos.SilosListener;
import fr.farmeurimmo.coreskyblock.purpur.silos.SilosManager;
import fr.farmeurimmo.coreskyblock.purpur.sync.SyncUsersManager;
import fr.farmeurimmo.coreskyblock.purpur.tpa.TpasManager;
import fr.farmeurimmo.coreskyblock.purpur.tpa.cmds.TpaCmd;
import fr.farmeurimmo.coreskyblock.purpur.tpa.cmds.TpaHereCmd;
import fr.farmeurimmo.coreskyblock.purpur.tpa.cmds.TpaNoCmd;
import fr.farmeurimmo.coreskyblock.purpur.tpa.cmds.TpaYesCmd;
import fr.farmeurimmo.coreskyblock.purpur.trade.*;
import fr.farmeurimmo.coreskyblock.purpur.worlds.WorldsManager;
import fr.farmeurimmo.coreskyblock.storage.DatabaseManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.coreskyblock.utils.InventorySyncUtils;
import fr.farmeurimmo.coreskyblock.utils.InventoryUtils;
import fr.mrmicky.fastinv.FastInvManager;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class CoreSkyblock extends JavaPlugin {

    public static final boolean SPAWN_IN_READ_ONLY = true;
    public static String SPAWN_WORLD_NAME = "spawn";
    public static ServerType SERVER_TYPE;
    public static CoreSkyblock INSTANCE;
    public static Location SPAWN;
    public static Location ENCHANTING_TABLE_LOCATION;
    public static String SERVER_NAME;
    public final Gson gson = new Gson();
    public final Map<String, ArrayList<Pair<UUID, String>>> skyblockPlayers = new HashMap<>();
    public ConsoleCommandSender console;
    public SlimePlugin slimePlugin;
    public ArrayList<UUID> buildModePlayers = new ArrayList<>();
    public Map<String, Integer> serversLoad = new HashMap<>();

    @Override
    public void onLoad() {
        INSTANCE = this;
        console = getServer().getConsoleSender();
        console.sendMessage("§b[CoreSkyblock] §7Récupération du plugin SlimeWorldManager...");
        slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        new WorldsManager();

        //This is now handled by the SlimeWorldManager plugin
        /*console.sendMessage("§b[CoreSkyblock] §7Chargement du spawn...");
        if (Bukkit.getWorld(SPAWN_WORLD_NAME) == null) {
            console.sendMessage("§b[CoreSkyblock] §7Création du monde spawn...");
            WorldsManager.INSTANCE.loadOrCreate(SPAWN_WORLD_NAME, SPAWN_IN_READ_ONLY);
        }*/
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        saveResource("old-shop.yml", true);
        saveResource("config.yml", false);

        String[] split = INSTANCE.getDataFolder().getAbsolutePath().split(File.separator);
        SERVER_NAME = split[split.length - 3];
        console.sendMessage("§bNom du serveur: §e§l" + SERVER_NAME);

        SERVER_TYPE = ServerType.getByName(SERVER_NAME);
        if (SERVER_TYPE == null) {
            console.sendMessage("§c§lErreur: Impossible de déterminer le type de serveur.");
            Bukkit.shutdown();
            return;
        }
        console.sendMessage("§bType de serveur: §e§l" + SERVER_TYPE.getName());

        console.sendMessage("§b[CoreSkyblock] §7Démarrage du plugin CoreSkyblock...");

        console.sendMessage("§b[CoreSkyblock] §7Enregistrement des dépendances...");
        FastInvManager.register(INSTANCE);
        new InventorySyncUtils();
        new InventoryUtils();

        if (SERVER_TYPE == ServerType.PVP) {
            SPAWN_WORLD_NAME = "pvp-spawn";
        } else if (SERVER_TYPE == ServerType.PVE) {
            SPAWN_WORLD_NAME = "pve-spawn";
        } else if (SERVER_TYPE == ServerType.GAME) {
            SPAWN_WORLD_NAME = "game-spawn";
        }
        SPAWN = new Location(Bukkit.getWorld(SPAWN_WORLD_NAME), 0.5, 80, 0.5, 180, 0);
        World spawnWorld = Bukkit.getWorld(SPAWN_WORLD_NAME);
        if (spawnWorld != null) {
            spawnWorld.setSpawnLocation(SPAWN);
            spawnWorld.getWorldBorder().setCenter(SPAWN);
            spawnWorld.getWorldBorder().setSize(500);
        }

        console.sendMessage("§b[CoreSkyblock] §7Connexion à la base de donnée...");
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

        console.sendMessage("§b[CoreSkyblock] §7Démarrage des managers...");
        new SkyblockUsersManager();
        new IslandsManager(INSTANCE);
        new SyncUsersManager();

        new ScoreboardManager();
        new FeatherFlyManager();
        new ChatReactionManager();
        new ChestsManager();
        new MinionsManager();
        new TradesManager();
        new TpasManager();

        new ShopsManager();
        new SilosManager();

        new AgricultureCycleManager();

        new ChatDisplayManager();
        new PrestigesManager();

        console.sendMessage("§b[CoreSkyblock] §7Connexion à redis...");
        new JedisManager();

        console.sendMessage("§b[CoreSkyblock] §7Enregistrement des listeners...");
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new FeatherFlyListener(), this);
        getServer().getPluginManager().registerEvents(new ChatReactionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new ChestsListener(), this);
        getServer().getPluginManager().registerEvents(new MinionsListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new SilosListener(), this);

        console.sendMessage("§b[CoreSkyblock] §7Enregistrement des commandes...");
        Objects.requireNonNull(getCommand("featherfly")).setExecutor(new FeatherFlyCmd());
        Objects.requireNonNull(getCommand("money")).setExecutor(new MoneyCmd());
        Objects.requireNonNull(getCommand("is")).setExecutor(new IslandCmd());
        Objects.requireNonNull(getCommand("chests")).setExecutor(new ChestsCmd());
        Objects.requireNonNull(getCommand("minions")).setExecutor(new MinionsCmd());
        Objects.requireNonNull(getCommand("trade")).setExecutor(new TradeCmd());
        Objects.requireNonNull(getCommand("tradeaccept")).setExecutor(new TradeAcceptCmd());
        Objects.requireNonNull(getCommand("tradedeny")).setExecutor(new TradeDenyCmd());
        Objects.requireNonNull(getCommand("tradecancel")).setExecutor(new TradeCancelCmd());
        Objects.requireNonNull(getCommand("buildspawn")).setExecutor(new BuildSpawnCmd());
        Objects.requireNonNull(getCommand("spawn")).setExecutor(new SpawnCmd());
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCmd());
        Objects.requireNonNull(getCommand("sellall")).setExecutor(new SellAllCmd());
        Objects.requireNonNull(getCommand("trash")).setExecutor(new TrashCmd());
        Objects.requireNonNull(getCommand("craft")).setExecutor(new CraftCmd());
        Objects.requireNonNull(getCommand("enchantement")).setExecutor(new EnchantementCmd());
        Objects.requireNonNull(getCommand("anvil")).setExecutor(new AnvilCmd());
        Objects.requireNonNull(getCommand("furnace")).setExecutor(new FurnaceCmd());
        Objects.requireNonNull(getCommand("silo")).setExecutor(new SiloCmd());
        Objects.requireNonNull(getCommand("feed")).setExecutor(new FeedCmd());
        Objects.requireNonNull(getCommand("near")).setExecutor(new NearCmd());
        Objects.requireNonNull(getCommand("fix")).setExecutor(new FixCmd());
        Objects.requireNonNull(getCommand("kits")).setExecutor(new KitsCmd());
        Objects.requireNonNull(getCommand("tpa")).setExecutor(new TpaCmd());
        Objects.requireNonNull(getCommand("tpahere")).setExecutor(new TpaHereCmd());
        Objects.requireNonNull(getCommand("tpayes")).setExecutor(new TpaYesCmd());
        Objects.requireNonNull(getCommand("tpano")).setExecutor(new TpaNoCmd());
        Objects.requireNonNull(getCommand("xp")).setExecutor(new XpCmd());
        Objects.requireNonNull(getCommand("prestige")).setExecutor(new PrestigeCmd());
        Objects.requireNonNull(getCommand("baltop")).setExecutor(new BaltopCmd());

        console.sendMessage("§b[CoreSkyblock] §7Enregistrement des canaux BungeeCord...");
        getServer().getMessenger().registerOutgoingPluginChannel(INSTANCE, "BungeeCord");

        console.sendMessage("§b[CoreSkyblock] §7Enregistrement des tâches...");
        CompletableFuture.runAsync(this::clockSendPlayerConnectedToRedis);
        clockForBuildMode();
        if (SERVER_TYPE == ServerType.SPAWN) {
            setupEnchantingTable();

            serversLoad.put(SERVER_NAME, getSpawnServerLoad());

            Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () ->
                    JedisManager.INSTANCE.publishToRedis("coreskyblock", "spawn:space:" +
                            CoreSkyblock.SERVER_NAME + ":" + getSpawnServerLoad()), 0, 20);

            Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, () -> serversLoad.put(SERVER_NAME,
                    getSpawnServerLoad()), 0, 20);

            World world = Bukkit.getWorld(SPAWN_WORLD_NAME);
            if (world != null) {
                world.setSpawnLocation(SPAWN);

                console.sendMessage("§b[CoreSkyblock] §7Forçage des chunks du spawn...");
                long startTime2 = System.currentTimeMillis();
                for (int x = -9; x <= 9; x++) {
                    for (int z = -9; z <= 9; z++) {
                        world.getChunkAt(x, z).setForceLoaded(true);
                    }
                }
                console.sendMessage("§b[CoreSkyblock] §7Forçage des chunks terminé en " + (System.currentTimeMillis() - startTime2) + "ms");
            }
        }
        optimizeSpawn();

        console.sendMessage("§b[CoreSkyblock] §aDémarrage du plugin CoreSkyblock terminé en " + (System.currentTimeMillis() - startTime) + "ms");
    }

    public void setupEnchantingTable() {
        World w = Bukkit.getWorld(SPAWN_WORLD_NAME);
        if (w == null) return;
        ENCHANTING_TABLE_LOCATION = new Location(w, 0, 318, 0);
    }

    @Override
    public void onDisable() {
        console.sendMessage("§6Arrêt du plugin CoreSkyblock");

        SyncUsersManager.INSTANCE.onDisable();

        WorldsManager.INSTANCE.unload(SPAWN_WORLD_NAME, !SPAWN_IN_READ_ONLY);

        if (CoreSkyblock.SERVER_TYPE == ServerType.GAME) {
            IslandsManager.INSTANCE.onDisable();

            WorldsManager.INSTANCE.unload("island_template_1", false);
        }

        DatabaseManager.INSTANCE.closeConnection();

        JedisManager.INSTANCE.onDisable();

        console.sendMessage("§aArrêt du plugin CoreSkyblock terminé");
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
        ArrayList<Pair<UUID, String>> playerList = new ArrayList<>();
        for (Player player : players) {
            sb.append(player.getUniqueId()).append(";").append(player.getName()).append(",");
            playerList.add(Pair.of(player.getUniqueId(), player.getName()));
        }
        if (!sb.isEmpty()) sb.deleteCharAt(sb.length() - 1);
        skyblockPlayers.put(SERVER_NAME, playerList);
        JedisManager.INSTANCE.publishToRedis("coreskyblock", "player_list:" + SERVER_NAME + ":" + sb);
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

    public ArrayList<String> getPlayersConnected() {
        ArrayList<String> players = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Pair<UUID, String>>> entry : CoreSkyblock.INSTANCE.skyblockPlayers.entrySet()) {
            for (Pair<UUID, String> pair : entry.getValue()) {
                players.add(pair.right());
            }
        }
        return players;
    }

    public ArrayList<String> getSuggestions(String name, String sender) {
        ArrayList<String> suggestions = new ArrayList<>();
        for (String player : getPlayersConnected()) {
            if (player.toLowerCase().startsWith(name.toLowerCase()) && !player.equalsIgnoreCase(sender)) {
                suggestions.add(player);
            }
        }
        return suggestions;
    }

    public boolean isPlayerConnected(String name) {
        return getPlayersConnected().contains(name);
    }

    public Pair<UUID, String> getPlayerFromName(String name) {
        for (Map.Entry<String, ArrayList<Pair<UUID, String>>> entry : CoreSkyblock.INSTANCE.skyblockPlayers.entrySet()) {
            for (Pair<UUID, String> pair : entry.getValue()) {
                if (pair.right().equalsIgnoreCase(name)) {
                    return pair;
                }
            }
        }
        return null;
    }

    public String getServerNameWherePlayerIsConnected(UUID uuid) {
        for (Map.Entry<String, ArrayList<Pair<UUID, String>>> entry : CoreSkyblock.INSTANCE.skyblockPlayers.entrySet()) {
            for (Pair<UUID, String> pair : entry.getValue()) {
                if (pair.left().equals(uuid)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private int getSpawnServerLoad() {
        return Bukkit.getOnlinePlayers().size();
    }

    public String getASpawnServer() {
        int min = Integer.MAX_VALUE;
        String server = null;
        for (Map.Entry<String, Integer> entry : serversLoad.entrySet()) {
            if (entry.getValue() < min) {
                min = entry.getValue();
                server = entry.getKey();
            }
        }
        return server;
    }

    public void sendToServer(Player player, String server) {
        player.sendMessage(Component.text("§aTéléportation au serveur §6" + server + "§a..."));

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(INSTANCE, "BungeeCord", out.toByteArray());
    }
}
