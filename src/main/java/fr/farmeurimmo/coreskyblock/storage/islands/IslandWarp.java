package fr.farmeurimmo.coreskyblock.storage.islands;

import com.google.gson.JsonObject;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandWarpCategories;
import fr.farmeurimmo.coreskyblock.utils.LocationTranslator;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.UUID;

public class IslandWarp {

    private final UUID uuid;
    private final UUID islandUUID;
    private final ArrayList<IslandWarpCategories> categories;
    private String name;
    private String description;
    private Location location;
    private boolean isActivated;
    private long forwardedWarp;

    public IslandWarp(UUID uuid, UUID islandUUID, String name, String description, ArrayList<IslandWarpCategories> categories,
                      Location location, boolean isActivated, long forwardedWarp) {
        this.uuid = uuid;
        this.islandUUID = islandUUID;
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.location = location;
        this.isActivated = isActivated;
        this.forwardedWarp = forwardedWarp;
    }

    public static IslandWarp fromJson(JsonObject jsonObject) {
        UUID uuid = UUID.fromString(jsonObject.get("uuid").getAsString());
        UUID islandUUID = UUID.fromString(jsonObject.get("islandUUID").getAsString());
        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();
        Location location = LocationTranslator.fromString(jsonObject.get("location").getAsString());
        boolean isActivated = jsonObject.get("isActivated").getAsBoolean();
        String[] categories = jsonObject.get("categories").getAsString().split(",");
        ArrayList<IslandWarpCategories> islandWarpCategories = new ArrayList<>();
        for (String category : categories) {
            islandWarpCategories.add(IslandWarpCategories.getById(Integer.parseInt(category)));
        }
        long forwardedWarp = jsonObject.get("forwardedWarp").getAsLong();
        return new IslandWarp(uuid, islandUUID, name, description, islandWarpCategories, location, isActivated, forwardedWarp);
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

    public void update() {
        // Update the warp in the cache dans in the database
    }

    public String getCategoriesString() {
        StringBuilder categories = new StringBuilder();
        for (IslandWarpCategories category : this.categories) {
            categories.append(category.getName()).append(", ");
        }
        return categories.toString();
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
        return IslandsManager.INSTANCE.gson.toJson(jsonObject);
    }

}
