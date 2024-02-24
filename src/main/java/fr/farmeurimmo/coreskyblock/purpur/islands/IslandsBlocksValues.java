package fr.farmeurimmo.coreskyblock.purpur.islands;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class IslandsBlocksValues {

    public static IslandsBlocksValues INSTANCE;
    private final Map<Material, Float> blocksValues = new HashMap<>();

    public IslandsBlocksValues() {
        INSTANCE = this;

        blocksValues.put(Material.BEACON, 1f);
        blocksValues.put(Material.DRAGON_EGG, 1000f);
    }

    public float getBlockValue(Material material) {
        return blocksValues.getOrDefault(material, 0f);
    }

    public final Map<Material, Float> getBlocksValues() {
        return blocksValues;
    }
}
