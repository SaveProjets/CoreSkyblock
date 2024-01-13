package fr.farmeurimmo.skylyblock.purpur.core;

import com.grinderwolf.swm.api.SlimePlugin;
import fr.farmeurimmo.skylyblock.common.JedisManager;
import fr.farmeurimmo.skylyblock.common.ServerType;
import fr.farmeurimmo.skylyblock.common.SkyblockUsersManager;
import fr.farmeurimmo.skylyblock.purpur.core.chests.ChestsCmd;
import fr.farmeurimmo.skylyblock.purpur.core.chests.ChestsListener;
import fr.farmeurimmo.skylyblock.purpur.core.chests.ChestsManager;
import fr.farmeurimmo.skylyblock.purpur.core.cmds.IslandCmd;
import fr.farmeurimmo.skylyblock.purpur.core.eco.MoneyCmd;
import fr.farmeurimmo.skylyblock.purpur.core.events.ChatReactionManager;
import fr.farmeurimmo.skylyblock.purpur.core.featherfly.FeatherFlyCmd;
import fr.farmeurimmo.skylyblock.purpur.core.featherfly.FeatherFlyListener;
import fr.farmeurimmo.skylyblock.purpur.core.featherfly.FeatherFlyManager;
import fr.farmeurimmo.skylyblock.purpur.core.listeners.ChatListener;
import fr.farmeurimmo.skylyblock.purpur.core.listeners.ChatReactionListener;
import fr.farmeurimmo.skylyblock.purpur.core.listeners.PlayerListener;
import fr.farmeurimmo.skylyblock.purpur.core.minions.MinionsCmd;
import fr.farmeurimmo.skylyblock.purpur.core.minions.MinionsListener;
import fr.farmeurimmo.skylyblock.purpur.core.minions.MinionsManager;
import fr.farmeurimmo.skylyblock.purpur.core.scoreboard.ScoreboardManager;
import fr.farmeurimmo.skylyblock.purpur.core.worlds.WorldManager;
import fr.farmeurimmo.skylyblock.purpur.hub.HubManager;
import fr.farmeurimmo.skylyblock.purpur.island.IslandsManager;
import fr.farmeurimmo.skylyblock.purpur.utils.InventorySyncUtils;
import fr.mrmicky.fastinv.FastInvManager;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class SkylyBlock extends JavaPlugin {

    public static SkylyBlock INSTANCE;
    public ConsoleCommandSender console;
    public SlimePlugin slimePlugin;
    private ServerType serverType;
    private String serverName;

    @Override
    public void onLoad() {
        INSTANCE = this;
        console = getServer().getConsoleSender();
        console.sendMessage("§b[SkylyBlock] §7Chargement des mondes...");
        slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        console.sendMessage("§b[SkylyBlock] §7Démarrage du plugin SkylyBlock...");
        console.sendMessage("§b[SkylyBlock] §7Démarrage de la partie commune...");

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des dépendances...");
        FastInvManager.register(INSTANCE);
        new InventorySyncUtils();

        console.sendMessage("§b[SkylyBlock] §7Démarrage des managers commun...");
        new WorldManager();
        new SkyblockUsersManager();
        new ScoreboardManager();

        new FeatherFlyManager();
        new ChatReactionManager();
        new ChestsManager();
        new MinionsManager();

        console.sendMessage("§b[SkylyBlock] §7Détection du type de serveur...");
        String serverTypeFromServerName = Bukkit.getServerName();

        serverType = ServerType.isSimilar(serverTypeFromServerName);
        if (serverType == null) serverType = ServerType.getDefault();

        console.sendMessage("§b[SkylyBlock] §7Connexion à redis...");
        new JedisManager(serverType);

        console.sendMessage("§b[SkylyBlock] §7Type de serveur détecté: " + serverType.getServerName());
        serverName = serverType.getServerName();
        if (serverType == ServerType.SKYBLOCK_SPAWN) {
            new HubManager(INSTANCE);
        } else if (serverType == ServerType.SKYBLOCK_ISLAND) {
            new IslandsManager(INSTANCE);
        }

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des listeners commun...");
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new FeatherFlyListener(), this);
        getServer().getPluginManager().registerEvents(new ChatReactionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new ChestsListener(), this);
        getServer().getPluginManager().registerEvents(new MinionsListener(), this);

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des commandes communes...");
        getCommand("featherfly").setExecutor(new FeatherFlyCmd());
        getCommand("money").setExecutor(new MoneyCmd());
        getCommand("is").setExecutor(new IslandCmd());
        getCommand("chests").setExecutor(new ChestsCmd());
        getCommand("minions").setExecutor(new MinionsCmd());

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des tâches communes...");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::clockSendPlayerConnectedToRedis, 0, 20 * 3);

        console.sendMessage("§b[SkylyBlock] §aDémarrage du plugin SkylyBlock terminé en " + (System.currentTimeMillis() - startTime) + "ms");
    }

    @Override
    public void onDisable() {
        console.sendMessage("§6Arrêt du plugin SkylyBlock");

        if (serverType == ServerType.SKYBLOCK_SPAWN) {
            HubManager.INSTANCE.disable();
        } else if (serverType == ServerType.SKYBLOCK_ISLAND) {
            IslandsManager.INSTANCE.disable();
        }

        try {
            Bukkit.unloadWorld("hub", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JedisManager.INSTANCE.onDisable();

        console.sendMessage("§aArrêt du plugin SkylyBlock terminé");
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void clockSendPlayerConnectedToRedis() {
        ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        StringBuilder sb = new StringBuilder();
        for (Player player : players) {
            sb.append(player.getUniqueId()).append(":").append(player.getName()).append(",");
        }
        if (!sb.isEmpty()) sb.deleteCharAt(sb.length() - 1);
        JedisManager.INSTANCE.sendToRedis("skylyblock:players:" + serverName, sb.toString());
    }
}
