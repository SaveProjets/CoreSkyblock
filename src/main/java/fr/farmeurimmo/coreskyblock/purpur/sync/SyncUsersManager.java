package fr.farmeurimmo.coreskyblock.purpur.sync;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.farmeurimmo.coreskyblock.ServerType;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsEffectsManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import fr.farmeurimmo.coreskyblock.storage.sync.SyncUser;
import fr.farmeurimmo.coreskyblock.storage.sync.SyncUsersDataManager;
import fr.farmeurimmo.coreskyblock.utils.InventorySyncUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SyncUsersManager {

    public static SyncUsersManager INSTANCE;
    public final List<UUID> inSync = new ArrayList<>(); // List of players currently being synced (all of their actions are cancelled)
    private final Map<UUID, SyncUser> users = new HashMap<>();
    private final Gson gson = new Gson();
    private final boolean showMessages = false;

    public SyncUsersManager() {
        INSTANCE = this;

        new SyncUsersDataManager();

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                autoSave(p, false);
            }
        }, 0, 5 * 60 * 20);
    }

    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            autoSave(p, false);
        }
    }

    public void autoSave(Player p, boolean async) {
        try {
            SyncUser user = users.get(p.getUniqueId());
            if (user == null) {
                user = new SyncUser(p.getUniqueId(), InventorySyncUtils.INSTANCE.inventoryToBase64String(p.getInventory()),
                        p.getHealth(), p.getFoodLevel(), p.getExp(), p.getLevel(),
                        p.getActivePotionEffects().toArray(PotionEffect[]::new));
            } else {
                user.updateInventory(p.getInventory());
            }
            user.setHealth(p.getHealth());
            user.setFood(p.getFoodLevel());
            user.setExp(p.getExp());

            ArrayList<PotionEffect> effects = new ArrayList<>(p.getActivePotionEffects());
            for (PotionEffect effect : user.getPotionEffects()) {
                if (!effect.hasParticles()) {
                    effects.remove(effect);
                }
            }
            user.setPotionEffects(effects.toArray(PotionEffect[]::new));

            if (async) {
                final SyncUser finalUser = user;
                CompletableFuture.runAsync(() -> {
                    JedisManager.INSTANCE.sendToRedis("coreskyblock:sync:" + finalUser.getUuid(),
                            gson.toJson(finalUser.toJson()));

                    SyncUsersDataManager.INSTANCE.saveInventory(finalUser);
                });
            } else {
                JedisManager.INSTANCE.sendToRedis("coreskyblock:sync:" + user.getUuid(), gson.toJson(user.toJson()));
                SyncUsersDataManager.INSTANCE.saveInventory(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Triggered when a player logs out
    public void stopPlayerSyncInAsync(Player p) {
        if (inSync.contains(p.getUniqueId())) return;

        autoSave(p, true);

        Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
            users.remove(p.getUniqueId());
            return null;
        });
    }

    // Triggered when a player logs in
    public void startPlayerSync(Player p) {
        inSync.add(p.getUniqueId()); // Add the player to the list of players currently being synced and cancel all of their actions
        if (showMessages) p.sendMessage(Component.text("§cSynchronisation de votre inventaire en cours..."));
        long start = System.currentTimeMillis();

        CompletableFuture.runAsync(() -> {
            String data = JedisManager.INSTANCE.getFromRedis("coreskyblock:sync:" + p.getUniqueId());
            if (data != null) {
                JsonObject json = gson.fromJson(data, JsonObject.class);
                SyncUser user = SyncUser.fromJson(json);

                callbackAndApplyThings(user, p);
                return;
            }

            //fetch the user from the database
            SyncUser user = SyncUsersDataManager.INSTANCE.getInventory(p.getUniqueId());
            if (user != null) {
                users.put(p.getUniqueId(), user);

                callbackAndApplyThings(user, p);
                return;
            }

            //if the user is not in the database, create a new one
            user = new SyncUser(p.getUniqueId(), InventorySyncUtils.INSTANCE.inventoryToBase64String(p.getInventory()),
                    p.getHealth(), p.getFoodLevel(), p.getExp(), p.getLevel(),
                    p.getActivePotionEffects().toArray(PotionEffect[]::new));
            users.put(p.getUniqueId(), user);

            JedisManager.INSTANCE.sendToRedis("coreskyblock:sync:" + user.getUuid(), gson.toJson(user.toJson()));
        }).thenRun(() -> Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
            inSync.remove(p.getUniqueId()); // Remove the player from the list of players currently being synced
            if (showMessages)
                p.sendMessage(Component.text("§aSynchronisation terminée en " + (System.currentTimeMillis() - start) + "ms"));
            return null;
        })).exceptionally(ex -> {
            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                p.kick(Component.text("§cErreur lors de la connexion au serveur, veuillez réessayer plus tard !"));
                return null;
            });
            ex.printStackTrace();
            return null;
        });
    }

    public void callbackAndApplyThings(SyncUser user, Player p) {
        Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
            p.getInventory().clear();
            p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
            p.getInventory().setContents(user.getContentsItemStack());
            p.setHealth(user.getHealth());
            p.setFoodLevel(user.getFood());
            p.setExp(user.getExp());
            p.setLevel(user.getLevel());
            for (PotionEffect effect : user.getPotionEffects()) {
                p.addPotionEffect(effect);
            }

            if (CoreSkyblock.SERVER_TYPE == ServerType.GAME) {
                Island island = IslandsManager.INSTANCE.getIslandOf(p.getUniqueId());
                if (island != null) IslandsEffectsManager.INSTANCE.setEffects(island);
            }
            return null;
        });
    }
}
