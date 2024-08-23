package fr.farmeurimmo.coreskyblock.storage.skyblockusers;

import com.google.gson.JsonObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkyblockUser {

    private final UUID uuid;
    private final String name;
    private double money;
    private double adventureExp;
    private double adventureLevel;
    private int flyTime;
    private int currentPrestigeLevel;
    private int lastPrestigeLevelClaimed;
    private int currentPremiumPrestigeLevel;
    private int lastPremiumPrestigeLevelClaimed;
    private boolean ownPremiumPrestige;
    private String lastSpecialBooks;
    private int pvpKills;
    private int pvpDeaths;
    private int pveDeaths;

    private boolean modified = false;

    public SkyblockUser(UUID uuid, String name, double money, double adventureExp, double adventureLevel, int flyTime,
                        int currentPrestigeLevel, int lastPrestigeLevelClaimed, int currentPremiumPrestigeLevel,
                        int lastPremiumPrestigeLevelClaimed, boolean ownPremiumPrestige, String lastSpecialBooks,
                        int pvpKills, int pvpDeaths, int pveDeaths) {
        this.uuid = uuid;
        this.name = name;
        this.money = money;
        this.adventureExp = adventureExp;
        this.adventureLevel = adventureLevel;
        this.flyTime = flyTime;
        this.currentPrestigeLevel = currentPrestigeLevel;
        this.lastPrestigeLevelClaimed = lastPrestigeLevelClaimed;
        this.currentPremiumPrestigeLevel = currentPremiumPrestigeLevel;
        this.lastPremiumPrestigeLevelClaimed = lastPremiumPrestigeLevelClaimed;
        this.ownPremiumPrestige = ownPremiumPrestige;
        this.lastSpecialBooks = lastSpecialBooks;
        this.pvpKills = pvpKills;
        this.pvpDeaths = pvpDeaths;
        this.pveDeaths = pveDeaths;
    }

    public SkyblockUser(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.money = 0;
        this.adventureExp = 0;
        this.adventureLevel = 0;
        this.flyTime = 0;
        this.currentPrestigeLevel = 0;
        this.lastPrestigeLevelClaimed = 0;
        this.currentPremiumPrestigeLevel = 0;
        this.lastPremiumPrestigeLevelClaimed = 0;
        this.ownPremiumPrestige = false;
        this.lastSpecialBooks = "";
        this.pvpKills = 0;
        this.pvpDeaths = 0;
        this.pveDeaths = 0;
    }

    public static SkyblockUser fromJson(JsonObject json) {
        try {
            UUID uuid = UUID.fromString(json.get("uuid").getAsString());
            String name = json.get("name").getAsString();
            double money = json.get("money").getAsDouble();
            double adventureExp = json.get("advExp").getAsDouble();
            double adventureLevel = json.get("advLevel").getAsDouble();
            int flyTime = json.get("flyTime").getAsInt();
            int currentPrestigeLevel = json.get("cPL").getAsInt();
            int lastPrestigeLevelClaimed = json.get("lPLC").getAsInt();
            int currentPremiumPrestigeLevel = json.get("cPPL").getAsInt();
            int lastPremiumPrestigeLevelClaimed = json.get("lPPLC").getAsInt();
            boolean ownPremiumPrestige = json.get("ownPP").getAsBoolean();
            String lastSpecialBooks = json.get("lSP").getAsString();
            int pvpKills = json.get("pvpKills").getAsInt();
            int pvpDeaths = json.get("pvpDeaths").getAsInt();
            int pveDeaths = json.get("pveDeaths").getAsInt();

            return new SkyblockUser(uuid, name, money, adventureExp, adventureLevel, flyTime, currentPrestigeLevel,
                    lastPrestigeLevelClaimed, currentPremiumPrestigeLevel, lastPremiumPrestigeLevelClaimed,
                    ownPremiumPrestige, lastSpecialBooks, pvpKills, pvpDeaths, pveDeaths);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", uuid.toString());
        json.addProperty("name", name);
        json.addProperty("money", money);
        json.addProperty("advExp", adventureExp);
        json.addProperty("advLevel", adventureLevel);
        json.addProperty("flyTime", flyTime);
        json.addProperty("cPL", currentPrestigeLevel);
        json.addProperty("lPLC", lastPrestigeLevelClaimed);
        json.addProperty("cPPL", currentPremiumPrestigeLevel);
        json.addProperty("lPPLC", lastPremiumPrestigeLevelClaimed);
        json.addProperty("ownPP", ownPremiumPrestige);
        json.addProperty("lSP", lastSpecialBooks);
        json.addProperty("pvpKills", pvpKills);
        json.addProperty("pvpDeaths", pvpDeaths);
        json.addProperty("pveDeaths", pveDeaths);

        return json;
    }

    public int getCurrentPrestigeLevel() {
        return currentPrestigeLevel;
    }

    public void incrementCurrentPrestigeLevel() {
        currentPrestigeLevel++;
        currentPremiumPrestigeLevel++;

        update();
    }

    public int getLastPrestigeLevelClaimed() {
        return lastPrestigeLevelClaimed;
    }

    public void setLastPrestigeLevelClaimed(int lastPrestigeLevelClaimed) {
        this.lastPrestigeLevelClaimed = lastPrestigeLevelClaimed;

        update();
    }

    public int getCurrentPremiumPrestigeLevel() {
        return currentPremiumPrestigeLevel;
    }

    public int getLastPremiumPrestigeLevelClaimed() {
        return lastPremiumPrestigeLevelClaimed;
    }

    public void setLastPremiumPrestigeLevelClaimed(int lastPremiumPrestigeLevelClaimed) {
        this.lastPremiumPrestigeLevelClaimed = lastPremiumPrestigeLevelClaimed;

        update();
    }

    public boolean ownPremiumPrestige() {
        return ownPremiumPrestige;
    }

    public void setOwnPremiumPrestige(boolean ownPremiumPrestige) {
        this.ownPremiumPrestige = ownPremiumPrestige;

        update();
    }

    public String getLastSpecialBooks() {
        return lastSpecialBooks;
    }

    public void setLastSpecialBooks(String lastSpecialBooks) {
        this.lastSpecialBooks = lastSpecialBooks;

        update();
    }

    public int getPvpKills() {
        return pvpKills;
    }

    public void incrementPvpKills() {
        pvpKills++;

        setModified(true);
    }

    public int getPvpDeaths() {
        return pvpDeaths;
    }

    public void incrementPvpDeaths() {
        pvpDeaths++;

        setModified(true);
    }

    public int getPveDeaths() {
        return pveDeaths;
    }

    public void incrementPveDeaths() {
        pveDeaths++;

        setModified(true);
    }


}
