package fr.farmeurimmo.coreskyblock.purpur.prestige;

import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.coreskyblock.utils.ExperienceUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.UUID;

public class PrestigeInv extends FastInv {

    private int prestigeLevelDisplayed;
    private long lastAction = System.currentTimeMillis();

    public PrestigeInv(UUID uuid) {
        super(54, "§0Menu de prestige");

        SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(uuid);
        if (user == null) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.closeInventory();
                p.sendMessage("§cUne erreur est survenue lors de la récupération de vos données.");
            }
            return;
        }

        prestigeLevelDisplayed = user.getCurrentPrestigeLevel();

        setItems(new int[]{18, 19, 20, 21, 22, 23, 24, 25, 26}, ItemBuilder.copyOf(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)).name(" ").build());

        setItem(47, ItemBuilder.copyOf(new ItemStack(Material.ITEM_FRAME)).name("§6Fonctionnement des prestiges")
                .lore("§7Les prestiges sont ce qui peut",
                        "§7s'apparenter à ton niveau de",
                        "§7skyblock, il te permet de",
                        "§7débloquer de nombreuses",
                        "§7récompenses et avantages").build());

        setItem(51, ItemBuilder.copyOf(new ItemStack(Material.PRISMARINE)).name("§6Tags de couleurs")
                .lore("§7Gris §7de §f0§7 à §f99§7 prestiges", "§dRose §7de §f100§7 à §f199§7 prestiges",
                        "§5Violet §7de §f200§7 à §f299§7 prestiges", "§1Bleu foncé §7de §f300§7 à §f399§7 prestiges",
                        "§9Bleu §7de §f400§7 à §f499§7 prestiges", "§3Cyan léger §7de §f500§7 à §f599§7 prestiges",
                        "§bCyan §7de §f600§7 à §f699§7 prestiges", "§aVert §7de §f700§7 à §f799§7 prestiges",
                        "§2Vert foncé §7de §f800§7 à §f899§7 prestiges", "§eJaune §7de §f900§7 à §f999§7 prestiges",
                        "§6Orange §7de §f1000§7 à §f1099§7 prestiges", "§cRouge clair §7de §f1100§7 à §f1199§7 prestiges",
                        "§4Rouge §7au delà de §f1200§7 prestiges compris").build());

        for (int i = 0; i < getInventory().getSize(); i++) {
            if (getInventory().getItem(i) == null) {
                setItem(i, ItemBuilder.copyOf(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)).name(" ").build());
            }
        }

        update(user);
    }

    private void update(SkyblockUser user) {
        if (prestigeLevelDisplayed <= 0) {
            prestigeLevelDisplayed = 1;
        }

        Player p = Bukkit.getPlayer(user.getUuid());
        int currentExpHold = 0;
        if (p != null) {
            currentExpHold = ExperienceUtils.getExp(p);
        }

        for (int i = prestigeLevelDisplayed; i < prestigeLevelDisplayed + 7; i++) {
            boolean isMajorReward = PrestigesManager.INSTANCE.isMajorReward(i);
            ItemStack prestigeItem = ItemBuilder.copyOf(new ItemStack(isMajorReward ? Material.DIAMOND_BLOCK : Material.DIAMOND))
                    .name("§6Prestige §f" + i)
                    .lore((PrestigesManager.INSTANCE.isUltraMajorReward(i) ? PrestigesManager.INSTANCE.getMajorRewardName(i) : ""),
                            "",
                            (user.getLastPrestigeLevelClaimed() >= i ? "§aDéjà réclamé" : (user.getCurrentPrestigeLevel() >= i ? "§cNon réclamé" : "§cNon atteint")))
                    .flags(ItemFlag.HIDE_ENCHANTS)
                    .build();
            if (user.getLastPrestigeLevelClaimed() < i && user.getCurrentPrestigeLevel() >= i) {
                prestigeItem.addUnsafeEnchantment(Enchantment.LUCK, 1);
            }
            int finalI = i;
            setItem(10 + (i - prestigeLevelDisplayed), prestigeItem, e -> {
                if (isInCooldown()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVeuillez patienter entre chaque action."));
                    return;
                }
                lastAction = System.currentTimeMillis();
                if (user.getLastPrestigeLevelClaimed() >= finalI) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous avez déjà réclamé cette récompense."));
                    return;
                }
                if (user.getCurrentPrestigeLevel() < finalI) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas atteint ce prestige."));
                    return;
                }
                // the player can redeem 1 or more prestiges at once
                int numberOfPrestigeToRedeem = Math.max(1, finalI - user.getLastPrestigeLevelClaimed());
                user.setLastPrestigeLevelClaimed(finalI);
                for (int j = 0; j < numberOfPrestigeToRedeem; j++) {
                    PrestigesManager.INSTANCE.giveRewards(user, finalI - j);
                }
                update(user);
            });

            ItemStack premiumPrestigeItem = ItemBuilder.copyOf(new ItemStack(isMajorReward ? Material.EMERALD_BLOCK : Material.EMERALD))
                    .name("§6Prestige §f" + i + " §6premium")
                    .lore((user.getLastPremiumPrestigeLevelClaimed() >= i ? "§aDéjà réclamé" :
                            (user.getCurrentPrestigeLevel() >= i ? "§cNon réclamé" : "§cNon atteint")))
                    .flags(ItemFlag.HIDE_ENCHANTS)
                    .build();
            if (user.getLastPremiumPrestigeLevelClaimed() <= i && user.getCurrentPrestigeLevel() >= i) {
                premiumPrestigeItem.addUnsafeEnchantment(Enchantment.LUCK, 1);
            }
            setItem(28 + (i - prestigeLevelDisplayed), premiumPrestigeItem,
                    e -> {
                        if (isInCooldown()) {
                            e.getWhoClicked().sendMessage(Component.text("§cVeuillez patienter entre chaque action."));
                            return;
                        }
                        lastAction = System.currentTimeMillis();
                        e.getWhoClicked().sendMessage(Component.text("§cPas encore implémenté."));
                    });
        }

        if (prestigeLevelDisplayed > 1) {
            setItem(45, ItemBuilder.copyOf(new ItemStack(Material.ARROW)).name("§6Vers la gauche").build(),
                    e -> {
                        prestigeLevelDisplayed--;
                        update(user);
                    });
        } else {
            setItem(45, ItemBuilder.copyOf(new ItemStack(Material.BARRIER)).name("§cDécalage impossible").build());
        }

        if (prestigeLevelDisplayed < (1200 - 6))
            setItem(53, ItemBuilder.copyOf(new ItemStack(Material.ARROW)).name("§6Vers la droite").build(),
                    e -> {
                        prestigeLevelDisplayed++;
                        update(user);
                    });
        else setItem(53, ItemBuilder.copyOf(new ItemStack(Material.BARRIER)).name("§cDécalage impossible").build());

        setItem(49, ItemBuilder.copyOf(new ItemStack(Material.EXPERIENCE_BOTTLE)).name("§6Passer au prestige suivant")
                .lore(("§7Expérience actuelle: §f" + (currentExpHold >= 200_000 ? "§a" : "§c") +
                        NumberFormat.getInstance().format(currentExpHold) +
                        "§7/" + NumberFormat.getInstance().format(200_000))).build(), e -> {
            if (isInCooldown()) {
                e.getWhoClicked().sendMessage(Component.text("§cVeuillez patienter entre chaque action."));
                return;
            }
            lastAction = System.currentTimeMillis();
            Player player = (Player) e.getWhoClicked();
            int expHoldNow = ExperienceUtils.getExp(player);
            if (expHoldNow >= 200_000) {
                user.incrementCurrentPrestigeLevel();
                ExperienceUtils.changeExp(player, -200_000);
                player.sendMessage("§aVous avez passé le prestige " + NumberFormat.getInstance().format(user.getCurrentPrestigeLevel()) +
                        ". N'oubliez pas de réclamer vos récompenses.");
                player.playSound(player.getLocation(), "entity.player.levelup", 1, 1);
                update(user);
            } else {
                player.sendMessage("§cVous n'avez pas assez d'expérience pour passer un prestige. Il vous manque " +
                        NumberFormat.getInstance().format(200_000 - expHoldNow) + " points d'expérience.");
                player.playSound(player.getLocation(), "entity.villager.no", 1, 1);
            }
        });
    }

    public boolean isInCooldown() {
        return System.currentTimeMillis() - lastAction < 750;
    }
}
