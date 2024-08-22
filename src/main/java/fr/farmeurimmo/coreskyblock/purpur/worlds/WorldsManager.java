package fr.farmeurimmo.coreskyblock.purpur.worlds;

import com.infernalsuite.aswm.api.loaders.SlimeLoader;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimeProperties;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import com.infernalsuite.aswm.loaders.mysql.MysqlLoader;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class WorldsManager {

    public static WorldsManager INSTANCE;

    SlimePropertyMap properties = new SlimePropertyMap();
    SlimeLoader loader;

    public WorldsManager() {
        INSTANCE = this;

        properties.setValue(SlimeProperties.PVP, false);
        properties.setValue(SlimeProperties.ALLOW_ANIMALS, true);
        properties.setValue(SlimeProperties.ALLOW_MONSTERS, true);
        properties.setValue(SlimeProperties.DEFAULT_BIOME, "plains");
        properties.setValue(SlimeProperties.SPAWN_X, 0);
        properties.setValue(SlimeProperties.SPAWN_Y, 80);
        properties.setValue(SlimeProperties.SPAWN_Z, 0);
    }

    public void createLoader(String host, int port, String user, String password, String database) {
        try {
            loader = new MysqlLoader("jdbc:mysql://" + host + ":" + port + "/" + database, host, port, database, false, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadOrCreate(String name, boolean readOnly) { //only used for the spawn world
        try {
            if (loader.worldExists(name)) {
                loadAsync(name, readOnly);
            } else {
                try {
                    //the boolean is for loading the world in read-only mode
                    SlimeWorld world = CoreSkyblock.INSTANCE.slimePlugin.createEmptyWorld(name, readOnly, properties, loader);

                    CoreSkyblock.INSTANCE.slimePlugin.loadWorld(world, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> loadAsync(String name, boolean readOnly) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                //the boolean is for loading the world in read-only mode
                return CoreSkyblock.INSTANCE.slimePlugin.readWorld(loader, name, readOnly, properties);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(world -> {
            if (world == null) return;
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                CoreSkyblock.INSTANCE.slimePlugin.loadWorld(world, true);
                return null;
            });
        });
    }

    public SlimeWorld cloneAndLoad(String name, String cloneFrom, boolean readOnly) {
        try {
            //the boolean is for loading the world in read-only mode
            if (!loader.worldExists(cloneFrom)) return null;
            SlimeWorld worldToClone = CoreSkyblock.INSTANCE.slimePlugin.readWorld(loader, cloneFrom, readOnly, properties);
            if (worldToClone == null) return null;

            SlimeWorld world = worldToClone.clone(name, loader);

            CoreSkyblock.INSTANCE.slimePlugin.loadWorld(world, true);
            return world;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unload(String name, boolean write) {
        Bukkit.unloadWorld(name, write);
    }

    public void saveWorldAsync(SlimeWorld slimeWorld) {
        try {
            CoreSkyblock.INSTANCE.slimePlugin.saveWorld(slimeWorld);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveWorldAsync(World world) {
        SlimeWorld slimeWorld = CoreSkyblock.INSTANCE.slimePlugin.getLoadedWorld(world.getName());
        if (slimeWorld == null) return;
        saveWorldAsync(slimeWorld);
    }
}
