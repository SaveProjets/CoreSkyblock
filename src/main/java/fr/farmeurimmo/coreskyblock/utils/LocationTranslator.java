package fr.farmeurimmo.coreskyblock.utils;

import org.bukkit.Location;

import java.text.NumberFormat;

public class LocationTranslator {

    public static Location fromString(String locationString) {
        String[] split = locationString.split(";");
        return new Location(null, Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]),
                Float.parseFloat(split[3]), Float.parseFloat(split[4]));
    }

    public static String fromLocation(Location location) {
        return location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch();
    }

    public static String readableLocation(Location location) {
        return "X: " + NumberFormat.getInstance().format(location.getX()) + ", Y: " + NumberFormat.getInstance()
                .format(location.getY()) + ", Z: " + NumberFormat.getInstance().format(location.getZ()) +
                ", Yaw: " + location.getYaw() + ", Pitch: " + location.getPitch();
    }
}
