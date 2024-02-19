package fr.farmeurimmo.mineblock.purpur.islands;

import fr.farmeurimmo.mineblock.common.islands.Island;
import fr.farmeurimmo.mineblock.purpur.MineBlock;
import fr.farmeurimmo.mineblock.purpur.islands.upgrades.IslandsSizeManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
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
        int minX = -IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());
        int minZ = -IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());
        int maxX = IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());
        int maxZ = IslandsSizeManager.INSTANCE.getSizeFromLevel(island.getMaxSize());

        World world = IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID());

        Set<ChunkSnapshot> chunks = new HashSet<>();

        long start = System.currentTimeMillis();
        for (int x = minX; x < (maxX + 16); x += 16) {
            for (int z = minZ; z < (maxZ + 16); z += 16) {
                if (!world.getBlockAt(x, 0, z).getChunk().isLoaded()) {
                    world.getBlockAt(x, 0, z).getChunk().load();
                    chunks.add(world.getBlockAt(x, 0, z).getChunk().getChunkSnapshot(false, false, false));
                    world.getBlockAt(x, 0, z).getChunk().unload();
                } else {
                    chunks.add(world.getBlockAt(x, 0, z).getChunk().getChunkSnapshot());
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
        }).thenAccept(level -> Bukkit.getScheduler().callSyncMethod(MineBlock.INSTANCE, () -> {
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
