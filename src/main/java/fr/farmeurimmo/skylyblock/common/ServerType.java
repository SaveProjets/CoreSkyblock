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

    public String getServerName() {
        return serverName;
    }
}
