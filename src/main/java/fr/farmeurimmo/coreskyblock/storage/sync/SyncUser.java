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
    private PotionEffect[] potionEffects;

    public SyncUser(UUID uuid, String inventory, double health, int food, float exp, PotionEffect[] potionEffects) {
        this.uuid = uuid;
        this.inventory = inventory;
        this.health = health;
        this.food = food;
        this.exp = exp;
        this.potionEffects = potionEffects;
    }

    // used for redis cache
    public static SyncUser fromJson(JsonObject json) {
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        String inventory = json.get("inventory").getAsString();
        float health = json.get("health").getAsFloat();
        int food = json.get("food").getAsInt();
        float exp = json.get("exp").getAsFloat();
        PotionEffect[] potionEffects = new PotionEffect[0];
        if (json.has("potionEffects") && !json.get("potionEffects").isJsonNull()) {
            potionEffects = InventorySyncUtils.INSTANCE.jsonToPotionEffects(json.get("potionEffects").getAsString());
        }
        return new SyncUser(uuid, inventory, health, food, exp, potionEffects);
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
        if (potionEffects != null && potionEffects.length > 0) {
            json.add("potionEffects", InventorySyncUtils.INSTANCE.potionEffectsToJson(potionEffects));
        }
        return json;
    }
}
