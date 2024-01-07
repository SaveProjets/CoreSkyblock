package fr.farmeurimmo.skylyblock.common;

import java.util.UUID;

public class SkyblockUser {

    private final UUID uuid;
    private final String name;
    private double money;
    private double adventureExp;
    private double adventureLevel;
    private int flyTime;
    private int hasteLevel;
    private int speedLevel;
    private int jumpLevel;
    private boolean hasteActive;
    private boolean speedActive;
    private boolean jumpActive;

    public SkyblockUser(UUID uuid, String name, double money, double adventureExp, double adventureLevel, int flyTime,
                        int hasteLevel, int speedLevel, int jumpLevel, boolean hasteActive, boolean speedActive,
                        boolean jumpActive) {
        this.uuid = uuid;
        this.name = name;
        this.money = money;
        this.adventureExp = adventureExp;
        this.adventureLevel = adventureLevel;
        this.flyTime = flyTime;
        this.hasteLevel = hasteLevel;
        this.speedLevel = speedLevel;
        this.jumpLevel = jumpLevel;
        this.hasteActive = hasteActive;
        this.speedActive = speedActive;
        this.jumpActive = jumpActive;
    }

    public SkyblockUser(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.money = 0;
        this.adventureExp = 0;
        this.adventureLevel = 0;
        this.flyTime = 0;
        this.hasteLevel = 0;
        this.speedLevel = 0;
        this.jumpLevel = 0;
        this.hasteActive = false;
        this.speedActive = false;
        this.jumpActive = false;
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
    }

    public double getAdventureExp() {
        return adventureExp;
    }

    public void setAdventureExp(double adventureExp) {
        this.adventureExp = adventureExp;
    }

    public double getAdventureLevel() {
        return adventureLevel;
    }

    public void setAdventureLevel(double adventureLevel) {
        this.adventureLevel = adventureLevel;
    }

    public int getFlyTime() {
        return flyTime;
    }

    public void setFlyTime(int flyTime) {
        this.flyTime = flyTime;
    }

    public int getHasteLevel() {
        return hasteLevel;
    }

    public void setHasteLevel(int hasteLevel) {
        this.hasteLevel = hasteLevel;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public void setSpeedLevel(int speedLevel) {
        this.speedLevel = speedLevel;
    }

    public int getJumpLevel() {
        return jumpLevel;
    }

    public void setJumpLevel(int jumpLevel) {
        this.jumpLevel = jumpLevel;
    }

    public boolean isHasteActive() {
        return hasteActive;
    }

    public void setHasteActive(boolean hasteActive) {
        this.hasteActive = hasteActive;
    }

    public boolean isSpeedActive() {
        return speedActive;
    }

    public void setSpeedActive(boolean speedActive) {
        this.speedActive = speedActive;
    }

    public boolean isJumpActive() {
        return jumpActive;
    }

    public void setJumpActive(boolean jumpActive) {
        this.jumpActive = jumpActive;
    }

    public void addMoney(double money) {
        this.money += money;
    }

}
