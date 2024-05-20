package fr.farmeurimmo.coreskyblock.purpur.islands;

import java.util.UUID;

public class CoopPlayer {

    private final UUID player;
    private final UUID coop;
    private long lastCheckSuccessPlayer;
    private long lastCheckSuccessCoop;

    public CoopPlayer(UUID player, UUID coop) {
        this.player = player;
        this.coop = coop;
        this.lastCheckSuccessPlayer = System.currentTimeMillis();
        this.lastCheckSuccessCoop = System.currentTimeMillis();
    }

    public UUID getPlayer() {
        return player;
    }

    public UUID getCoop() {
        return coop;
    }

    public long getLastCheckSuccessPlayer() {
        return lastCheckSuccessPlayer;
    }

    public void setLastCheckSuccessPlayer(long lastCheckSuccessPlayer) {
        this.lastCheckSuccessPlayer = lastCheckSuccessPlayer;
    }

    public long getLastCheckSuccessCoop() {
        return lastCheckSuccessCoop;
    }

    public void setLastCheckSuccessCoop(long lastCheckSuccessCoop) {
        this.lastCheckSuccessCoop = lastCheckSuccessCoop;
    }
}
