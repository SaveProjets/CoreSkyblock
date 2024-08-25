package fr.farmeurimmo.coreskyblock.purpur.islands.upgrades;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandSettings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;

public class IslandsEffectsManager {

    public static final int EFFECT_DURATION = 15 * 20;

    /*
     * index 0 = speed (max 2) Prix : 300 puis 500
     * index 1 = régénération (max 1) Prix : 300
     * index 2 = résistance au feu (max 1) Prix : 150
     * index 3 = respiration aquatique (max 1) Prix : 150
     * index 4 = vision nocturne (max 1) Prix : 400
     * index 5 = force (max 2) Prix : 400 puis 400
     * index 6 = célérité (max 3) Prix : 300 ensuite 500 ensuite 500
     * index 7 = résistance (max 1) Prix : 300
     * */
    public static IslandsEffectsManager INSTANCE;

    public IslandsEffectsManager() {
        INSTANCE = this;
    }

    public void setEffects(Island island) {
        for (Player p : island.getOnlineMembers()) {
            setEffectsPlayer(p, island);
        }
    }

    public void setEffectsPlayer(Player p, Island island) {
        if (!p.getWorld().getName().equalsIgnoreCase(IslandsManager.INSTANCE.getIslandWorldName(island.getIslandUUID()))) {
            removeEffectThatArentActive(p, island, true);
            return;
        }
        removeEffectThatArentActive(p, island, false);

        if (getLevelForEffect(island, 0) >= 0)
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_DURATION, getLevelForEffect(island, 0), true, false));
        if (getLevelForEffect(island, 1) >= 0)
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, EFFECT_DURATION, getLevelForEffect(island, 1), true, false));
        if (getLevelForEffect(island, 2) >= 0)
            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, EFFECT_DURATION, getLevelForEffect(island, 2), true, false));
        if (getLevelForEffect(island, 3) >= 0)
            p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, EFFECT_DURATION, getLevelForEffect(island, 3), true, false));
        if (getLevelForEffect(island, 4) >= 0)
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, EFFECT_DURATION, getLevelForEffect(island, 4), true, false));
        if (getLevelForEffect(island, 5) >= 0)
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, EFFECT_DURATION, getLevelForEffect(island, 5), true, false));
        if (getLevelForEffect(island, 6) >= 0)
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, EFFECT_DURATION, getLevelForEffect(island, 6), true, false));
        if (getLevelForEffect(island, 7) >= 0)
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, EFFECT_DURATION, getLevelForEffect(island, 7), true, false));
    }

    public void removeEffectThatArentActive(Player p, Island island, boolean force) {
        for (PotionEffect effect : new ArrayList<>(p.getActivePotionEffects())) {
            if (!effect.hasParticles() && force) {
                p.removePotionEffect(effect.getType());
                return;
            }
            if (!island.hasSettingActivated(IslandSettings.SPEED_EFFECT) && effect.getType().equals(PotionEffectType.SPEED))
                p.removePotionEffect(effect.getType());
            if (!island.hasSettingActivated(IslandSettings.REGENERATION_EFFECT) && effect.getType().equals(PotionEffectType.REGENERATION))
                p.removePotionEffect(effect.getType());
            if (!island.hasSettingActivated(IslandSettings.RESISTANCE_FIRE_EFFECT) && effect.getType().equals(PotionEffectType.FIRE_RESISTANCE))
                p.removePotionEffect(effect.getType());
            if (!island.hasSettingActivated(IslandSettings.RESPIRATION_EFFECT) && effect.getType().equals(PotionEffectType.WATER_BREATHING))
                p.removePotionEffect(effect.getType());
            if (!island.hasSettingActivated(IslandSettings.NIGHT_VISION_EFFECT) && effect.getType().equals(PotionEffectType.NIGHT_VISION))
                p.removePotionEffect(effect.getType());
            if (!island.hasSettingActivated(IslandSettings.STRENGTH_EFFECT) && effect.getType().equals(PotionEffectType.STRENGTH))
                p.removePotionEffect(effect.getType());
            if (!island.hasSettingActivated(IslandSettings.HASTE_EFFECT) && effect.getType().equals(PotionEffectType.HASTE))
                p.removePotionEffect(effect.getType());
            if (!island.hasSettingActivated(IslandSettings.RESISTANCE_EFFECT) && effect.getType().equals(PotionEffectType.RESISTANCE))
                p.removePotionEffect(effect.getType());
        }
    }

    public int getLevelForEffect(Island island, int number) {
        try {
            return Integer.parseInt(island.getEffectsLevel().split(":")[number]);
        } catch (Exception e) {
            return -1;
        }
    }

    public ItemStack getPotionEffectItem(int number) {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        switch (number) {
            case 0:
                meta.setBasePotionType(PotionType.LONG_SWIFTNESS);
                break;
            case 1:
                meta.setBasePotionType(PotionType.LONG_REGENERATION);
                break;
            case 2:
                meta.setBasePotionType(PotionType.LONG_FIRE_RESISTANCE);
                break;
            case 3:
                meta.setBasePotionType(PotionType.LONG_WATER_BREATHING);
                break;
            case 4:
                meta.setBasePotionType(PotionType.LONG_NIGHT_VISION);
                break;
            case 5:
                meta.setBasePotionType(PotionType.LONG_STRENGTH);
                break;
            case 6:
                meta.setBasePotionType(PotionType.WATER_BREATHING);
                break;
            case 7:
                meta.setBasePotionType(PotionType.STRONG_HARMING);
                break;
        }
        item.setItemMeta(meta);
        item.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        return item;
    }

    public int getPrice(int number, int level) {
        return switch (number) {
            case 0, 6 -> level == 0 ? 300 : 500;
            case 1, 7 -> 300;
            case 2, 3 -> 150;
            case 4, 5 -> 400;
            default -> 0;
        };
    }

    public boolean ownIt(int number, int level, Island island) {
        return getLevelForEffect(island, number) >= level;
    }

    public ArrayList<String> getLore(int number, Island island) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§aDescription:");
        switch (number) {
            case 0:
                lore.add("§f▶  §7Effet de vitesse pour");
                lore.add("    §7tous les membres de l'île.");
                lore.add("");
                lore.add("§dInformation:");
                lore.add("§f▶  §7Vitesse 1: " + (ownIt(number, 0, island) ? "§aPossédée" : "§d§l" + getPrice(0, 0) + "§dexp"));
                lore.add("§f▶  §7Vitesse 2: " + (ownIt(number, 1, island) ? "§aPossédée" : "§d§l" + getPrice(0, 1) + "§dexp"));
                break;
            case 1:
                lore.add("§f▶  §7Effet de régénération pour");
                lore.add("    §7tous les membres de l'île.");
                lore.add("");
                lore.add("§dInformation:");
                lore.add("§f▶  §7Régénération: " + (ownIt(number, 0, island) ? "§aPossédée" : "§d§l" + getPrice(1, 0) + "§dexp"));
                break;
            case 2:
                lore.add("§f▶  §7Effet de résistance au feu pour");
                lore.add("    §7tous les membres de l'île.");
                lore.add("");
                lore.add("§dInformation:");
                lore.add("§f▶  §7Résistance au feu: " + (ownIt(number, 0, island) ? "§aPossédée" : "§d§l" + getPrice(2, 0) + "§dexp"));
                break;
            case 3:
                lore.add("§f▶  §7Effet de respiration aquatique pour");
                lore.add("    §7tous les membres de l'île.");
                lore.add("");
                lore.add("§dInformation:");
                lore.add("§f▶  §7Respiration aquatique: " + (ownIt(number, 0, island) ? "§aPossédée" : "§d§l" + getPrice(3, 0) + "§dexp"));
                break;
            case 4:
                lore.add("§f▶  §7Effet de vision nocturne pour");
                lore.add("    §7tous les membres de l'île.");
                lore.add("");
                lore.add("§dInformation:");
                lore.add("§f▶  §7Vision nocturne: " + (ownIt(number, 0, island) ? "§aPossédée" : "§d§l" + getPrice(4, 0) + "§dexp"));
                break;
            case 5:
                lore.add("§f▶  §7Effet de force pour");
                lore.add("    §7tous les membres de l'île.");
                lore.add("");
                lore.add("§dInformation:");
                lore.add("§f▶  §7Force 1: " + (ownIt(number, 0, island) ? "§aPossédée" : "§d§l" + getPrice(5, 0) + "§dexp"));
                lore.add("§f▶  §7Force 2: " + (ownIt(number, 1, island) ? "§aPossédée" : "§d§l" + getPrice(5, 1) + "§dexp"));
                break;
            case 6:
                lore.add("§f▶  §7Effet de célérité pour");
                lore.add("    §7tous les membres de l'île.");
                lore.add("");
                lore.add("§dInformation:");
                lore.add("§f▶  §7Célérité 1: " + (ownIt(number, 0, island) ? "§aPossédée" : "§d§l" + getPrice(6, 0) + "§dexp"));
                lore.add("§f▶  §7Célérité 2: " + (ownIt(number, 1, island) ? "§aPossédée" : "§d§l" + getPrice(6, 1) + "§dexp"));
                lore.add("§f▶  §7Célérité 3: " + (ownIt(number, 2, island) ? "§aPossédée" : "§d§l" + getPrice(6, 2) + "§dexp"));
                break;
            case 7:
                lore.add("§f▶  §7Effet de résistance pour");
                lore.add("    §7tous les membres de l'île.");
                lore.add("");
                lore.add("§dInformation:");
                lore.add("§f▶  §7Résistance: " + (ownIt(number, 0, island) ? "§aPossédée" : "§d§l" + getPrice(7, 0) + "§dexp"));
                break;
        }
        lore.add("");
        lore.add("§8➡ §fCliquez pour améliorer.");

        return lore;
    }
}
