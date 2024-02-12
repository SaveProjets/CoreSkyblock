package fr.farmeurimmo.mineblock.purpur.worlds;

import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import fr.farmeurimmo.mineblock.purpur.MineBlock;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;

public class WorldsManager {

    public static WorldsManager INSTANCE;

    SlimePropertyMap properties = new SlimePropertyMap();
    SlimeLoader loader;

    public WorldsManager() {
        INSTANCE = this;

        properties.setValue(SlimeProperties.PVP, false);
        properties.setValue(SlimeProperties.ALLOW_ANIMALS, false);
        properties.setValue(SlimeProperties.ALLOW_MONSTERS, false);
        properties.setValue(SlimeProperties.DEFAULT_BIOME, "plains");

        loader = MineBlock.INSTANCE.slimePlugin.getLoader("mysql");
    }

    public void loadOrCreate(String name, boolean readOnly) { //only used for the spawn world
        try {
            if (loader.worldExists(name)) {
                loadAsync(name, readOnly);
            } else {
                try {
                    //the boolean is for loading the world in read-only mode
                    SlimeWorld world = MineBlock.INSTANCE.slimePlugin.createEmptyWorld(loader, name, readOnly, properties);

                    MineBlock.INSTANCE.slimePlugin.generateWorld(world);
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
                return MineBlock.INSTANCE.slimePlugin.loadWorld(loader, name, readOnly, properties);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(world -> {
            if (world == null) return;
            Bukkit.getScheduler().callSyncMethod(MineBlock.INSTANCE, () -> {
                MineBlock.INSTANCE.slimePlugin.generateWorld(world);
                return null;
            });
        });
    }

    public SlimeWorld cloneAndLoad(String name, String cloneFrom) {
        try {
            //the boolean is for loading the world in read-only mode
            if (!loader.worldExists(cloneFrom)) return null;
            SlimeWorld worldToClone = MineBlock.INSTANCE.slimePlugin.getWorld(cloneFrom);
            if (worldToClone == null) return null;

            SlimeWorld world = worldToClone.clone(name, loader, true);

            MineBlock.INSTANCE.slimePlugin.generateWorld(world);
            return world;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unload(String name, boolean write) {
        Bukkit.unloadWorld(name, write);
    }
}
