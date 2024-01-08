package fr.farmeurimmo.skylyblock.common;

public enum ServerType {

    SKYBLOCK_SPAWN("spawn"),
    SKYBLOCK_ISLAND("ile");

    private final String serverName;

    ServerType(String serverName) {
        this.serverName = serverName;
    }

    public static ServerType getDefault() {
        return SKYBLOCK_SPAWN;
    }

    public static ServerType isSimilar(String serverName) {
        for (ServerType serverType : values()) {
            if (serverType.getServerName().toLowerCase().equalsIgnoreCase(serverName)) return serverType;
        }
        return null;
    }

    public String getServerName() {
        return serverName;
    }
}
