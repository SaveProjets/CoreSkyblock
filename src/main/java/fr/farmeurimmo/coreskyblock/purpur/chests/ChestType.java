package fr.farmeurimmo.coreskyblock.purpur.chests;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ChestType {

    CROP_HOPPER(0, "§6§lCropHopper", Material.HOPPER, List.of("§7Une fois placé, il va automatiquement",
            "§7récupérer les items qui tombent dans son chunk", "§7et les téléporter dans son inventaire.")),
    SELL_CHEST(1, "§6§lSellChest", Material.CHEST, List.of("§7Une fois placé, il va de temps en temps",
            "§7vendre les items qui entrent dans son", "§7inventaire et vous donner l'argent.")),
    PLAYER_SHOP(2, "§6§lPlayerShop", Material.CHEST, List.of("§7Une fois placé, il vous permettra de",
            "§7vendre/acheter des items à d'autres joueurs.")),
    BLOCK_STOCKER(3, "§6§lBlockStocker", null, List.of("§7Une fois placé, vous pourrez stocker",
            "§7un type de bloc possédant une valeur d'île", "§7Il permet de limiter le nombre de blocs",
            "§7sur l'île pour un même is level."));

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
