package fr.farmeurimmo.coreskyblock.purpur.islands.levels;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsSizeManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandsLevelCalculator {

    public static IslandsLevelCalculator INSTANCE;

    public IslandsLevelCalculator() {
        INSTANCE = this;
    }

    public void calculateIslandLevel(Island island, UUID uuid) {
        double minX = -IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());
        double minZ = -IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());
        double maxX = IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());
        double maxZ = IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());

        World world = IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID());

        Set<ChunkSnapshot> chunks = new HashSet<>();

        long start = System.currentTimeMillis();
        for (int x = (int) minX; x < (maxX + 16); x += 16) {
            for (int z = (int) minZ; z < (maxZ + 16); z += 16) {
                Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                if (!chunk.isLoaded()) {
                    if (chunk.load(false)) {
                        chunks.add(chunk.getChunkSnapshot(true, false, false));
                        chunk.unload();
                    }
                } else {
                    chunks.add(chunk.getChunkSnapshot(true, false, false));
                }
            }
        }

        System.out.println("Time to get snapshots: " + (System.currentTimeMillis() - start) + "ms");

        int minY = world.getMinHeight();
        Map<Material, Float> values = IslandsBlocksValues.INSTANCE.getBlocksValues();

        long finalStart = System.currentTimeMillis();
        CompletableFuture.supplyAsync(() -> {
            float level = 0;
            for (ChunkSnapshot chunk : chunks) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = minY; y < chunk.getHighestBlockYAt(x, z) + 1; y++) {
                            Material material = chunk.getBlockType(x, y, z);

                            if (material == Material.AIR) continue;
                            if (values.containsKey(material)) {
                                level += values.get(material);
                            }
                        }
                    }
                }
            }
            return level;
        }).thenAccept(level -> Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
            island.setLevel(level);
            System.out.println("Time to calculate level --> " + (System.currentTimeMillis() - finalStart) + "ms");
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(Component.text("§aNouveau niveau de l'île: §6" +
                        NumberFormat.getInstance().format(level) + "."));
            }
            return null;
        }));
    }
}
