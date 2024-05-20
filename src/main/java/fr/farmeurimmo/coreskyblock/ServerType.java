package fr.farmeurimmo.coreskyblock;

public enum ServerType {

    SPAWN("spawn"),
    GAME("game"),
    PVE("pve"),
    PVP("pvp");

    private final String name;

    ServerType(String name) {
        this.name = name;
    }

    public static ServerType getByName(String name) {
        for (ServerType type : values()) {
            if (name.contains(type.getName())) {
                return type;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

}
