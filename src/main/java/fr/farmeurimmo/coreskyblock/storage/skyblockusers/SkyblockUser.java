package fr.farmeurimmo.coreskyblock.storage.skyblockusers;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkyblockUser {

    private final UUID uuid;
    private final String name;
    private double money;
    private double adventureExp;
    private double adventureLevel;
    private int flyTime;

    private boolean modified = false;

    public SkyblockUser(UUID uuid, String name, double money, double adventureExp, double adventureLevel, int flyTime) {
        this.uuid = uuid;
        this.name = name;
        this.money = money;
        this.adventureExp = adventureExp;
        this.adventureLevel = adventureLevel;
        this.flyTime = flyTime;
    }

    public SkyblockUser(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.money = 0;
        this.adventureExp = 0;
        this.adventureLevel = 0;
        this.flyTime = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;

        update();
    }

    public double getAdventureExp() {
        return adventureExp;
    }

    public void setAdventureExp(double adventureExp) {
        this.adventureExp = adventureExp;

        setModified(true);
    }

    public double getAdventureLevel() {
        return adventureLevel;
    }

    public void setAdventureLevel(double adventureLevel) {
        this.adventureLevel = adventureLevel;

        setModified(true);
    }

    public int getFlyTime() {
        return flyTime;
    }

    public void setFlyTime(int flyTime) {
        this.flyTime = flyTime;

        setModified(true);
    }

    public void addMoney(double money) {
        this.money += money;

        update();
    }

    public void removeMoney(double money) {
        this.money -= money;

        update();
    }

    public void update() {
        CompletableFuture.runAsync(() -> SkyblockUsersManager.INSTANCE.upsertUser(this));

        setModified(false);
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

}
