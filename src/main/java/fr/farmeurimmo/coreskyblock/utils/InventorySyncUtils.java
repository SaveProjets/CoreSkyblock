package fr.farmeurimmo.coreskyblock.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class InventorySyncUtils {

    private static final int SIZE = 41; // 41 is the max size of a player inventory
    public static InventorySyncUtils INSTANCE;
    private final Gson gson = new Gson();

    public InventorySyncUtils() {
        INSTANCE = this;
    }


    // LINK https://github.com/brunyman/MysqlInventoryBridge/blob/master/Mysql%20Inventory%20Bridge/src/net/craftersland/bridge/inventory/InventoryUtils.java
    // AUTHOR brunyman
    // EDITED BY Farmeurimmo

    public String itemStackToBase64(ItemStack itemStack) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            if (itemStack == null) {
                return "";
            }

            // Write the item stack
            dataOutput.writeObject(itemStack);

            // Serialize that item stack
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    public ItemStack itemStackFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            return new ItemStack(Material.AIR);
        }
    }

    public String inventoryToBase64String(Inventory inventory) {
        JsonObject jsonObject = new JsonObject();
        int position = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null) {
                jsonObject.addProperty(String.valueOf(position), itemStackToBase64(itemStack));
            }
            position++;
        }
        return gson.toJson(jsonObject);
    }

    public ItemStack[] inventoryFromBase64String(String data) {
        JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
        ItemStack[] itemStacks = new ItemStack[SIZE];
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            itemStacks[Integer.parseInt(entry.getKey())] = itemStackFromBase64(entry.getValue().getAsString());
        }
        for (int i = 0; i < itemStacks.length; i++) {
            if (itemStacks[i] == null) {
                itemStacks[i] = new ItemStack(Material.AIR);
            }
        }
        return itemStacks;
    }


    public JsonObject potionEffectsToJson(PotionEffect[] potionEffects) {
        JsonObject jsonObject = new JsonObject();
        try {
            for (int i = 0; i < potionEffects.length; i++) {
                PotionEffect potionEffect = potionEffects[i];
                JsonObject potionEffectJson = new JsonObject();
                potionEffectJson.addProperty("type", potionEffect.getType().getName());
                potionEffectJson.addProperty("duration", potionEffect.getDuration());
                potionEffectJson.addProperty("amplifier", potionEffect.getAmplifier());
                potionEffectJson.addProperty("ambient", potionEffect.isAmbient());
                potionEffectJson.addProperty("particles", potionEffect.hasParticles());
                potionEffectJson.addProperty("icon", potionEffect.hasIcon());
                jsonObject.add(String.valueOf(i), potionEffectJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String potionEffectsToStringJson(PotionEffect[] potionEffects) {
        if (potionEffects == null || potionEffects.length == 0) return "";
        return gson.toJson(potionEffectsToJson(potionEffects));
    }

    public PotionEffect[] jsonElementToPotionEffects(JsonElement jsonElement) {
        return jsonToPotionEffects(gson.toJson(jsonElement));
    }

    public PotionEffect[] jsonToPotionEffects(String json) {
        if (json == null || json.isEmpty()) return new PotionEffect[0];
        if (json.length() < 2) return new PotionEffect[0];
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        PotionEffect[] potionEffects = new PotionEffect[jsonObject.size()];
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonObject potionEffectJson = entry.getValue().getAsJsonObject();
            PotionEffect potionEffect = new PotionEffect(
                    Objects.requireNonNull(PotionEffectType.getByName(potionEffectJson.get("type").getAsString())),
                    potionEffectJson.get("duration").getAsInt(),
                    potionEffectJson.get("amplifier").getAsInt(),
                    potionEffectJson.get("ambient").getAsBoolean(),
                    potionEffectJson.get("particles").getAsBoolean(),
                    potionEffectJson.get("icon").getAsBoolean()
            );
            potionEffects[Integer.parseInt(entry.getKey())] = potionEffect;
        }
        return potionEffects;
    }
}
