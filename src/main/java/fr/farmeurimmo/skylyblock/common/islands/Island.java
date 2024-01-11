package fr.farmeurimmo.skylyblock.common.islands;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Island {

    private final UUID islandUUID;
    private final Map<UUID, IslandRanks> members;
    private final Map<IslandRanks, ArrayList<IslandPerms>> perms;
    private final ArrayList<UUID> bannedPlayers;
    private String name;
    private Location spawn;
    private int maxSize;
    private int maxMembers;
    private int generatorLevel;
    private double bankMoney;
    private double crystalMoney;
    private boolean isPublic;
    private double level;
    private double levelExp;

    public Island(UUID islandUUID, String name, Location spawn, Map<UUID, IslandRanks> members, Map<IslandRanks,
            ArrayList<IslandPerms>> perms, int maxSize, int maxMembers, int generatorLevel, double bankMoney,
                  double crystalMoney, ArrayList<UUID> bannedPlayers, boolean isPublic, double level, double levelExp) {
        this.islandUUID = islandUUID;
        this.name = name;
        this.spawn = spawn;
        this.members = members;
        this.perms = perms;
        this.maxSize = maxSize;
        this.maxMembers = maxMembers;
        this.generatorLevel = generatorLevel;
        this.bankMoney = bankMoney;
        this.crystalMoney = crystalMoney;
        this.bannedPlayers = bannedPlayers;
        this.isPublic = isPublic;
        this.level = level;
        this.levelExp = levelExp;
    }

    //default just the necessary to establish a start island
    public Island(UUID islandUUID, Location spawn, UUID owner) {
        this.islandUUID = islandUUID;
        this.name = "Nom par défaut";
        this.spawn = spawn;
        this.members = new HashMap<>();
        addMember(owner, IslandRanks.CHEF);
        this.perms = new HashMap<>();
        this.maxSize = 40;
        this.maxMembers = 4;
        this.generatorLevel = 1;
        this.bankMoney = 0;
        this.crystalMoney = 0;
        this.bannedPlayers = new ArrayList<>();
        this.isPublic = true;
        this.level = 1;
        this.levelExp = 0;
    }

    public UUID getIslandUUID() {
        return this.islandUUID;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getSpawn() {
        return this.spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public Map<UUID, IslandRanks> getMembers() {
        return this.members;
    }

    public Map<IslandRanks, ArrayList<IslandPerms>> getPerms() {
        return this.perms;
    }

    public int getMaxSize() {
        return this.maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxMembers() {
        return this.maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public int getGeneratorLevel() {
        return this.generatorLevel;
    }

    public void setGeneratorLevel(int generatorLevel) {
        this.generatorLevel = generatorLevel;
    }

    public double getBankMoney() {
        return this.bankMoney;
    }

    public void setBankMoney(double bankMoney) {
        this.bankMoney = bankMoney;
    }

    public double getCrystalMoney() {
        return this.crystalMoney;
    }

    public void setCrystalMoney(double crystalMoney) {
        this.crystalMoney = crystalMoney;
    }

    public ArrayList<UUID> getBannedPlayers() {
        return this.bannedPlayers;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public double getLevel() {
        return this.level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public double getLevelExp() {
        return this.levelExp;
    }

    public void setLevelExp(double levelExp) {
        this.levelExp = levelExp;
    }

    public void addMember(UUID uuid, IslandRanks rank) {
        this.members.put(uuid, rank);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
    }
}
