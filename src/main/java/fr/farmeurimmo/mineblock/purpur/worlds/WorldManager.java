package fr.farmeurimmo.mineblock.purpur.worlds;

import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import fr.farmeurimmo.mineblock.purpur.MineBlock;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;

public class WorldManager {

    public static WorldManager INSTANCE;

    SlimePropertyMap properties = new SlimePropertyMap();
    SlimeLoader loader;

    public WorldManager() {
        INSTANCE = this;

        properties.setValue(SlimeProperties.PVP, false);
        properties.setValue(SlimeProperties.ALLOW_ANIMALS, false);
        properties.setValue(SlimeProperties.ALLOW_MONSTERS, false);
        properties.setValue(SlimeProperties.DEFAULT_BIOME, "plains");

        loader = MineBlock.INSTANCE.slimePlugin.getLoader("mysql");
    }

    public void loadOrCreate(String name, boolean readOnly) {
        try {
            if (loader.worldExists(name)) {
                load(name, readOnly);
            } else {
                MineBlock.INSTANCE.console.sendMessage("§b[MineBlock] §aCréation du monde...");

                try {
                    //the boolean is for loading the world in read-only mode
                    SlimeWorld world = MineBlock.INSTANCE.slimePlugin.createEmptyWorld(loader, name, readOnly, properties);

                    MineBlock.INSTANCE.slimePlugin.generateWorld(world);
                    MineBlock.INSTANCE.console.sendMessage("§b[MineBlock] §aMonde " + name + " créé");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<SlimeWorld> loadAsync(String name, boolean readOnly) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                //the boolean is for loading the world in read-only mode
                SlimeWorld world = MineBlock.INSTANCE.slimePlugin.loadWorld(loader, name, readOnly, properties);
                MineBlock.INSTANCE.console.sendMessage("§b[MineBlock] §aMonde " + name + " chargé");

                MineBlock.INSTANCE.slimePlugin.generateWorld(world);
                return world;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public SlimeWorld cloneAndLoad(String name, String cloneFrom) {
        try {
            //the boolean is for loading the world in read-only mode
            if (!loader.worldExists(cloneFrom)) return null;
            SlimeWorld worldToClone = MineBlock.INSTANCE.slimePlugin.loadWorld(loader, cloneFrom, true, properties);
            MineBlock.INSTANCE.slimePlugin.generateWorld(worldToClone);

            if (worldToClone == null) return null;
            SlimeWorld world = worldToClone.clone(name, loader, true);
            MineBlock.INSTANCE.console.sendMessage("§b[MineBlock] §aMonde " + name + " chargé");

            MineBlock.INSTANCE.slimePlugin.generateWorld(world);
            return world;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void load(String name, boolean readOnly) {
        try {
            //the boolean is for loading the world in read-only mode
            SlimeWorld world = MineBlock.INSTANCE.slimePlugin.loadWorld(loader, name, readOnly, properties);
            MineBlock.INSTANCE.console.sendMessage("§b[MineBlock] §aMonde " + name + " chargé");

            MineBlock.INSTANCE.slimePlugin.generateWorld(world);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unload(String name, boolean write) {
        Bukkit.unloadWorld(name, write);
    }
}
