package fr.farmeurimmo.mineblock.purpur.islands.invs;

import fr.farmeurimmo.mineblock.common.islands.Island;
import fr.farmeurimmo.mineblock.purpur.islands.upgrades.IslandsGeneratorManager;
import fr.farmeurimmo.mineblock.purpur.islands.upgrades.IslandsMaxMembersManager;
import fr.farmeurimmo.mineblock.purpur.islands.upgrades.IslandsSizeManager;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IslandUpgradesInv extends FastInv {

    public IslandUpgradesInv(Island island, Player p) {
        super(27, "§8Améliorations de l'île");

        if (island == null) {
            return;
        }

        update(island, p);

        setItem(26, ItemBuilder.copyOf(new ItemStack(Material.ARROW))
                .name("§6Retour §8| §7(clic gauche)").build(), e -> new IslandInv(island).open((Player) e.getWhoClicked()));
    }

    private void update(Island island, Player p) {
        if (island == null) {
            return;
        }

        setItem(10, ItemBuilder.copyOf(new ItemStack(Material.GRASS_BLOCK)).name("§6Taille de l'île")
                .lore(IslandsSizeManager.INSTANCE.getLore(island.getMaxSize())).build(), e -> {
            int currentLevelSize = island.getMaxSize();
            if (currentLevelSize < 5) {
                double price = IslandsSizeManager.INSTANCE.getSizePriceFromLevel(currentLevelSize + 1);
                //FIXME: Add the possibility to buy the upgrade
                island.setMaxSize(currentLevelSize + 1);
                p.sendMessage(Component.text("§aEn développement... Prix: " + price + "exp"));
                update(island, p);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(11, ItemBuilder.copyOf(new ItemStack(Material.COBBLESTONE)).name("§6Générateur de l'île")
                .lore(IslandsGeneratorManager.INSTANCE.getLore(island.getGeneratorLevel())).build(), e -> {
            int currentLevel = island.getGeneratorLevel();
            if (currentLevel < 5) {
                double price = IslandsGeneratorManager.INSTANCE.getGeneratorPriceFromLevel(currentLevel + 1);
                //FIXME: Add the possibility to buy the upgrade
                p.sendMessage(Component.text("§aEn développement... Prix: " + price + "exp"));
                island.setGeneratorLevel(currentLevel + 1);
                update(island, p);
            } else {
                p.sendMessage(Component.text("§cVotre générateur est déjà au niveau maximum !"));
            }
        });

        setItem(12, ItemBuilder.copyOf(new ItemStack(Material.BEACON)).name("§6Membres de l'île")
                .lore(IslandsMaxMembersManager.INSTANCE.getLore(island.getMaxMembers())).build(), e -> {
            int currentLevel = island.getMaxMembers();
            if (currentLevel < 5) {
                double price = IslandsMaxMembersManager.INSTANCE.getMembersPriceFromLevel(currentLevel + 1);
                p.sendMessage(Component.text("§aEn développement... Prix: " + price + "exp"));
                island.setMaxMembers(currentLevel + 1);
                update(island, p);
            } else {
                p.sendMessage(Component.text("§cVotre île est déjà au niveau maximum !"));
            }
        });

        setItem(14, ItemBuilder.copyOf(new ItemStack(Material.PAPER))
                .name("§6Warps d'île").build(), e -> p.sendMessage(Component.text("§cEn développement...")));

        setItem(15, ItemBuilder.copyOf(new ItemStack(Material.CHEST))
                .name("§6Coffres et Hoppeurs").build(), e -> p.sendMessage(Component.text("§cEn développement...")));

        setItem(16, ItemBuilder.copyOf(new ItemStack(Material.SPAWNER))
                .name("§6Spawneurs").build(), e -> p.sendMessage(Component.text("§cEn développement...")));
    }
}
