package fr.farmeurimmo.coreskyblock.purpur.islands.upgrades;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class IslandsBlocksLimiterManager {

    public static IslandsBlocksLimiterManager INSTANCE;
    public static Map<UUID, Map<Material, Integer>> blocksPlaced = new HashMap<>();
    public final ArrayList<UUID> inCalculating = new ArrayList<>();
    public final ArrayList<UUID> lastWarningTriggered = new ArrayList<>();

    public IslandsBlocksLimiterManager() {
        INSTANCE = this;
    }

    public boolean hasReachedLimit(Material material, Island island) {
        if (inCalculating.contains(island.getIslandUUID())) return true;
        Map<Material, Integer> blocks = blocksPlaced.get(island.getIslandUUID());
        if (blocks == null) return false;
        if (blocks.getOrDefault(material, 0) >= getLimit(material, 0)) {
            return true;
        } else {
            blocks.put(material, blocks.get(material) + 1);
            return false;
        }
    }

    public int getLimit(Material material, int level) {
        return switch (material) {
            case CHEST:
                yield switch (level) {
                    case 1 -> 15;
                    case 2 -> 20;
                    default -> 10;
                };
            case HOPPER:
                yield 2;
            default:
                yield 0;
        };
    }

    public void initializeIsland(Island island) {
        if (island == null) return;

        inCalculating.add(island.getIslandUUID());

        final Set<ChunkSnapshot> chunks = IslandsManager.INSTANCE.getSnapshots(island);

        final int minY = IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID()).getMinHeight();

        final Map<Material, Integer> values = new HashMap<>();
        values.put(Material.CHEST, 0);
        values.put(Material.HOPPER, 0);

        CompletableFuture.supplyAsync(() -> {
            if (chunks.isEmpty()) return 0.0;

            float level = 0;
            for (ChunkSnapshot chunk : chunks) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = minY; y < chunk.getHighestBlockYAt(x, z) + 1; y++) {
                            Material material = chunk.getBlockType(x, y, z);

                            if (material == Material.AIR) continue;
                            if (values.containsKey(material)) {
                                values.put(material, values.get(material) + 1);
                            } else {
                                values.put(material, 1);
                            }
                        }
                    }
                }
            }
            return level;
        }).thenAccept(level -> Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
            blocksPlaced.put(island.getIslandUUID(), values);

            inCalculating.remove(island.getIslandUUID());
            return null;
        }));
    }

    public void remove(Material material, Island island) {
        Map<Material, Integer> blocks = blocksPlaced.get(island.getIslandUUID());
        if (blocks == null) return;
        if (blocks.getOrDefault(material, 0) > 0) {
            blocks.put(material, blocks.get(material) - 1);
        }
    }

}
