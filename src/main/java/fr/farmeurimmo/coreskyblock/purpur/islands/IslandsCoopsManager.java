package fr.farmeurimmo.coreskyblock.purpur.islands;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IslandsCoopsManager {

    private static final long COOP_TIMEOUT = 1000 * 30;
    public static IslandsCoopsManager INSTANCE;
    private final List<CoopPlayer> coops = new ArrayList<>();

    public IslandsCoopsManager() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () -> {
            for (CoopPlayer coop : coops) {
                if (System.currentTimeMillis() - coop.getLastCheckSuccessPlayer() > COOP_TIMEOUT) {
                    IslandsManager.INSTANCE.coopNoResponse(coop.getPlayer());
                }
                if (System.currentTimeMillis() - coop.getLastCheckSuccessCoop() > COOP_TIMEOUT) {
                    IslandsManager.INSTANCE.coopNoResponse(coop.getCoop());
                }
            }
            for (CoopPlayer coop : coops) {
                JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:coop_check:" + coop.getPlayer());
                JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:coop_check:" + coop.getCoop());
            }
        }, 0, 20 * 20L);
    }

    public void addCoop(UUID player, UUID coop) {
        coops.add(new CoopPlayer(player, coop));
    }

    public void removeCoop(UUID player) {
        coops.removeIf(coop -> coop.getPlayer().equals(player) || coop.getCoop().equals(player));

        IslandsManager.INSTANCE.coopNoResponse(player);
    }

    public void gotResponse(UUID player) {
        coops.stream().filter(coop -> coop.getPlayer().equals(player) || coop.getCoop().equals(player)).forEach(coop -> {
            if (coop.getPlayer().equals(player)) {
                coop.setLastCheckSuccessPlayer(System.currentTimeMillis());
            } else {
                coop.setLastCheckSuccessCoop(System.currentTimeMillis());
            }
        });
    }
}
