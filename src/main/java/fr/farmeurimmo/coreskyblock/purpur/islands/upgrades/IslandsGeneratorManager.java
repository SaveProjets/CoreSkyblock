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
        lore.add("");
        lore.add("§aDescription:");
        lore.add("§f▶  §7Améliorer le pourcentage");
        lore.add("    §7d'apparition de vos");
        lore.add("    §7différents minerais.");
        lore.add("");
        lore.add("§dInformation:");

        // Define headers
        String[] headers = {"Roche", "Charbon", "Fer", "Cuivre", "Or", "Redstone", "Lapis", "Diamant", "Émeraude", "Débris"};
        int[] maxWidths = new int[headers.length];

        // Calculate maximum width for each column
        for (int i = 0; i < headers.length; i++) {
            maxWidths[i] = stripColorCodes(headers[i]).length();
        }
        for (Map<Material, Integer> generator : generators) {
            for (int i = 0; i < headers.length; i++) {
                Material material = getMaterialByIndex(i);
                int width = stripColorCodes(getPercentage(generator, material)).length();
                if (width > maxWidths[i]) {
                    maxWidths[i] = width;
                }
            }
        }

        // Build header row
        StringBuilder headerRow = new StringBuilder("§7");
        for (int i = 0; i < headers.length; i++) {
            headerRow.append(padRight(headers[i], maxWidths[i])).append(" ");
        }
        lore.add(headerRow.toString());

        // Build data rows
        for (int i = 0; i < generators.size(); i++) {
            Map<Material, Integer> generator = generators.get(i);
            StringBuilder row = new StringBuilder("§7");
            for (int j = 0; j < headers.length; j++) {
                Material material = getMaterialByIndex(j);
                row.append(padRight(getPercentage(generator, material), maxWidths[j])).append(" ");
            }
            lore.add(row.toString());
            lore.add("§8| " + (currentOwned >= i + 1 ? "§aPossédée" : "§7Prix: §d§l" + getGeneratorPriceFromLevel(i + 1) + "§dexp"));
        }

        lore.add("");
        lore.add("§8➡ §fCliquez pour améliorer.");
        return lore;
    }

    private String padRight(String text, int length) {
        int paddingLength = Math.max(0, length - stripColorCodes(text).length());
        return text + " ".repeat(paddingLength);
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }

    private Material getMaterialByIndex(int index) {
        return switch (index) {
            case 0 -> Material.STONE;
            case 1 -> Material.COAL_ORE;
            case 2 -> Material.IRON_ORE;
            case 3 -> Material.COPPER_ORE;
            case 4 -> Material.GOLD_ORE;
            case 5 -> Material.REDSTONE_ORE;
            case 6 -> Material.LAPIS_ORE;
            case 7 -> Material.DIAMOND_ORE;
            case 8 -> Material.EMERALD_ORE;
            case 9 -> Material.ANCIENT_DEBRIS;
            default -> Material.STONE;
        };
    }

    private String getPercentage(Map<Material, Integer> generator, Material material) {
        return generator.getOrDefault(material, 0) + "%";
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
}
