package fr.farmeurimmo.skylyblock.purpur.core;

import com.grinderwolf.swm.api.SlimePlugin;
import fr.farmeurimmo.skylyblock.common.JedisManager;
import fr.farmeurimmo.skylyblock.common.ServerType;
import fr.farmeurimmo.skylyblock.common.SkyblockUserManager;
import fr.farmeurimmo.skylyblock.purpur.core.cmds.IslandCmd;
import fr.farmeurimmo.skylyblock.purpur.core.eco.MoneyCmd;
import fr.farmeurimmo.skylyblock.purpur.core.events.ChatReactionManager;
import fr.farmeurimmo.skylyblock.purpur.core.featherfly.FeatherFlyCmd;
import fr.farmeurimmo.skylyblock.purpur.core.featherfly.FeatherFlyListener;
import fr.farmeurimmo.skylyblock.purpur.core.featherfly.FeatherFlyManager;
import fr.farmeurimmo.skylyblock.purpur.core.listeners.ChatListener;
import fr.farmeurimmo.skylyblock.purpur.core.listeners.ChatReactionListener;
import fr.farmeurimmo.skylyblock.purpur.core.listeners.PlayerListener;
import fr.farmeurimmo.skylyblock.purpur.core.scoreboard.ScoreboardManager;
import fr.farmeurimmo.skylyblock.purpur.core.worlds.WorldManager;
import fr.farmeurimmo.skylyblock.purpur.hub.HubManager;
import fr.farmeurimmo.skylyblock.purpur.island.IslandManager;
import fr.mrmicky.fastinv.FastInvManager;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkylyBlock extends JavaPlugin {

    public static SkylyBlock INSTANCE;
    public ConsoleCommandSender console;
    public SlimePlugin slimePlugin;
    private ServerType serverType;

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

        console.sendMessage("§b[SkylyBlock] §7Démarrage des managers commun...");
        new WorldManager();
        new SkyblockUserManager();
        new ScoreboardManager();
        new FeatherFlyManager();
        new ChatReactionManager();

        console.sendMessage("§b[SkylyBlock] §7Détection du type de serveur...");
        String serverTypeFromServerName = Bukkit.getServerName();

        ServerType serverType = ServerType.isSimilar(serverTypeFromServerName);
        if (serverType == null) serverType = ServerType.getDefault();

        console.sendMessage("§b[SkylyBlock] §7Connexion à redis...");
        new JedisManager(serverType);

        console.sendMessage("§b[SkylyBlock] §7Type de serveur détecté: " + serverType.getServerName());
        if (serverType == ServerType.SKYBLOCK_SPAWN) {
            new HubManager(INSTANCE);
        } else if (serverType == ServerType.SKYBLOCK_ISLAND) {
            new IslandManager(INSTANCE);
        }

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des listeners commun...");
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new FeatherFlyListener(), this);
        getServer().getPluginManager().registerEvents(new ChatReactionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        console.sendMessage("§b[SkylyBlock] §7Enregistrement des commandes communes...");
        getServer().getPluginCommand("featherfly").setExecutor(new FeatherFlyCmd());
        getServer().getPluginCommand("money").setExecutor(new MoneyCmd());
        getServer().getPluginCommand("is").setExecutor(new IslandCmd());

        console.sendMessage("§b[SkylyBlock] §aDémarrage du plugin SkylyBlock terminé en " + (System.currentTimeMillis() - startTime) + "ms");
    }

    @Override
    public void onDisable() {
        console.sendMessage("§6Arrêt du plugin SkylyBlock");

        if (serverType == ServerType.SKYBLOCK_SPAWN) {
            HubManager.INSTANCE.disable();
        } else if (serverType == ServerType.SKYBLOCK_ISLAND) {
            IslandManager.INSTANCE.disable();
        }

        try {
            Bukkit.unloadWorld("hub", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JedisManager.INSTANCE.onDisable();

        console.sendMessage("§aArrêt du plugin SkylyBlock terminé");
    }

    public final ServerType getServerType() {
        return serverType;
    }
}
