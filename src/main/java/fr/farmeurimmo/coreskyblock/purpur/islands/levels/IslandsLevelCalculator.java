package fr.farmeurimmo.coreskyblock.purpur.islands.levels;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
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
        long start = System.currentTimeMillis();

        Set<ChunkSnapshot> chunks = IslandsManager.INSTANCE.getSnapshots(island);

        long total = System.currentTimeMillis() - start;
        if (total > 1000) {
            CoreSkyblock.INSTANCE.console.sendMessage("§c§lANORMAL §cTime to calculate level --> " + total + "ms");
        }

        int minY = IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID()).getMinHeight();
        Map<Material, Float> values = IslandsBlocksValues.INSTANCE.getBlocksValues();

        long finalStart = System.currentTimeMillis();
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
                                level += values.get(material);
                            }
                        }
                    }
                }
            }
            return level;
        }).thenAccept(level -> Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
            island.setLevel(level.floatValue());

            long finalTotal = System.currentTimeMillis() - finalStart;
            if (finalTotal > 1000) {
                CoreSkyblock.INSTANCE.console.sendMessage("§c§lANORMAL §cTime to calculate level --> " + finalTotal + "ms");
            }
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(Component.text("§aNouveau niveau de l'île: §6" +
                        NumberFormat.getInstance().format(level) + ". §a(" + finalTotal + "ms)"));
            }
            return null;
        }));
    }
}
