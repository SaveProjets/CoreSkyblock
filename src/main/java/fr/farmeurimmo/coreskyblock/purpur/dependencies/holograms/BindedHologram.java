package fr.farmeurimmo.coreskyblock.purpur.dependencies.holograms;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class BindedHologram {

    private final double defaultDistance;
    private final boolean shouldRemoveOnDeath;
    private Hologram hologram;
    private Entity entity;
    private Location loc;
    private long expiration;

    public BindedHologram(Hologram hologram, Entity entity, Location loc, long expiration, boolean shouldRemoveOnDeath) {
        this.hologram = hologram;
        this.entity = entity;
        this.loc = entity.getLocation().clone().subtract(loc);
        this.expiration = expiration;
        defaultDistance = loc.distance(entity.getLocation());
        this.shouldRemoveOnDeath = shouldRemoveOnDeath;
    }

    public boolean shouldRemoveOnDeath() {
        return shouldRemoveOnDeath;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public double getDefaultDistance() {
        return defaultDistance;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
