package fr.farmeurimmo.coreskyblock.storage.islands;

import com.google.gson.JsonObject;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandWarpCategories;
import fr.farmeurimmo.coreskyblock.utils.LocationTranslator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandWarp {

    private final UUID uuid;
    private final UUID islandUUID;
    private final ArrayList<IslandWarpCategories> categories;
    private String name;
    private String description;
    private Location location;
    private boolean isActivated;
    private long forwardedWarp;
    private Material material;

    public IslandWarp(UUID uuid, UUID islandUUID, String name, String description, ArrayList<IslandWarpCategories> categories,
                      Location location, boolean isActivated, long forwardedWarp, Material material) {
        this.uuid = uuid;
        this.islandUUID = islandUUID;
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.location = location;
        this.isActivated = isActivated;
        this.forwardedWarp = forwardedWarp;
        this.material = material;
    }

    public IslandWarp(UUID islandUUID, String creator, Location location, boolean create) {
        this(UUID.randomUUID(), islandUUID, "Warp de l'île de " + creator, "Aucune description renseignée",
                new ArrayList<>(), location, false, 0, Material.GRASS_BLOCK);

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
        return new IslandWarp(uuid, islandUUID, name, description, islandWarpCategories, location, isActivated,
                forwardedWarp, material);
    }

    public static ArrayList<IslandWarpCategories> getCategoriesFromString(String categories) {
        ArrayList<IslandWarpCategories> islandWarpCategories = new ArrayList<>();
        for (String category : categories.split(";")) {
            islandWarpCategories.add(IslandWarpCategories.getById(Integer.parseInt(category)));
        }
        return islandWarpCategories;
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

}
