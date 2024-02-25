package fr.farmeurimmo.coreskyblock.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class InventorySyncUtils {

    private static final int SIZE = 41; // 41 is the max size of a player inventory
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

    public ItemStack[] jsonToInventory(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        ItemStack[] itemStacks = new ItemStack[SIZE];
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
        ItemMeta itemMeta = itemStack.getItemMeta();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", itemStack.getType().name());
        if (itemStack.getAmount() > 1)
            jsonObject.addProperty("amount", itemStack.getAmount());
        if (itemStack.hasDamage())
            jsonObject.addProperty("durability", itemStack.getDurability());
        if (itemStack.hasDisplayName())
            jsonObject.addProperty("displayName", itemStack.getDisplayName());
        if (itemStack.hasLore())
            jsonObject.addProperty("lore", Optional.ofNullable(itemStack.getItemMeta()).map(ItemMeta::getLore).map(gson::toJson).orElse(""));

        if (itemStack.hasEnchants())
            jsonObject.addProperty("enchantments", gson.toJson(itemStack.getEnchantments()));

        if (!itemStack.getItemFlags().isEmpty())
            jsonObject.addProperty("itemFlags", gson.toJson(itemStack.getItemMeta().getItemFlags()));

        if (itemStack.hasCustomModelData())
            jsonObject.addProperty("customModelData", itemStack.getCustomModelData());

        if (itemStack.hasRepairCost())
            jsonObject.addProperty("repairCost", itemStack.getRepairCost());
        if (itemStack.isUnbreakable())
            jsonObject.addProperty("isUnbreakable", itemStack.getItemMeta().isUnbreakable());

        if (itemStack.hasAttributeModifiers())
            jsonObject.addProperty("attributes", gson.toJson(itemStack.getItemMeta().getAttributeModifiers()));

        if (itemMeta instanceof BookMeta bookMeta) {
            jsonObject.addProperty("author", bookMeta.getAuthor());
            jsonObject.addProperty("title", bookMeta.getTitle());
            jsonObject.addProperty("pages", gson.toJson(bookMeta.getPages()));
        }

        if (itemMeta instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
            if (!enchantmentStorageMeta.getStoredEnchants().isEmpty()) {
                List<JsonObject> result = new ArrayList<>();
                for (Map.Entry<Enchantment, Integer> entry : enchantmentStorageMeta.getStoredEnchants().entrySet()) {
                    JsonObject enchantmentJson = new JsonObject();
                    enchantmentJson.addProperty("enchantment", entry.getKey().getKey().getKey());
                    enchantmentJson.addProperty("level", entry.getValue());
                    result.add(enchantmentJson);
                }
                jsonObject.addProperty("storedEnchantments", gson.toJson(result));
            }
        }

        if (itemMeta instanceof SkullMeta skullMeta) {
            jsonObject.addProperty("owner", skullMeta.getOwningPlayer().getName());
        }

        if (itemMeta instanceof BannerMeta bannerMeta) {
            if (bannerMeta.getBaseColor() != null) {
                jsonObject.addProperty("baseColor", bannerMeta.getBaseColor().name());
            }
            jsonObject.addProperty("patterns", gson.toJson(bannerMeta.getPatterns()));
        }

        if (itemMeta instanceof FireworkMeta) {
            jsonObject.addProperty("fireworkEffect", gson.toJson(((FireworkMeta) itemMeta).getEffects()));
            jsonObject.addProperty("fireworkPower", ((FireworkMeta) itemMeta).getPower());
        }

        if (itemMeta instanceof FireworkEffectMeta fireworkEffectMeta) {
            jsonObject.addProperty("fireworkEffect", gson.toJson(fireworkEffectMeta.getEffect()));
        }

        if (itemMeta instanceof TropicalFishBucketMeta tropicalFishBucketMeta) {
            jsonObject.addProperty("bodyColor", tropicalFishBucketMeta.getBodyColor().name());
            jsonObject.addProperty("pattern", tropicalFishBucketMeta.getPattern().name());
            jsonObject.addProperty("patternColor", tropicalFishBucketMeta.getPatternColor().name());
        }

        if (itemMeta instanceof PotionMeta pM) {
            if (pM.hasCustomEffects()) {
                jsonObject.addProperty("potionEffects", gson.toJson(pM.getCustomEffects()));
            }
            jsonObject.addProperty("potionBaseType", pM.getBasePotionType().name());
        }

        if (itemMeta instanceof LeatherArmorMeta leatherArmorMeta) {
            jsonObject.addProperty("color", leatherArmorMeta.getColor().asRGB());
        }

        // We don't need to store the map data
        /*if (itemMeta instanceof MapMeta mapMeta) {
            jsonObject.addProperty("scaling", mapMeta.isScaling());
            jsonObject.addProperty("locationName", mapMeta.getLocationName());
            jsonObject.addProperty("mapId", mapMeta.getMapId());
        }*/

        if (itemMeta instanceof CompassMeta compassMeta) {
            jsonObject.addProperty("lodestone", LocationTranslator.fromLocation(Objects.requireNonNull(compassMeta.getLodestone())));
        }

        if (itemMeta instanceof CrossbowMeta crossbowMeta) {
            jsonObject.addProperty("chargedProjectiles", gson.toJson(crossbowMeta.getChargedProjectiles()));
        }

        if (itemMeta instanceof SpawnEggMeta spawnEggMeta) {
            if (spawnEggMeta.getCustomSpawnedType() != null) {
                jsonObject.addProperty("spawnedType", spawnEggMeta.getCustomSpawnedType().name());
            }
        }

        if (itemMeta instanceof SuspiciousStewMeta suspiciousStewMeta) {
            if (suspiciousStewMeta.hasCustomEffects()) {
                List<JsonObject> result = new ArrayList<>();
                for (PotionEffect effect : suspiciousStewMeta.getCustomEffects()) {
                    JsonObject effectJson = new JsonObject();
                    effectJson.addProperty("type", effect.getType().getName());
                    effectJson.addProperty("duration", effect.getDuration());
                    effectJson.addProperty("amplifier", effect.getAmplifier());
                    effectJson.addProperty("ambient", effect.isAmbient());
                    effectJson.addProperty("particles", effect.hasParticles());
                    effectJson.addProperty("icon", effect.hasIcon());
                    result.add(effectJson);
                }
                jsonObject.addProperty("suspiciousStewEffects", gson.toJson(result));
            }
        }

        if (itemStack.getType().name().contains("SHULKER_BOX")) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
            ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
            jsonObject.addProperty("shulkerBoxInventory", inventoryToJson(shulkerBox.getInventory()));
        }

        /*if (itemStack.getType().name().contains("PAINTING")) {
            // Clear the json object and only store the base64 of the item
            jsonObject = new JsonObject();
            jsonObject.addProperty("painting", itemStackToBase64(itemStack));
        }*/

        return jsonObject;
    }

    public ItemStack jsonToItemStack(JsonObject jsonObject) {
        if (jsonObject == null || jsonObject.isJsonNull()) {
            return null;
        }

        /*if (jsonObject.has("painting")) {
            return itemStackFromBase64(jsonObject.get("painting").getAsString());
        }*/

        Material material;
        try {
            material = Material.getMaterial(jsonObject.get("type").getAsString());
        } catch (Exception e) {
            return new ItemStack(Material.AIR);
        }
        int amount = (jsonObject.has("amount") ? jsonObject.get("amount").getAsInt() : 1);

        String displayName = (jsonObject.has("displayName") ? jsonObject.get("displayName").getAsString() : null);

        if (material == null) return new ItemStack(Material.AIR);

        ItemStack itemStack = new ItemStack(material, amount);
        if (displayName != null && !displayName.isEmpty())
            itemStack.editMeta(meta -> meta.displayName(Component.text(displayName)));
        ItemMeta itemMeta = itemStack.getItemMeta();

        Object[] keys = jsonObject.keySet().toArray();
        for (Object key : keys) {
            String keyStr = (String) key;
            if (keyStr.equals("slots")) continue; // We don't need to store the slots
            JsonElement value = jsonObject.get(keyStr);
            if (value.getAsString().isEmpty()) continue;
            switch (keyStr) {
                case "durability" -> itemStack.setDurability(value.getAsShort());
                case "lore" -> {
                    ArrayList<String> loreList = new ArrayList<>();
                    if (value.getAsString() != null) {
                        if (value.getAsString().isEmpty()) continue;
                        JsonArray jsonArray = JsonParser.parseString(value.getAsString()).getAsJsonArray();
                        for (JsonElement jsonElement : jsonArray) {
                            loreList.add(jsonElement.getAsString());
                        }
                        itemMeta.setLore(loreList);
                    }
                }
                case "enchantments" -> {
                    Map<Enchantment, Integer> enchantments = gson.fromJson(value.getAsString(), new TypeToken<Map<Enchantment, Integer>>() {
                    }.getType());
                    enchantments.forEach((enchantment, integer) -> itemMeta.addEnchant(enchantment, integer, true));
                }
                case "itemFlags" -> {
                    List<ItemFlag> itemFlags = gson.fromJson(value.getAsString(), new TypeToken<List<ItemFlag>>() {
                    }.getType());
                    itemFlags.forEach(itemMeta::addItemFlags);
                }
                case "customModelData" -> itemMeta.setCustomModelData(value.getAsInt());
                case "repairCost" -> itemStack.setRepairCost(value.getAsInt());
                case "isUnbreakable" -> itemMeta.setUnbreakable(value.getAsBoolean());
                case "attributes" -> {
                    Map<Attribute, AttributeModifier> attributes = gson.fromJson(value.getAsString(), new TypeToken<Map<Attribute, AttributeModifier>>() {
                    }.getType());
                    attributes.forEach(itemMeta::addAttributeModifier);
                }
                case "author" -> {
                    BookMeta bookMeta = (BookMeta) itemMeta;
                    bookMeta.setAuthor(value.getAsString());
                }
                case "title" -> {
                    BookMeta bookMeta = (BookMeta) itemMeta;
                    bookMeta.setTitle(value.getAsString());
                }
                case "pages" -> {
                    BookMeta bookMeta = (BookMeta) itemMeta;
                    bookMeta.setPages(gson.fromJson(value.getAsString(), List.class));
                }
                case "storedEnchantments" -> {
                    EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) itemMeta;
                    List<JsonObject> storedEnchantments = gson.fromJson(value.getAsString(), new TypeToken<List<JsonObject>>() {
                    }.getType());
                    for (JsonObject storedEnchantment : storedEnchantments) {
                        enchantmentStorageMeta.addStoredEnchant(
                                Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(
                                        storedEnchantment.get("enchantment").getAsString()))),
                                storedEnchantment.get("level").getAsInt(), true
                        );
                    }
                }
                case "owner" -> {
                    SkullMeta skullMeta = (SkullMeta) itemMeta;
                    skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(value.getAsString()));
                }
                case "baseColor" -> {
                    BannerMeta bannerMeta = (BannerMeta) itemMeta;
                    bannerMeta.setBaseColor(DyeColor.valueOf(value.getAsString()));
                }
                case "patterns" -> {
                    BannerMeta bannerMeta = (BannerMeta) itemMeta;
                    for (JsonElement jsonElement : JsonParser.parseString(value.getAsString()).getAsJsonArray()) {
                        JsonObject patternJson = jsonElement.getAsJsonObject();
                        bannerMeta.addPattern(new Pattern(DyeColor.valueOf(patternJson.get("color").getAsString()),
                                PatternType.valueOf(patternJson.get("pattern").getAsString())));
                    }
                }
                case "fireworkEffect" -> {
                    FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;
                    fireworkMeta.addEffect(gson.fromJson(value.getAsString(), FireworkEffect.class));
                }
                case "fireworkPower" -> {
                    FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;
                    fireworkMeta.setPower(value.getAsInt());
                }
                case "bodyColor" -> {
                    TropicalFishBucketMeta tropicalFishBucketMeta = (TropicalFishBucketMeta) itemMeta;
                    tropicalFishBucketMeta.setBodyColor(DyeColor.valueOf(value.getAsString()));
                }
                case "pattern" -> {
                    TropicalFishBucketMeta tropicalFishBucketMeta = (TropicalFishBucketMeta) itemMeta;
                    tropicalFishBucketMeta.setPattern(TropicalFish.Pattern.valueOf(value.getAsString()));
                }
                case "patternColor" -> {
                    TropicalFishBucketMeta tropicalFishBucketMeta = (TropicalFishBucketMeta) itemMeta;
                    tropicalFishBucketMeta.setPatternColor(DyeColor.valueOf(value.getAsString()));
                }
                case "potionEffects" -> {
                    PotionMeta potionMeta = (PotionMeta) itemMeta;
                    if (value.getAsString().length() > 2)
                        potionMeta.addCustomEffect(gson.fromJson(value.getAsString(), PotionEffect.class), true);
                }
                case "potionBaseType" -> {
                    PotionMeta potionMeta = (PotionMeta) itemMeta;
                    potionMeta.setBasePotionType(PotionType.valueOf(value.getAsString()));
                }
                case "color" -> {
                    LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
                    leatherArmorMeta.setColor(org.bukkit.Color.fromRGB(value.getAsInt()));
                }
                case "lodestone" -> {
                    CompassMeta compassMeta = (CompassMeta) itemMeta;
                    compassMeta.setLodestone(LocationTranslator.fromString(value.getAsString()));
                }
                case "chargedProjectiles" -> {
                    CrossbowMeta crossbowMeta = (CrossbowMeta) itemMeta;
                    crossbowMeta.addChargedProjectile(gson.fromJson(value.getAsString(), ItemStack.class));
                }
                case "spawnedType" -> {
                    SpawnEggMeta spawnEggMeta = (SpawnEggMeta) itemMeta;
                    spawnEggMeta.setCustomSpawnedType(EntityType.valueOf(value.getAsString()));
                }
                case "suspiciousStewEffects" -> {
                    SuspiciousStewMeta suspiciousStewMeta = (SuspiciousStewMeta) itemMeta;
                    if (value.getAsString().length() > 2) {
                        JsonArray jsonArray = JsonParser.parseString(value.getAsString()).getAsJsonArray();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject effectJson = jsonElement.getAsJsonObject();
                            suspiciousStewMeta.addCustomEffect(new PotionEffect(
                                    Objects.requireNonNull(PotionEffectType.getByName(effectJson.get("type").getAsString())),
                                    effectJson.get("duration").getAsInt(),
                                    effectJson.get("amplifier").getAsInt(),
                                    effectJson.get("ambient").getAsBoolean(),
                                    effectJson.get("particles").getAsBoolean(),
                                    effectJson.get("icon").getAsBoolean()
                            ), true);
                        }
                    }
                }
                case "shulkerBoxInventory" -> {
                    BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
                    ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                    shulkerBox.getInventory().setContents(jsonToInventory(value.getAsString()));
                    blockStateMeta.setBlockState(shulkerBox);
                }
            }
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
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

    public JsonObject potionEffectsToJson(PotionEffect[] potionEffects) {
        JsonObject jsonObject = new JsonObject();
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
        return jsonObject;
    }

    public String potionEffectsToStringJson(PotionEffect[] potionEffects) {
        if (potionEffects == null || potionEffects.length == 0) return "";
        return gson.toJson(potionEffects);
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
