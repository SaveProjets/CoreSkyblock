package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.*;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;

public class IslandUpgradesInv extends FastInv {

    private boolean closed = false;

    public IslandUpgradesInv(Island island, Player p) {
        super(54, "§8Améliorations de l'île");

        if (island == null) {
            return;
        }

        update(island, p);

        CommonItemStacks.applyCommonPanes(Material.RED_STAINED_GLASS_PANE, getInventory());

        setItem(49, CommonItemStacks.getCommonBack(), e -> new IslandInv(island).open((Player) e.getWhoClicked()));

        setCloseFilter(player -> {
            closed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
            if (closed) {
                task.cancel();
                return;
            }
            update(island, p);
        }, 0, 40L);
    }

    private void update(Island island, Player p) {
        if (island == null) {
            return;
        }

        setItem(10, ItemBuilder.copyOf(new ItemStack(Material.GRASS_BLOCK)).name("§6Taille de l'île")
                .lore(IslandsSizeManager.INSTANCE.getLore(island.getMaxSize())).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevelSize = island.getMaxSize();
            if (currentLevelSize < 5) {
                double price = IslandsSizeManager.INSTANCE.getSizePriceFromLevel(currentLevelSize + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer la taille ! "
                            + "Il manque §6" + NumberFormat.getInstance().format(price - island.getExp()) + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setMaxSize(currentLevelSize + 1);
                update(island, p);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(11, ItemBuilder.copyOf(new ItemStack(Material.COBBLESTONE)).name("§6Générateur de l'île")
                .lore(IslandsGeneratorManager.INSTANCE.getLore(island.getGeneratorLevel())).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = island.getGeneratorLevel();
            if (currentLevel < 5) {
                double price = IslandsGeneratorManager.INSTANCE.getGeneratorPriceFromLevel(currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer le " +
                            "générateur ! Il manque §6" + NumberFormat.getInstance().format(price - island.getExp())
                            + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setGeneratorLevel(currentLevel + 1);
                update(island, p);
            } else {
                p.sendMessage(Component.text("§cVotre générateur est déjà au niveau maximum !"));
            }
        });

        setItem(13, ItemBuilder.copyOf(new ItemStack(Material.BEACON)).name("§6Membres de l'île")
                .lore(IslandsMaxMembersManager.INSTANCE.getLore(island.getMaxMembers())).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = island.getMaxMembers();
            if (currentLevel < 5) {
                double price = IslandsMaxMembersManager.INSTANCE.getMembersPriceFromLevel(currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer le nombre " +
                            "de membres ! Il manque §6" + NumberFormat.getInstance().format(price - island.getExp())
                            + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setMaxMembers(currentLevel + 1);
                update(island, p);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(15, ItemBuilder.copyOf(new ItemStack(Material.HOPPER)).name("§6Hoppeurs")
                .lore(IslandsBlocksLimiterManager.INSTANCE.getLores(Material.HOPPER, island.getHopperLevel())).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = island.getHopperLevel();
            if (currentLevel < 5) {
                double price = IslandsBlocksLimiterManager.INSTANCE.getPrice(Material.HOPPER, currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer le " +
                            "nombre de hoppeurs ! Il manque §6" + NumberFormat.getInstance().format(price - island.getExp())
                            + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setHopperLevel(currentLevel + 1);
                update(island, p);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(16, ItemBuilder.copyOf(new ItemStack(Material.SPAWNER)).name("§6Spawneurs")
                .lore(IslandsBlocksLimiterManager.INSTANCE.getLores(Material.SPAWNER, island.getSpawnerLevel()))
                .flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = island.getSpawnerLevel();
            double price = IslandsBlocksLimiterManager.INSTANCE.getPrice(Material.SPAWNER, currentLevel + 1);
            if (island.getExp() < price) {
                p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer le " +
                        "nombre de spawneurs ! Il manque §6" + NumberFormat.getInstance().format(price - island.getExp())
                        + "§c d'expérience."));
                return;
            }
            island.setLevelExp(island.getExp() - price);
            island.setSpawnerLevel(currentLevel + 1);
            update(island, p);
        });

        setItem(20, ItemBuilder.copyOf(IslandsEffectsManager.INSTANCE.getPotionEffectItem(0))
                .name("§6Vitesse").lore(IslandsEffectsManager.INSTANCE.getLore(0, island)).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = IslandsEffectsManager.INSTANCE.getLevelForEffect(island, 0);
            if (currentLevel < 2) {
                double price = IslandsEffectsManager.INSTANCE.getPrice(0, currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer l'effet de vitesse ! "
                            + "Il manque §6" + NumberFormat.getInstance().format(price - island.getExp()) + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setLevelForEffect(0, currentLevel + 1);
                update(island, p);

                IslandsEffectsManager.INSTANCE.setEffects(island);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(21, ItemBuilder.copyOf(IslandsEffectsManager.INSTANCE.getPotionEffectItem(1))
                .name("§6Régénération").lore(IslandsEffectsManager.INSTANCE.getLore(1, island)).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = IslandsEffectsManager.INSTANCE.getLevelForEffect(island, 1);
            if (currentLevel < 2) {
                double price = IslandsEffectsManager.INSTANCE.getPrice(1, currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer l'effet de régénération ! "
                            + "Il manque §6" + NumberFormat.getInstance().format(price - island.getExp()) + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setLevelForEffect(1, currentLevel + 1);
                update(island, p);

                IslandsEffectsManager.INSTANCE.setEffects(island);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(22, ItemBuilder.copyOf(IslandsEffectsManager.INSTANCE.getPotionEffectItem(2))
                .name("§6Résistance au feu").lore(IslandsEffectsManager.INSTANCE.getLore(2, island)).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = IslandsEffectsManager.INSTANCE.getLevelForEffect(island, 2);
            if (currentLevel < 2) {
                double price = IslandsEffectsManager.INSTANCE.getPrice(2, currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer l'effet de résistance au feu ! "
                            + "Il manque §6" + NumberFormat.getInstance().format(price - island.getExp()) + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setLevelForEffect(2, currentLevel + 1);
                update(island, p);

                IslandsEffectsManager.INSTANCE.setEffects(island);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(23, ItemBuilder.copyOf(IslandsEffectsManager.INSTANCE.getPotionEffectItem(3))
                .name("§6Respiration aquatique").lore(IslandsEffectsManager.INSTANCE.getLore(3, island)).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = IslandsEffectsManager.INSTANCE.getLevelForEffect(island, 3);
            if (currentLevel < 2) {
                double price = IslandsEffectsManager.INSTANCE.getPrice(3, currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer l'effet de respiration aquatique ! "
                            + "Il manque §6" + NumberFormat.getInstance().format(price - island.getExp()) + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setLevelForEffect(3, currentLevel + 1);
                update(island, p);

                IslandsEffectsManager.INSTANCE.setEffects(island);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(24, ItemBuilder.copyOf(IslandsEffectsManager.INSTANCE.getPotionEffectItem(4))
                .name("§6Vision nocturne").lore(IslandsEffectsManager.INSTANCE.getLore(4, island)).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = IslandsEffectsManager.INSTANCE.getLevelForEffect(island, 4);
            if (currentLevel < 2) {
                double price = IslandsEffectsManager.INSTANCE.getPrice(4, currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer l'effet de vision nocturne ! "
                            + "Il manque §6" + NumberFormat.getInstance().format(price - island.getExp()) + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setLevelForEffect(4, currentLevel + 1);
                update(island, p);

                IslandsEffectsManager.INSTANCE.setEffects(island);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(30, ItemBuilder.copyOf(IslandsEffectsManager.INSTANCE.getPotionEffectItem(5))
                .name("§6Force").lore(IslandsEffectsManager.INSTANCE.getLore(5, island)).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = IslandsEffectsManager.INSTANCE.getLevelForEffect(island, 5);
            if (currentLevel < 2) {
                double price = IslandsEffectsManager.INSTANCE.getPrice(5, currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer l'effet de force ! "
                            + "Il manque §6" + NumberFormat.getInstance().format(price - island.getExp()) + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setLevelForEffect(5, currentLevel + 1);
                update(island, p);

                IslandsEffectsManager.INSTANCE.setEffects(island);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(31, ItemBuilder.copyOf(IslandsEffectsManager.INSTANCE.getPotionEffectItem(6))
                .name("§6Célérité").lore(IslandsEffectsManager.INSTANCE.getLore(6, island)).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = IslandsEffectsManager.INSTANCE.getLevelForEffect(island, 6);
            if (currentLevel < 2) {
                double price = IslandsEffectsManager.INSTANCE.getPrice(6, currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer l'effet de célérité ! "
                            + "Il manque §6" + NumberFormat.getInstance().format(price - island.getExp()) + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setLevelForEffect(6, currentLevel + 1);
                update(island, p);

                IslandsEffectsManager.INSTANCE.setEffects(island);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(32, ItemBuilder.copyOf(IslandsEffectsManager.INSTANCE.getPotionEffectItem(7))
                .name("§6Résistance").lore(IslandsEffectsManager.INSTANCE.getLore(7, island)).build(), e -> {
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, e.getWhoClicked().getUniqueId())) {
                p.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            int currentLevel = IslandsEffectsManager.INSTANCE.getLevelForEffect(island, 7);
            if (currentLevel < 2) {
                double price = IslandsEffectsManager.INSTANCE.getPrice(7, currentLevel + 1);
                if (island.getExp() < price) {
                    p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer l'effet de résistance ! "
                            + "Il manque §6" + NumberFormat.getInstance().format(price - island.getExp()) + "§c d'expérience."));
                    return;
                }
                island.setLevelExp(island.getExp() - price);
                island.setLevelForEffect(7, currentLevel + 1);
                update(island, p);

                IslandsEffectsManager.INSTANCE.setEffects(island);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });
    }
}
