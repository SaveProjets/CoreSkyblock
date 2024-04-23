package fr.farmeurimmo.coreskyblock.storage.islands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsWarpManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandWarpCategories;
import fr.farmeurimmo.coreskyblock.utils.LocationTranslator;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandWarp {

    private final UUID uuid;
    private final UUID islandUUID;
    private final ArrayList<IslandWarpCategories> categories;
    private final Map<UUID, Pair<Integer, Long>> raters = new HashMap<>();
    private String name;
    private String description;
    private Location location;
    private boolean isActivated;
    private long forwardedWarp;
    private Material material;
    private double rate;

    public IslandWarp(UUID uuid, UUID islandUUID, String name, String description, ArrayList<IslandWarpCategories> categories,
                      Location location, boolean isActivated, long forwardedWarp, Material material, double rate,
                      Map<UUID, Pair<Integer, Long>> raters) {
        this.uuid = uuid;
        this.islandUUID = islandUUID;
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.location = location;
        this.isActivated = isActivated;
        this.forwardedWarp = forwardedWarp;
        this.material = material;
        this.rate = rate;
        this.raters.putAll(raters);
    }

    public IslandWarp(UUID islandUUID, String creator, Location location, boolean create) {
        this(UUID.randomUUID(), islandUUID, "Warp de l'île de " + creator, "Aucune description renseignée",
                new ArrayList<>(), location, false, 0, Material.GRASS_BLOCK, 100,
                new HashMap<>());

        if (create) update();
    }

    public static IslandWarp fromJson(JsonObject jsonObject) {
        UUID uuid = UUID.fromString(jsonObject.get("uuid").getAsString());
        UUID islandUUID = UUID.fromString(jsonObject.get("islandUUID").getAsString());
        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();
        Location location = LocationTranslator.fromString(jsonObject.get("location").getAsString());
        boolean isActivated = jsonObject.get("isActivated").getAsBoolean();
        String categories = jsonObject.get("categories").getAsString();
        ArrayList<IslandWarpCategories> islandWarpCategories = new ArrayList<>();
        if (categories != null && !categories.isEmpty()) {
            islandWarpCategories = getCategoriesFromString(categories);
        }
        long forwardedWarp = jsonObject.get("forwardedWarp").getAsLong();
        Material material = Material.getMaterial(jsonObject.get("material").getAsString());
        double rate = jsonObject.get("rate").getAsDouble();
        Map<UUID, Pair<Integer, Long>> raters = getRatersFromJson(jsonObject.get("raters"));
        return new IslandWarp(uuid, islandUUID, name, description, islandWarpCategories, location, isActivated,
                forwardedWarp, material, rate, raters);
    }

    public static ArrayList<IslandWarpCategories> getCategoriesFromString(String categories) {
        ArrayList<IslandWarpCategories> islandWarpCategories = new ArrayList<>();
        for (String category : categories.split(";")) {
            islandWarpCategories.add(IslandWarpCategories.getById(Integer.parseInt(category)));
        }
        return islandWarpCategories;
    }

    public static Map<UUID, Pair<Integer, Long>> getRatersFromJson(JsonElement jsonElement) {
        Map<UUID, Pair<Integer, Long>> raters = new HashMap<>();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            UUID uuid = UUID.fromString(entry.getKey());
            JsonObject object = entry.getValue().getAsJsonObject();
            int rate = object.get("rate").getAsInt();
            long time = object.get("time").getAsLong();
            raters.put(uuid, Pair.of(rate, time));
        }
        return raters;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getIslandUUID() {
        return islandUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        update();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;

        update();
    }

    public ArrayList<IslandWarpCategories> getCategories() {
        return categories;
    }

    public Location getLocation() {
        if (location.getWorld() == null) {
            World world = IslandsManager.INSTANCE.getIslandWorld(islandUUID);
            if (world == null) return null;
            location.setWorld(world);
        }
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;

        update();
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;

        update();
    }

    public void addCategory(IslandWarpCategories category) {
        categories.add(category);

        update();
    }

    public void removeCategory(IslandWarpCategories category) {
        categories.remove(category);

        update();
    }

    public long getForwardedWarp() {
        return forwardedWarp;
    }

    public void setForwardedWarp(long forwardedWarp) {
        this.forwardedWarp = forwardedWarp;

        update();
    }

    public boolean isStillForwarded() {
        return forwardedWarp > System.currentTimeMillis();
    }

    public boolean isInCooldownForForward() { // Cooldown of 1 day
        return forwardedWarp + 86_400_000 > System.currentTimeMillis();
    }

    public void update() {
        // Update the warp in the cache and in the database
        CompletableFuture.runAsync(() -> {
            JedisManager.INSTANCE.sendToRedis("coreskyblock:island:warp:" + islandUUID, toJson());
            JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:warp_update:" + islandUUID + ":" + CoreSkyblock.SERVER_NAME);

            IslandsDataManager.INSTANCE.updateIslandWarp(this);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uuid", uuid.toString());
        jsonObject.addProperty("islandUUID", islandUUID.toString());
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("location", LocationTranslator.fromLocation(location));
        jsonObject.addProperty("isActivated", isActivated);
        jsonObject.addProperty("categories", getCategoriesString());
        jsonObject.addProperty("forwardedWarp", forwardedWarp);
        jsonObject.addProperty("material", material.name());
        jsonObject.addProperty("rate", rate);
        jsonObject.add("raters", getRatersJSON());

        return IslandsManager.INSTANCE.gson.toJson(jsonObject);
    }

    public String getCategoriesString() {
        StringBuilder categories = new StringBuilder();
        for (IslandWarpCategories category : this.categories) {
            categories.append(category.getId()).append(";");
        }
        return (categories.isEmpty() ? categories.toString() : categories.substring(0, categories.length() - 1));
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;

        update();
    }

    public double getRate() {
        return rate;
    }

    public void applyRate(@NotNull UUID uniqueId, double rate) {
        this.rate += rate;
        addRater(uniqueId, (int) rate);

        Island island = IslandsManager.INSTANCE.getIslandByUUID(islandUUID);
        if (island != null) {
            island.sendMessageToAll("§e" + Bukkit.getOfflinePlayer(uniqueId).getName() + " §7a noté le warp comme " +
                    IslandsWarpManager.INSTANCE.getRateName((int) rate) + " §7(Nouvelle note: " + this.rate + ")");
        }

        update();
    }

    public JsonElement getRatersJSON() {
        return IslandsManager.INSTANCE.gson.toJsonTree(raters);
    }

    public void addRater(UUID uuid, int rate) {
        if (!raters.containsKey(uuid)) {
            raters.put(uuid, Pair.of(rate, System.currentTimeMillis()));
        } else {
            raters.computeIfPresent(uuid, (k, pair) -> Pair.of((pair.left() + rate), System.currentTimeMillis()));
        }

        update();
    }

    public long timeBeforeNextRate(UUID uuid) {
        if (!raters.containsKey(uuid)) return -1;
        return (raters.get(uuid).right() + 86_400_000 * 7) - System.currentTimeMillis();
    }

    public boolean canRate(UUID uuid) { // 7 days cooldown
        return (timeBeforeNextRate(uuid) <= 0);
    }

    public final Map<UUID, Pair<Integer, Long>> getRaters() {
        return raters;
    }

}
