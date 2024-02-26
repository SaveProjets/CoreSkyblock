package fr.farmeurimmo.coreskyblock.storage.sync;

import com.google.gson.JsonObject;
import fr.farmeurimmo.coreskyblock.utils.InventorySyncUtils;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;

public class SyncUser {
    private final UUID uuid;
    private String inventory;
    private double health;
    private int food;
    private float exp;
    private int level;
    private PotionEffect[] potionEffects;

    public SyncUser(UUID uuid, String inventory, double health, int food, float exp, int level,
                    PotionEffect[] potionEffects) {
        this.uuid = uuid;
        this.inventory = inventory;
        this.health = health;
        this.food = food;
        this.exp = exp;
        this.level = level;
        this.potionEffects = potionEffects;
    }

    // used for redis cache
    public static SyncUser fromJson(JsonObject json) {
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        String inventory = json.get("inventory").getAsString();
        float health = json.get("health").getAsFloat();
        int food = json.get("food").getAsInt();
        float exp = json.get("exp").getAsFloat();
        int level = json.has("level") ? json.get("level").getAsInt() : 0;
        PotionEffect[] potionEffects = null;
        if (json.has("potionEffects")) {
            potionEffects = InventorySyncUtils.INSTANCE.jsonElementToPotionEffects(json.get("potionEffects"));
        }
        if (potionEffects == null) {
            potionEffects = new PotionEffect[0];
        }
        return new SyncUser(uuid, inventory, health, food, exp, level, potionEffects);
    }

    public ItemStack[] getContentsItemStack() {
        return InventorySyncUtils.INSTANCE.jsonToInventory(inventory);
    }

    public String getInventory() {
        return inventory;
    }

    public void updateInventory(Inventory inv) {
        inventory = InventorySyncUtils.INSTANCE.inventoryToJson(inv);
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public float getExp() {
        return exp;
    }

    public void setExp(float exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public PotionEffect[] getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(PotionEffect[] potionEffects) {
        this.potionEffects = potionEffects;
    }

    public UUID getUuid() {
        return uuid;
    }

    // used for redis cache
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", uuid.toString());
        json.addProperty("inventory", inventory);
        json.addProperty("health", health);
        json.addProperty("food", food);
        json.addProperty("exp", exp);
        if (level > 0)
            json.addProperty("level", level);
        if (potionEffects != null && potionEffects.length > 0) {
            json.add("potionEffects", InventorySyncUtils.INSTANCE.potionEffectsToJson(potionEffects));
        }
        return json;
    }
}
