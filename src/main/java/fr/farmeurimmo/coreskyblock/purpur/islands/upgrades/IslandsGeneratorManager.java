package fr.farmeurimmo.coreskyblock.purpur.islands.upgrades;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.listeners.GeneratorListener;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class IslandsGeneratorManager {

    public static IslandsGeneratorManager INSTANCE;
    private final LinkedList<Map<Material, Integer>> generators = new LinkedList<>();
    private final Random random = new Random();

    public IslandsGeneratorManager() {
        INSTANCE = this;

        generators.add(Map.of(Material.STONE, 88, Material.COAL_ORE, 5, Material.IRON_ORE, 2,
                Material.COPPER_ORE, 5));
        generators.add(Map.of(Material.STONE, 70, Material.COAL_ORE, 8, Material.IRON_ORE, 5,
                Material.COPPER_ORE, 8, Material.GOLD_ORE, 3, Material.REDSTONE_ORE, 3,
                Material.LAPIS_ORE, 3));
        generators.add(Map.of(Material.STONE, 40, Material.COAL_ORE, 14, Material.IRON_ORE, 10,
                Material.COPPER_ORE, 10, Material.GOLD_ORE, 6, Material.REDSTONE_ORE, 7,
                Material.LAPIS_ORE, 6, Material.DIAMOND_ORE, 3, Material.EMERALD_ORE, 3,
                Material.ANCIENT_DEBRIS, 1));
        generators.add(Map.of(Material.STONE, 25, Material.COAL_ORE, 10, Material.IRON_ORE, 13,
                Material.COPPER_ORE, 10, Material.GOLD_ORE, 10, Material.REDSTONE_ORE, 12,
                Material.LAPIS_ORE, 10, Material.DIAMOND_ORE, 5, Material.EMERALD_ORE, 3,
                Material.ANCIENT_DEBRIS, 2));
        generators.add(Map.of(Material.STONE, 11, Material.COAL_ORE, 8, Material.IRON_ORE, 10,
                Material.COPPER_ORE, 8, Material.GOLD_ORE, 15, Material.REDSTONE_ORE, 10,
                Material.LAPIS_ORE, 15, Material.DIAMOND_ORE, 10, Material.EMERALD_ORE, 8,
                Material.ANCIENT_DEBRIS, 5));

        CoreSkyblock.INSTANCE.getServer().getPluginManager().registerEvents(new GeneratorListener(), CoreSkyblock.INSTANCE);
    }

    public Material getMaterialRandom(int level) {
        Map<Material, Integer> generator = generators.get(level - 1);
        int total = generator.values().stream().mapToInt(i -> i).sum();
        int randomInt = random.nextInt(total);
        int current = 0;
        for (Map.Entry<Material, Integer> entry : generator.entrySet()) {
            current += entry.getValue();
            if (randomInt < current) {
                return entry.getKey();
            }
        }
        return Material.STONE;
    }

    public ArrayList<String> getLore(int currentOwned) {
        ArrayList<String> lore = new ArrayList<>();
        for (int i = 0; i < generators.size(); i++) {
            StringBuilder sb = new StringBuilder("§7" + (i + 1) + ":");
            Map<Material, Integer> generator = generators.get(i);
            StringBuilder sb2 = new StringBuilder();
            int amount = 0;
            for (Map.Entry<Material, Integer> entry : generator.entrySet()) {
                amount++;
                if (amount > 5) {
                    sb2.append(" §f").append(entry.getValue()).append("% §7").append(getMaterialNameForLore(entry.getKey())).append(",");
                    continue;
                }
                sb.append(" §f").append(entry.getValue()).append("% §7").append(getMaterialNameForLore(entry.getKey())).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            lore.add(sb.toString());
            if (amount > 5) {
                sb2.deleteCharAt(0);
                sb2.deleteCharAt(sb2.length() - 1);
                lore.add(sb2.toString());
            }
            lore.add("§8| " + (currentOwned >= i + 1 ? "§aDéjà Achetée" : "§7Prix: §e" + getGeneratorPriceFromLevel(i + 1) + "§6exp"));
        }
        return lore;
    }

    public String getMaterialNameForLore(Material material) {
        return switch (material) {
            case COAL_ORE -> "Charbon";
            case IRON_ORE -> "Fer";
            case COPPER_ORE -> "Cuivre";
            case GOLD_ORE -> "Or";
            case REDSTONE_ORE -> "Redstone";
            case LAPIS_ORE -> "Lapis";
            case DIAMOND_ORE -> "Diamant";
            case EMERALD_ORE -> "Émeraude";
            case ANCIENT_DEBRIS -> "Débris";
            default -> "Roche";
        };
    }

    public double getGeneratorPriceFromLevel(int level) {
        return switch (level) {
            case 2 -> 200;
            case 3 -> 500;
            case 4 -> 1000;
            case 5 -> 2000;
            default -> 0;
        };
    }
}
