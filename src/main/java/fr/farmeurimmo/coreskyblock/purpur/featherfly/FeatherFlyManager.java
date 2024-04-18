package fr.farmeurimmo.coreskyblock.purpur.featherfly;

import fr.farmeurimmo.coreskyblock.ServerType;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.coreskyblock.utils.DateUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FeatherFlyManager {

    public static FeatherFlyManager INSTANCE;
    private final ArrayList<UUID> players = new ArrayList<>();

    public FeatherFlyManager() {
        INSTANCE = this;

        if (CoreSkyblock.SERVER_TYPE == ServerType.GAME)
            Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, this::clock, 0, 20);
    }

    public void clock() {
        for (SkyblockUser user : SkyblockUsersManager.INSTANCE.getCachedUsers().values()) {
            if (user == null) continue;
            if (user.getFlyTime() <= 0) continue;
            if (players.contains(user.getUuid())) continue;
            Player p = Bukkit.getPlayer(user.getUuid());
            if (p == null) continue;
            players.add(user.getUuid());
        }
        ArrayList<UUID> toRemove = new ArrayList<>();
        for (UUID uuid : players) {
            SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(uuid);
            if (user == null) continue;
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) {
                toRemove.add(uuid);
                continue;
            }
            if (p.getWorld().getName().contains("donjon")) {
                toRemove.add(uuid);
                continue;
            }
            if (!IslandsManager.INSTANCE.isAnIsland(p.getWorld())) {
                p.sendActionBar(Component.text("§aIl vous reste §e" + DateUtils.getFormattedTimeLeft(user.getFlyTime()) + " §ade fly, §2§lEN PAUSE"));
                continue;
            }
            int flyLeft = user.getFlyTime();
            if (flyLeft <= 0) {
                toRemove.add(uuid);
                p.sendActionBar(Component.text("§cFly désactivé !"));
                p.setAllowFlight(false);
                continue;
            }
            p.setAllowFlight(true);
            user.setFlyTime(flyLeft - 1);
            p.sendActionBar(Component.text("§aFly restant: §e" + DateUtils.getFormattedTimeLeft(flyLeft)));
        }
        Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
            for (UUID uuid : toRemove) {
                players.remove(uuid);
            }
            return null;
        });
    }

    public boolean giveFeatherFly(Player p, long time, boolean force) {
        ItemStack featherFly = new ItemStack(Material.FEATHER);
        ItemMeta featherFlyMeta = featherFly.getItemMeta();
        featherFlyMeta.displayName(Component.text("§6§lPlume de vol §8[§7" + time + "s§8]"));
        featherFlyMeta.lore(List.of(Component.text("§7Plûme qui vous donne la possibilité de"),
                Component.text("§7voler pendant un temps définit.")));
        featherFlyMeta.setUnbreakable(true);
        featherFlyMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        featherFly.setItemMeta(featherFlyMeta);

        if (p.getInventory().firstEmpty() == -1) {
            if (force) {
                p.getWorld().dropItem(p.getLocation(), featherFly);
                p.sendMessage(Component.text("§cErreur, votre inventaire est plein, la plume a été drop à vos pieds !"));
                return false;
            }
            return false;
        }
        p.getInventory().addItem(featherFly);
        return true;
    }

    public boolean enableFly(Player p, int time) {
        SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
        if (user == null) return false;
        if (CoreSkyblock.SERVER_TYPE != ServerType.GAME) return false;
        int flyLeft = user.getFlyTime();
        if (flyLeft + time > 86400) {
            p.sendMessage("§cErreur, vous ne pouvez pas avoir plus de 24h de fly actif !");
            return false;
        } else if (flyLeft != 0) {
            user.setFlyTime(flyLeft + time);
            p.sendMessage("§aVous pouvez voler pendant encore " + (flyLeft + time) + " secondes !");
            players.add(p.getUniqueId());
            return true;
        }
        user.setFlyTime(time);
        p.sendMessage("§aVous pouvez désormais voler pendant encore " + time + " secondes !");
        players.add(p.getUniqueId());
        return true;
    }
}
