package fr.farmeurimmo.coreskyblock.purpur.prestige;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.coreskyblock.utils.ExperienceUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrestigesManager {

    public static PrestigesManager INSTANCE;
    private final Map<UUID, String> progressBar = new HashMap<>();

    public PrestigesManager() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () -> {
            for (SkyblockUser skyblockUser : SkyblockUsersManager.INSTANCE.getCachedUsers().values()) {
                Player p = Bukkit.getPlayer(skyblockUser.getUuid());
                if (p == null) {
                    continue;
                }
                int currentExp = ExperienceUtils.getExp(p);
                if (currentExp >= 200_000) {
                    progressBar.put(p.getUniqueId(), "§a§l⬆");
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                int bars = (int) Math.ceil((double) currentExp / 20_000);
                sb.append("§a§l|".repeat(Math.max(0, bars)));
                sb.append("§c§l|".repeat(Math.max(0, 10 - bars)));
                progressBar.put(p.getUniqueId(), sb.toString());
            }
        }, 0, 20);
    }

    public String getProgressBar(UUID uuid) {
        return progressBar.getOrDefault(uuid, "§c§l|".repeat(10));
    }

    public boolean isMajorReward(int prestigeLevel) {
        return prestigeLevel % 5 == 0;
    }

    public String getColorCode(int prestigeLevel) {
        if (prestigeLevel >= 0 && prestigeLevel <= 99) {
            return "§7";
        } else if (prestigeLevel >= 100 && prestigeLevel <= 199) {
            return "§d";
        } else if (prestigeLevel >= 200 && prestigeLevel <= 299) {
            return "§5";
        } else if (prestigeLevel >= 300 && prestigeLevel <= 399) {
            return "§1";
        } else if (prestigeLevel >= 400 && prestigeLevel <= 499) {
            return "§9";
        } else if (prestigeLevel >= 500 && prestigeLevel <= 599) {
            return "§3";
        } else if (prestigeLevel >= 600 && prestigeLevel <= 699) {
            return "§b";
        } else if (prestigeLevel >= 700 && prestigeLevel <= 799) {
            return "§a";
        } else if (prestigeLevel >= 800 && prestigeLevel <= 899) {
            return "§2";
        } else if (prestigeLevel >= 900 && prestigeLevel <= 999) {
            return "§e";
        } else if (prestigeLevel >= 1000 && prestigeLevel <= 1099) {
            return "§6";
        } else if (prestigeLevel >= 1100 && prestigeLevel <= 1199) {
            return "§c";
        } else {
            return "§4";
        }
    }

    public boolean isUltraMajorReward(int prestigeLevel) {
        return prestigeLevel % 100 == 0;
    }

    public String getMajorRewardName(int prestigeLevel) {
        return isUltraMajorReward(prestigeLevel) ? getColorCode(prestigeLevel) + "Nouvelle couleur d'affichage" : "";
    }

    public void giveRewards(SkyblockUser user, int prestigeLevel) {
        Player p = Bukkit.getPlayer(user.getUuid());
        if (p == null) {
            return;
        }
        p.sendMessage(Component.text("§aVous avez réclamé le prestige " + prestigeLevel + "."));
    }
}
