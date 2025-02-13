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
        if (!blocks.containsKey(material)) return false;
        if (blocks.getOrDefault(material, 0) >= getLimit(material, 0)) {
            return true;
        } else {
            blocks.put(material, blocks.get(material) + 1);
            return false;
        }
    }

    public int getLimit(Material material, int level) {
        return switch (material) {
            case HOPPER:
                yield switch (level) {
                    case 1 -> 20;
                    case 2 -> 40;
                    case 3 -> 80;
                    case 4 -> 120;
                    case 5 -> 160;
                    default -> 0;
                };
            case SPAWNER:
                yield 50 + (level - 1);
            default:
                yield 0;
        };
    }

    public int getPrice(Material material, int level) {
        return switch (material) {
            case HOPPER:
                yield switch (level) {
                    case 2 -> 200;
                    case 3 -> 500;
                    case 4 -> 1000;
                    case 5 -> 2000;
                    default -> 0;
                };
            case SPAWNER:
                yield 100;
            default:
                yield 0;
        };
    }

    public ArrayList<String> getLores(Material material, int level) {
        List<String> lore = new java.util.ArrayList<>();
        return switch (material) {
            case HOPPER:
                for (int i = 1; i <= 5; i++) {
                    lore.add("§7" + i + ": §6" + getLimit(material, i) + " blocs §8| " + (level >= i ? "§aDéjà achetée" :
                            "§7Prix: §e" + getPrice(material, i) + "§6§lexp"));
                }
                yield new ArrayList<>(lore);
            case SPAWNER:
                lore.add("§7Limite actuelle: §6" + getLimit(material, level) + " spawneurs");
                lore.add("");
                lore.add("§7Prix: §e" + getPrice(material, level) + "§6§lexp");
                lore.add("");
                lore.add("§7Chaque niveau augmente la limite de §e§l1§7.");
                lore.add("§7Le prix n'augmente pas avec le niveau.");
                yield new ArrayList<>(lore);
            default:
                yield new ArrayList<>();
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
        values.put(Material.SPAWNER, 0);

        blocksPlaced.put(island.getIslandUUID(), values);

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
