package fr.farmeurimmo.skylyblock.common.islands;

import fr.farmeurimmo.skylyblock.common.IslandsDataManager;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    private double bankCrystals;
    private boolean isPublic;
    private double level;
    private double levelExp;

    private boolean isModified = false;
    private boolean areMembersModified = false;
    private boolean arePermsModified = false;
    private boolean areBannedPlayersModified = false;

    public Island(UUID islandUUID, String name, Location spawn, Map<UUID, IslandRanks> members, Map<IslandRanks,
            ArrayList<IslandPerms>> perms, int maxSize, int maxMembers, int generatorLevel, double bankMoney,
                  double bankCrystals, ArrayList<UUID> bannedPlayers, boolean isPublic, double level, double levelExp) {
        this.islandUUID = islandUUID;
        this.name = name;
        this.spawn = spawn;
        this.members = members;
        this.perms = perms;
        if (perms.isEmpty()) setDefaultPerms();
        this.maxSize = maxSize;
        this.maxMembers = maxMembers;
        this.generatorLevel = generatorLevel;
        this.bankMoney = bankMoney;
        this.bankCrystals = bankCrystals;
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
        this.members.put(owner, IslandRanks.CHEF); // this is an exception, please use the addMember method to trigger an update
        this.perms = new HashMap<>();
        setDefaultPerms();
        this.maxSize = 40;
        this.maxMembers = 4;
        this.generatorLevel = 1;
        this.bankMoney = 0;
        this.bankCrystals = 0;
        this.bannedPlayers = new ArrayList<>();
        this.isPublic = true;
        this.level = 1;
        this.levelExp = 0;
    }

    public void setDefaultPerms() {
        ArrayList<IslandPerms> permsVisit = new ArrayList<>();
        permsVisit.add(IslandPerms.INTERACT);
        perms.put(IslandRanks.VISITEUR, permsVisit);

        ArrayList<IslandPerms> perms = new ArrayList<>();
        perms.addAll(permsVisit);
        perms.addAll(Arrays.asList(IslandPerms.BUILD, IslandPerms.BREAK));
        this.perms.put(IslandRanks.COOP, perms);

        ArrayList<IslandPerms> permsMembre = new ArrayList<>();
        permsMembre.addAll(perms);
        permsMembre.addAll(Arrays.asList(IslandPerms.MINIONS_ADD, IslandPerms.MINIONS_INTERACT));
        this.perms.put(IslandRanks.MEMBRE, permsMembre);

        ArrayList<IslandPerms> permsMod = new ArrayList<>();
        permsMod.addAll(permsMembre);
        permsMod.addAll(Arrays.asList(IslandPerms.KICK, IslandPerms.PROMOTE, IslandPerms.DEMOTE, IslandPerms.MINIONS_REMOVE));
        this.perms.put(IslandRanks.MODERATEUR, permsMod);

        ArrayList<IslandPerms> permsCoChef = new ArrayList<>();
        permsCoChef.addAll(permsMod);
        permsCoChef.addAll(Arrays.asList(IslandPerms.KICK, IslandPerms.PROMOTE, IslandPerms.DEMOTE,
                IslandPerms.INVITE, IslandPerms.BAN));
        this.perms.put(IslandRanks.COCHEF, permsCoChef);

        ArrayList<IslandPerms> permsChef = new ArrayList<>();
        permsChef.add(IslandPerms.ALL_PERMS);
        this.perms.put(IslandRanks.CHEF, permsChef);

        arePermsModified = true;
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
        isModified = true;
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
        isModified = true;
    }

    public int getMaxMembers() {
        return this.maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
        isModified = true;
    }

    public int getGeneratorLevel() {
        return this.generatorLevel;
    }

    public void setGeneratorLevel(int generatorLevel) {
        this.generatorLevel = generatorLevel;
        isModified = true;
    }

    public double getBankMoney() {
        return this.bankMoney;
    }

    public void setBankMoney(double bankMoney) {
        this.bankMoney = bankMoney;
        update();
    }

    public double getBankCrystals() {
        return this.bankCrystals;
    }

    public void setCrystalMoney(double bankCrystals) {
        this.bankCrystals = bankCrystals;
        update();
    }

    public ArrayList<UUID> getBannedPlayers() {
        return this.bannedPlayers;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        update();
    }

    public double getLevel() {
        return this.level;
    }

    public void setLevel(double level) {
        this.level = level;
        update();
    }

    public double getLevelExp() {
        return this.levelExp;
    }

    public void setLevelExp(double levelExp) {
        this.levelExp = levelExp;
        isModified = true;
    }

    public void addMember(UUID uuid, IslandRanks rank) {
        this.members.put(uuid, rank);
        update();
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
        update();
    }

    public void update() {
        CompletableFuture.runAsync(() -> IslandsDataManager.INSTANCE.update(this, areMembersModified,
                arePermsModified, areBannedPlayersModified));

        isModified = false;
        areMembersModified = false;
        arePermsModified = false;
        areBannedPlayersModified = false;
    }

    public boolean needUpdate() {
        if (isModified) return true;
        if (areMembersModified) return true;
        if (arePermsModified) return true;
        return areBannedPlayersModified;
    }
}
