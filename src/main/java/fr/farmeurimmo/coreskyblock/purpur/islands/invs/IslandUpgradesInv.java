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
import java.util.List;
import java.util.function.BiConsumer;

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

        setUpgradeItem(10, new ItemStack(Material.GRASS_BLOCK), "§6Taille de l'île",
                IslandsSizeManager.INSTANCE.getLore(island.getMaxSize()),
                this::upgradeSize, island);

        setUpgradeItem(11, new ItemStack(Material.COBBLESTONE), "§6Générateur de l'île",
                IslandsGeneratorManager.INSTANCE.getLore(island.getGeneratorLevel()),
                this::upgradeGenerator, island);

        setUpgradeItem(13, new ItemStack(Material.BEACON), "§6Membres de l'île",
                IslandsMaxMembersManager.INSTANCE.getLore(island.getMaxMembers()),
                this::upgradeMembers, island);

        setUpgradeItem(15, new ItemStack(Material.HOPPER), "§6Hoppeurs",
                IslandsBlocksLimiterManager.INSTANCE.getLores(Material.HOPPER, island.getHopperLevel()),
                this::upgradeHoppers, island);

        setUpgradeItem(16, new ItemStack(Material.SPAWNER), "§6Spawneurs",
                IslandsBlocksLimiterManager.INSTANCE.getLores(Material.SPAWNER, island.getSpawnerLevel()),
                this::upgradeSpawners, island);

        setUpgradeItem(20, IslandsEffectsManager.INSTANCE.getPotionEffectItem(0), "§6Vitesse",
                IslandsEffectsManager.INSTANCE.getLore(0, island),
                (is, player) -> upgradeEffect(is, player, 0), island);

        setUpgradeItem(21, IslandsEffectsManager.INSTANCE.getPotionEffectItem(1), "§6Régénération",
                IslandsEffectsManager.INSTANCE.getLore(1, island),
                (is, player) -> upgradeEffect(is, player, 1), island);

        setUpgradeItem(22, IslandsEffectsManager.INSTANCE.getPotionEffectItem(2), "§6Résistance au feu",
                IslandsEffectsManager.INSTANCE.getLore(2, island),
                (is, player) -> upgradeEffect(is, player, 2), island);

        setUpgradeItem(23, IslandsEffectsManager.INSTANCE.getPotionEffectItem(3), "§6Respiration aquatique",
                IslandsEffectsManager.INSTANCE.getLore(3, island),
                (is, player) -> upgradeEffect(is, player, 3), island);

        setUpgradeItem(24, IslandsEffectsManager.INSTANCE.getPotionEffectItem(4), "§6Vision nocturne",
                IslandsEffectsManager.INSTANCE.getLore(4, island),
                (is, player) -> upgradeEffect(is, player, 4), island);

        setUpgradeItem(30, IslandsEffectsManager.INSTANCE.getPotionEffectItem(5), "§6Force",
                IslandsEffectsManager.INSTANCE.getLore(5, island),
                (is, player) -> upgradeEffect(is, player, 5), island);

        setUpgradeItem(31, IslandsEffectsManager.INSTANCE.getPotionEffectItem(6), "§6Célérité",
                IslandsEffectsManager.INSTANCE.getLore(6, island),
                (is, player) -> upgradeEffect(is, player, 6), island);

        setUpgradeItem(32, IslandsEffectsManager.INSTANCE.getPotionEffectItem(7), "§6Résistance",
                IslandsEffectsManager.INSTANCE.getLore(7, island),
                (is, player) -> upgradeEffect(is, player, 7), island);
    }

    private void setUpgradeItem(int slot, ItemStack itemStack, String name, List<String> lore, BiConsumer<Island, Player> upgradeAction, Island island) {
        setItem(slot, ItemBuilder.copyOf(itemStack).name(name).lore(lore).flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP).build(), e -> {
            Player player = (Player) e.getWhoClicked();
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly(player);
                return;
            }
            if (!island.hasPerms(island, IslandPerms.UPGRADE_ISLAND, player.getUniqueId())) {
                player.sendMessage(Component.text("§cVous n'avez pas la permission d'améliorer l'île !"));
                return;
            }
            upgradeAction.accept(island, player);
        });
    }

    private void upgradeSize(Island island, Player p) {
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
    }

    private void upgradeGenerator(Island island, Player p) {
        int currentLevel = island.getGeneratorLevel();
        if (currentLevel < 5) {
            double price = IslandsGeneratorManager.INSTANCE.getGeneratorPriceFromLevel(currentLevel + 1);
            if (island.getExp() < price) {
                p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer le "
                        + "générateur ! Il manque §6" + NumberFormat.getInstance().format(price - island.getExp())
                        + "§c d'expérience."));
                return;
            }
            island.setLevelExp(island.getExp() - price);
            island.setGeneratorLevel(currentLevel + 1);
            update(island, p);
        } else {
            p.sendMessage(Component.text("§cVotre générateur est déjà au niveau maximum !"));
        }
    }

    private void upgradeMembers(Island island, Player p) {
        int currentLevel = island.getMaxMembers();
        if (currentLevel < 5) {
            double price = IslandsMaxMembersManager.INSTANCE.getMembersPriceFromLevel(currentLevel + 1);
            if (island.getExp() < price) {
                p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer le nombre "
                        + "de membres ! Il manque §6" + NumberFormat.getInstance().format(price - island.getExp())
                        + "§c d'expérience."));
                return;
            }
            island.setLevelExp(island.getExp() - price);
            island.setMaxMembers(currentLevel + 1);
            update(island, p);
        } else {
            p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
        }
    }

    private void upgradeHoppers(Island island, Player p) {
        int currentLevel = island.getHopperLevel();
        if (currentLevel < 5) {
            double price = IslandsBlocksLimiterManager.INSTANCE.getPrice(Material.HOPPER, currentLevel + 1);
            if (island.getExp() < price) {
                p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer le "
                        + "nombre de hoppeurs ! Il manque §6" + NumberFormat.getInstance().format(price - island.getExp())
                        + "§c d'expérience."));
                return;
            }
            island.setLevelExp(island.getExp() - price);
            island.setHopperLevel(currentLevel + 1);
            update(island, p);
        } else {
            p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
        }
    }

    private void upgradeSpawners(Island island, Player p) {
        int currentLevel = island.getSpawnerLevel();
        double price = IslandsBlocksLimiterManager.INSTANCE.getPrice(Material.SPAWNER, currentLevel + 1);
        if (island.getExp() < price) {
            p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer le "
                    + "nombre de spawneurs ! Il manque §6" + NumberFormat.getInstance().format(price - island.getExp())
                    + "§c d'expérience."));
            return;
        }
        island.setLevelExp(island.getExp() - price);
        island.setSpawnerLevel(currentLevel + 1);
        update(island, p);
    }

    private void upgradeEffect(Island island, Player p, int effectIndex) {
        int currentLevel = IslandsEffectsManager.INSTANCE.getLevelForEffect(island, effectIndex);
        if (currentLevel < 2) {
            double price = IslandsEffectsManager.INSTANCE.getPrice(effectIndex, currentLevel + 1);
            if (island.getExp() < price) {
                p.sendMessage(Component.text("§cL'île n'a pas assez d'expérience pour améliorer l'effet ! "
                        + "Il manque §6" + NumberFormat.getInstance().format(price - island.getExp()) + "§c d'expérience."));
                return;
            }
            island.setLevelExp(island.getExp() - price);
            island.setLevelForEffect(effectIndex, currentLevel + 1);
            update(island, p);

            IslandsEffectsManager.INSTANCE.setEffects(island);
        } else {
            p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
        }
    }
}
