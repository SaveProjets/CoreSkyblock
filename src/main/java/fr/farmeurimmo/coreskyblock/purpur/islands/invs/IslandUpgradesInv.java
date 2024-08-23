package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsBlocksLimiterManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsGeneratorManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsMaxMembersManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsSizeManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;

public class IslandUpgradesInv extends FastInv {

    public IslandUpgradesInv(Island island, Player p) {
        super(27, "§8Améliorations de l'île");

        if (island == null) {
            return;
        }

        update(island, p);

        setItem(26, CommonItemStacks.getCommonBack(), e -> new IslandInv(island).open((Player) e.getWhoClicked()));
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
    }
}
