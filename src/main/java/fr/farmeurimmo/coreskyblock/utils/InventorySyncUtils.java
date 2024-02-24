package fr.farmeurimmo.coreskyblock.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class InventorySyncUtils {

    public static InventorySyncUtils INSTANCE;
    private final Gson gson = new Gson();

    public InventorySyncUtils() {
        INSTANCE = this;
    }

    public String inventoryToJson(Inventory inventory) {
        JsonObject jsonObject = new JsonObject();
        int position = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null) {
                jsonObject.add(String.valueOf(position), itemStackToJson(itemStack));
            }
            position++;
        }

        return jsonObject.toString();
    }

    public ItemStack[] jsonToInventory(String json, int size) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        ItemStack[] itemStacks = new ItemStack[size];
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            itemStacks[Integer.parseInt(entry.getKey())] = jsonToItemStack(entry.getValue().getAsJsonObject());
        }
        for (int i = 0; i < itemStacks.length; i++) {
            if (itemStacks[i] == null) {
                itemStacks[i] = new ItemStack(Material.AIR);
            }
        }
        return itemStacks;
    }

    public JsonObject itemStackToJson(ItemStack itemStack) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", itemStack.getType().name());
        jsonObject.addProperty("amount", itemStack.getAmount());
        jsonObject.addProperty("durability", itemStack.getDurability());
        jsonObject.addProperty("displayName", itemStack.getDisplayName());
        jsonObject.addProperty("lore", Optional.ofNullable(itemStack.getItemMeta()).map(ItemMeta::getLore).map(gson::toJson).orElse(""));

        if (itemStack.hasEnchants())
            jsonObject.addProperty("enchantments", gson.toJson(itemStack.getEnchantments()));

        jsonObject.addProperty("itemFlags", gson.toJson(itemStack.getItemMeta().getItemFlags()));

        if (itemStack.hasCustomModelData())
            jsonObject.addProperty("customModelData", itemStack.getCustomModelData());

        jsonObject.addProperty("repairCost", itemStack.getRepairCost());
        jsonObject.addProperty("isUnbreakable", itemStack.getItemMeta().isUnbreakable());

        if (itemStack.hasAttributeModifiers())
            jsonObject.addProperty("attributes", gson.toJson(itemStack.getItemMeta().getAttributeModifiers()));

        if (itemStack.getType().name().contains("TIPPED_ARROW")) {
            jsonObject.addProperty("potionType", itemStack.getType().name());
            jsonObject.addProperty("potionData", gson.toJson(itemStack.getDurability()));
        }

        if (itemStack.getItemMeta() instanceof BookMeta bookMeta) {
            jsonObject.addProperty("author", bookMeta.getAuthor());
            jsonObject.addProperty("title", bookMeta.getTitle());
            jsonObject.addProperty("pages", gson.toJson(bookMeta.getPages()));
        }

        if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
            jsonObject.addProperty("storedEnchantments", gson.toJson(enchantmentStorageMeta.getStoredEnchants()));
        }

        if (itemStack.getItemMeta() instanceof SkullMeta skullMeta) {
            jsonObject.addProperty("owner", skullMeta.getOwningPlayer().getName());
        }

        if (itemStack.getItemMeta() instanceof BannerMeta bannerMeta) {
            jsonObject.addProperty("baseColor", bannerMeta.getBaseColor().name());
            jsonObject.addProperty("patterns", gson.toJson(bannerMeta.getPatterns()));
        }

        if (itemStack.getItemMeta() instanceof FireworkMeta) {
            jsonObject.addProperty("fireworkEffect", gson.toJson(((FireworkMeta) itemStack.getItemMeta()).getEffects()));
            jsonObject.addProperty("fireworkPower", ((FireworkMeta) itemStack.getItemMeta()).getPower());
        }

        if (itemStack.getItemMeta() instanceof FireworkEffectMeta fireworkEffectMeta) {
            jsonObject.addProperty("fireworkEffect", gson.toJson(fireworkEffectMeta.getEffect()));
        }

        if (itemStack.getItemMeta() instanceof TropicalFishBucketMeta tropicalFishBucketMeta) {
            jsonObject.addProperty("bodyColor", tropicalFishBucketMeta.getBodyColor().name());
            jsonObject.addProperty("pattern", tropicalFishBucketMeta.getPattern().name());
            jsonObject.addProperty("patternColor", tropicalFishBucketMeta.getPatternColor().name());
        }

        if (itemStack.getItemMeta() instanceof PotionMeta pM) {
            jsonObject.addProperty("potionEffects", gson.toJson(pM.getCustomEffects()));
        }

        if (itemStack.getItemMeta() instanceof LeatherArmorMeta leatherArmorMeta) {
            jsonObject.addProperty("color", leatherArmorMeta.getColor().asRGB());
        }

        // We don't need to store the map data
        /*if (itemStack.getItemMeta() instanceof MapMeta mapMeta) {
            jsonObject.addProperty("scaling", mapMeta.isScaling());
            jsonObject.addProperty("locationName", mapMeta.getLocationName());
            jsonObject.addProperty("mapId", mapMeta.getMapId());
        }*/

        if (itemStack.getItemMeta() instanceof CompassMeta compassMeta) {
            jsonObject.addProperty("lodestone", LocationTranslator.fromLocation(Objects.requireNonNull(compassMeta.getLodestone())));
        }

        if (itemStack.getItemMeta() instanceof CrossbowMeta crossbowMeta) {
            jsonObject.addProperty("chargedProjectiles", gson.toJson(crossbowMeta.getChargedProjectiles()));
        }

        return jsonObject;
    }

    public ItemStack jsonToItemStack(JsonObject jsonObject) {
        if (jsonObject == null || jsonObject.isJsonNull()) {
            return null;
        }

        Material material = jsonObject.has("type") ? Material.getMaterial(jsonObject.get("type").getAsString()) : Material.AIR;
        int amount = jsonObject.has("amount") ? jsonObject.get("amount").getAsInt() : 1;
        short durability = jsonObject.has("durability") ? jsonObject.get("durability").getAsShort() : 0;
        String displayName = jsonObject.has("displayName") ? jsonObject.get("displayName").getAsString() : null;
        String lore = jsonObject.has("lore") ? jsonObject.get("lore").getAsString() : null;
        Map<Enchantment, Integer> enchantments = jsonObject.has("enchantments") ? gson.fromJson(jsonObject.get("enchantments").getAsString(), new TypeToken<Map<Enchantment, Integer>>() {
        }.getType()) : null;
        List<ItemFlag> itemFlags = jsonObject.has("itemFlags") ? gson.fromJson(jsonObject.get("itemFlags").getAsString(), new TypeToken<List<ItemFlag>>() {
        }.getType()) : null;
        Map<Attribute, AttributeModifier> attributes = jsonObject.has("attributes") ? gson.fromJson(jsonObject.get("attributes").getAsString(), new TypeToken<Map<Attribute, AttributeModifier>>() {
        }.getType()) : null;
        int customModelData = jsonObject.has("customModelData") ? jsonObject.get("customModelData").getAsInt() : 0;
        int repairCost = jsonObject.has("repairCost") ? jsonObject.get("repairCost").getAsInt() : 0;
        boolean isUnbreakable = jsonObject.has("isUnbreakable") && jsonObject.get("isUnbreakable").getAsBoolean();
        String potionType = jsonObject.has("potionType") ? jsonObject.get("potionType").getAsString() : null;
        int potionData = jsonObject.has("potionData") ? jsonObject.get("potionData").getAsInt() : 0;
        String author = jsonObject.has("author") ? jsonObject.get("author").getAsString() : null;
        String title = jsonObject.has("title") ? jsonObject.get("title").getAsString() : null;
        List<String> pages = jsonObject.has("pages") ? gson.fromJson(jsonObject.get("pages").getAsString(), List.class) : null;
        Map<Enchantment, Integer> storedEnchantments = jsonObject.has("storedEnchantments") ? gson.fromJson(jsonObject.get("storedEnchantments").getAsString(), Map.class) : null;
        String owner = jsonObject.has("owner") ? jsonObject.get("owner").getAsString() : null;
        DyeColor baseColor = jsonObject.has("baseColor") ? DyeColor.valueOf(jsonObject.get("baseColor").getAsString()) : null;
        List<Pattern> patterns = jsonObject.has("patterns") ? gson.fromJson(jsonObject.get("patterns").getAsString(), List.class) : null;
        List<FireworkEffect> fireworkEffect = jsonObject.has("fireworkEffect") ? gson.fromJson(jsonObject.get("fireworkEffect").getAsString(), List.class) : null;
        int fireworkPower = jsonObject.has("fireworkPower") ? jsonObject.get("fireworkPower").getAsInt() : 0;
        FireworkEffect fireworkEffect1 = jsonObject.has("fireworkEffect") ? gson.fromJson(jsonObject.get("fireworkEffect").getAsString(), FireworkEffect.class) : null;
        DyeColor bodyColor = jsonObject.has("bodyColor") ? DyeColor.valueOf(jsonObject.get("bodyColor").getAsString()) : null;
        TropicalFish.Pattern pattern = jsonObject.has("pattern") ? TropicalFish.Pattern.valueOf(jsonObject.get("pattern").getAsString()) : null;
        DyeColor patternColor = jsonObject.has("patternColor") ? DyeColor.valueOf(jsonObject.get("patternColor").getAsString()) : null;
        List<PotionEffect> potionEffects = jsonObject.has("potionEffects") ? gson.fromJson(jsonObject.get("potionEffects").getAsString(), List.class) : null;
        int color = jsonObject.has("color") ? jsonObject.get("color").getAsInt() : 0;
        String lodestone = jsonObject.has("lodestone") ? jsonObject.get("lodestone").getAsString() : null;
        List<ItemStack> chargedProjectiles = jsonObject.has("chargedProjectiles") ? gson.fromJson(jsonObject.get("chargedProjectiles").getAsString(), List.class) : null;

        ItemStack itemStack = new ItemStack(material, amount, durability);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (displayName != null) {
            itemMeta.displayName(Component.text(displayName));
        }

        if (lore != null) {
            ArrayList<String> loreList = new ArrayList<>();
            JsonArray jsonArray = JsonParser.parseString(lore).getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                loreList.add(jsonElement.getAsString());
            }
            itemMeta.setLore(loreList);
        }

        if (enchantments != null) {
            enchantments.forEach((enchantment, integer) -> itemMeta.addEnchant(enchantment, integer, true));
        }

        if (itemFlags != null) {
            itemFlags.forEach((itemFlag) -> itemMeta.addItemFlags(itemFlag));
        }

        if (customModelData != 0) {
            itemMeta.setCustomModelData(customModelData);
        }

        if (repairCost != 0) {
            itemStack.setRepairCost(repairCost);
        }

        if (isUnbreakable) {
            itemMeta.setUnbreakable(true);
        }

        if (attributes != null) {
            attributes.forEach(itemMeta::addAttributeModifier);
        }

        if (potionType != null) {
            itemStack = new ItemStack(Material.getMaterial(potionType), amount, (short) potionData);
        }

        if (author != null && title != null && pages != null) {
            BookMeta bookMeta = (BookMeta) itemMeta;
            bookMeta.setAuthor(author);
            bookMeta.setTitle(title);
            bookMeta.setPages(pages);
        }

        if (storedEnchantments != null) {
            EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) itemMeta;
            storedEnchantments.forEach((enchantment, integer) -> enchantmentStorageMeta.addStoredEnchant(enchantment, integer, true));
        }

        if (owner != null) {
            SkullMeta skullMeta = (SkullMeta) itemMeta;
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
        }

        if (baseColor != null && patterns != null) {
            BannerMeta bannerMeta = (BannerMeta) itemMeta;
            bannerMeta.setBaseColor(baseColor);
            patterns.forEach(bannerMeta::addPattern);
        }

        if (fireworkEffect != null) {
            FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;
            fireworkEffect.forEach(fireworkMeta::addEffect);
            fireworkMeta.setPower(fireworkPower);
        }

        if (fireworkEffect1 != null) {
            FireworkEffectMeta fireworkEffectMeta = (FireworkEffectMeta) itemMeta;
            fireworkEffectMeta.setEffect(fireworkEffect1);
        }

        if (bodyColor != null && pattern != null && patternColor != null) {
            TropicalFishBucketMeta tropicalFishBucketMeta = (TropicalFishBucketMeta) itemMeta;
            tropicalFishBucketMeta.setBodyColor(bodyColor);
            tropicalFishBucketMeta.setPattern(pattern);
            tropicalFishBucketMeta.setPatternColor(patternColor);
        }

        if (potionEffects != null) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            potionEffects.forEach(potionEffect -> potionMeta.addCustomEffect(potionEffect, true));
        }

        if (color != 0) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
            leatherArmorMeta.setColor(org.bukkit.Color.fromRGB(color));
        }

        if (lodestone != null) {
            CompassMeta compassMeta = (CompassMeta) itemMeta;
            compassMeta.setLodestone(LocationTranslator.fromString(lodestone));
        }

        if (chargedProjectiles != null) {
            CrossbowMeta crossbowMeta = (CrossbowMeta) itemMeta;
            chargedProjectiles.forEach(crossbowMeta::addChargedProjectile);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    /* NO LONGER USED
    //LINK https://github.com/brunyman/MysqlInventoryBridge/blob/master/Mysql%20Inventory%20Bridge/src/net/craftersland/bridge/inventory/InventoryUtils.java
    //AUTHOR brunyman

    public String toBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(inventory.getSize());

            // Save every element in the list
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public Inventory fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }

            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    public String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(items.length);

            // Save every element in the list
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }*/

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
}
