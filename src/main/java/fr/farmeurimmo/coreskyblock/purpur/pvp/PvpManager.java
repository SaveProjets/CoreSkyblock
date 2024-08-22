package fr.farmeurimmo.coreskyblock.purpur.pvp;

import fr.farmeurimmo.coreskyblock.purpur.pvp.listeners.PvpListener;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpManager {

    public static PvpManager INSTANCE;

    public PvpManager(JavaPlugin plugin) {
        INSTANCE = this;

        plugin.getServer().getPluginManager().registerEvents(new PvpListener(), plugin);
    }
}
