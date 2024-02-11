package fr.farmeurimmo.mineblock.purpur.islands.invs;

import fr.farmeurimmo.mineblock.common.islands.Island;
import fr.farmeurimmo.mineblock.purpur.islands.IslandsManager;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IslandUpgradesInv extends FastInv {

    public IslandUpgradesInv(Island island, Player p) {
        super(27, "§8Améliorations de l'île");

        if (island == null) {
            return;
        }

        String[] lore = new String[5];
        for (int i = 1; i <= 5; i++) {
            lore[i - 1] = "§7" + i + ": §6" + IslandsManager.INSTANCE.getSizeFromLevel(i) + "§7x§6" +
                    IslandsManager.INSTANCE.getSizeFromLevel(i) + " §8| §7Prix xp: §e" +
                    IslandsManager.INSTANCE.getSizePriceFromLevel(i) + "§7$";
        }

        setItem(10, ItemBuilder.copyOf(new ItemStack(Material.GRASS_BLOCK))
                .name("§6Taille de l'île").lore(lore).build(), e -> {
            int currentLevel = island.getMaxSize();
            if (currentLevel < 5) {
                double price = IslandsManager.INSTANCE.getSizePriceFromLevel(currentLevel + 1);
                p.sendMessage("§aEn développement... Prix: " + price + "xp");
            } else {
                p.sendMessage("§cVotre île est déjà au niveau maximum !");
            }
        });

        setItem(11, ItemBuilder.copyOf(new ItemStack(Material.COBBLESTONE))
                .name("§6Générateur de l'île").build(), e -> {
            int currentLevel = island.getGeneratorLevel();
            if (currentLevel < 5) {
                double price = IslandsManager.INSTANCE.getGeneratorPriceFromLevel(currentLevel + 1);
                p.sendMessage("§aEn développement... Prix: " + price + "xp");
            } else {
                p.sendMessage("§cVotre générateur est déjà au niveau maximum !");
            }
        });

        setItem(12, ItemBuilder.copyOf(new ItemStack(Material.BEACON))
                .name("§6Membres de l'île").build(), e -> {
            int currentLevel = island.getMaxMembers();
            if (currentLevel < 5) {
                double price = IslandsManager.INSTANCE.getMembersPriceFromLevel(currentLevel + 1);
                p.sendMessage("§aEn développement... Prix: " + price + "xp");
            } else {
                p.sendMessage("§cVotre île est déjà au niveau maximum !");
            }
        });

        setItem(14, ItemBuilder.copyOf(new ItemStack(Material.PAPER))
                .name("§6Warps d'île").build(), e -> {
            p.sendMessage("§cEn développement...");
        });

        setItem(15, ItemBuilder.copyOf(new ItemStack(Material.CHEST))
                .name("§6Coffres et Hoppeurs").build(), e -> {
            p.sendMessage("§cEn développement...");
        });

        setItem(16, ItemBuilder.copyOf(new ItemStack(Material.SPAWNER))
                .name("§6Spawneurs").build(), e -> {
            p.sendMessage("§cEn développement...");
        });


        setItem(26, ItemBuilder.copyOf(new ItemStack(Material.ARROW))
                .name("§6Retour §8| §7(clic gauche)").build(), e -> new IslandInv(island).open((Player) e.getWhoClicked()));
    }
}
