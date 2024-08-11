package fr.farmeurimmo.coreskyblock.purpur.blocks.chests;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ChestType {

    CYBER_HOPPER(0, "§6§lCyber Hopper", Material.HOPPER, List.of("§7Une fois placé, il va automatiquement",
            "§7récupérer les items qui tombent dans son chunk", "§7et les téléporter dans son inventaire.")),
    SELL_CHEST(1, "§eSell Chest", Material.CHEST, List.of("§7Une fois placé, il va de temps en temps",
            "§7vendre les items qui entrent dans son", "§7inventaire et vous donner l'argent.")),
    PLAYER_SHOP(2, "§6§lPlayer Shop", Material.CHEST, List.of("§7Une fois placé, il vous permettra de",
            "§7vendre/acheter des items à d'autres joueurs."));

    private final int id;
    private final String name;
    private final Material material;
    private final List<String> lore;

    ChestType(int id, String name, Material material, List<String> lore) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.lore = lore;
    }

    public static ChestType getById(int id) {
        for (ChestType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    public static ChestType getByName(String name) {
        for (ChestType type : values()) {
            if (name.contains(type.getNameWithoutColor())) {
                return type;
            }
        }
        return null;
    }

    public static ChestType getByType(String type) {
        for (ChestType chestType : values()) {
            if (chestType.name().equalsIgnoreCase(type)) {
                return chestType;
            }
        }
        return null;
    }

    public static boolean containsChestTypeInIt(String name) {
        for (ChestType type : values()) {
            if (name.contains(type.getNameWithoutColor())) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Material> getMaterials() {
        return Arrays.stream(values()).collect(ArrayList::new, (list, type) -> list.add(type.getMaterial()), ArrayList::addAll);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getNameWithoutColor() {
        return ChatColor.stripColor(this.name);
    }

    public Material getMaterial() {
        return this.material;
    }

    public List<String> getLore() {
        return this.lore;
    }
}
