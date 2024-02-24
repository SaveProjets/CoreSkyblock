package fr.farmeurimmo.coreskyblock.common.islands;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsSizeManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Island {

    private final UUID islandUUID;
    private final Map<UUID, IslandRanks> members;
    private final Map<IslandRanks, ArrayList<IslandPerms>> perms;
    private final Map<UUID, String> membersNames = new HashMap<>();
    private final ArrayList<UUID> bannedPlayers;
    private final Map<UUID, Long> invites = new HashMap<>();
    private final List<IslandSettings> settings;
    private String name;
    private Location spawn;
    private int maxSize;
    private int maxMembers;
    private int generatorLevel;
    private double bankMoney;
    private boolean isPublic;
    private double exp;
    private float level;

    private boolean loaded = false;
    private long loadTimeout = -1;

    private boolean isModified = false;
    private boolean areMembersModified = false;
    private boolean arePermsModified = false;
    private boolean areBannedPlayersModified = false;
    private boolean areSettingsModified = false;

    public Island(UUID islandUUID, String name, Location spawn, Map<UUID, IslandRanks> members, Map<UUID,
            String> membersNames, Map<IslandRanks, ArrayList<IslandPerms>> perms, int maxSize, int maxMembers,
                  int generatorLevel, double bankMoney, ArrayList<UUID> bannedPlayers, boolean isPublic, double exp,
                  List<IslandSettings> settings, float level) {
        this.islandUUID = islandUUID;
        this.name = name;
        this.spawn = spawn;
        this.members = members;
        this.membersNames.putAll(membersNames);
        this.perms = perms;
        if (perms.isEmpty()) setDefaultPerms(true);
        this.maxSize = maxSize;
        this.maxMembers = maxMembers;
        this.generatorLevel = generatorLevel;
        this.bankMoney = bankMoney;
        this.bannedPlayers = bannedPlayers;
        this.isPublic = isPublic;
        this.exp = exp;
        this.settings = settings;
        this.level = level;
    }

    //default just the necessary to establish a start island
    public Island(UUID islandUUID, Location spawn, UUID owner) {
        this.islandUUID = islandUUID;
        this.name = "Nom par défaut";
        this.spawn = spawn;
        this.members = new HashMap<>();
        this.members.put(owner, IslandRanks.CHEF); // this is an exception, please use the addMember method to trigger an update
        this.membersNames.put(owner, CoreSkyblock.INSTANCE.getServer().getOfflinePlayer(owner).getName());
        this.perms = new HashMap<>();
        setDefaultPerms(false);
        this.maxSize = 1;
        this.maxMembers = 1;
        this.generatorLevel = 1;
        this.bankMoney = 0;
        this.bannedPlayers = new ArrayList<>();
        this.isPublic = true;
        this.exp = 0;
        this.settings = new ArrayList<>();
        this.level = 0;
        addDefaultSettings();
    }

    public static Map<IslandRanks, ArrayList<IslandPerms>> getRanksPermsFromReduced(Map<IslandRanks, ArrayList<IslandPerms>> reducedPerms) {
        Map<IslandRanks, ArrayList<IslandPerms>> toReturn = new HashMap<>();
        ArrayList<IslandPerms> alreadyAdded = new ArrayList<>();
        for (IslandRanks rank : IslandRanks.getRanksReverse()) {
            reducedPerms.computeIfAbsent(rank, k -> new ArrayList<>());
            ArrayList<IslandPerms> perms = new ArrayList<>(reducedPerms.get(rank));
            perms.addAll(alreadyAdded);
            toReturn.put(rank, perms);
            alreadyAdded.addAll(perms);
        }
        return toReturn;
    }

    public void setDefaultPerms(boolean update) {
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
        permsMod.addAll(Arrays.asList(IslandPerms.KICK, IslandPerms.PROMOTE, IslandPerms.DEMOTE));
        this.perms.put(IslandRanks.MODERATEUR, permsMod);

        ArrayList<IslandPerms> permsCoChef = new ArrayList<>();
        permsCoChef.addAll(permsMod);
        permsCoChef.addAll(Arrays.asList(IslandPerms.INVITE, IslandPerms.BAN));
        this.perms.put(IslandRanks.COCHEF, permsCoChef);

        ArrayList<IslandPerms> permsChef = new ArrayList<>();
        permsChef.add(IslandPerms.ALL_PERMS);
        this.perms.put(IslandRanks.CHEF, permsChef);

        if (update) {
            arePermsModified = true;
        }
    }

    public void addDefaultSettings() {
        addSetting(IslandSettings.TIME_DEFAULT);
        addSetting(IslandSettings.WEATHER_DEFAULT);
        addSetting(IslandSettings.BLOCK_BURNING);
        addSetting(IslandSettings.LIGHTNING_STRIKE);
        addSetting(IslandSettings.BLOCK_EXPLOSION);
        addSetting(IslandSettings.MOB_SPAWNING);
        addSetting(IslandSettings.MOB_GRIEFING);
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
        if (this.spawn == null) {
            if (isLoaded()) {
                this.spawn.setWorld(IslandsManager.INSTANCE.getIslandWorld(this.islandUUID));
            }
        }
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

        IslandsSizeManager.INSTANCE.updateWorldBorder(this);
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
        update(true);
    }

    public ArrayList<UUID> getBannedPlayers() {
        return this.bannedPlayers;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        if (!isPublic) IslandsManager.INSTANCE.checkForPlayerOnAccessibilityChange(this);

        update(true);
    }

    public double getExp() {
        return this.exp;
    }

    public void setLevelExp(double exp) {
        this.exp = exp;
        isModified = true;
    }

    public void addMember(UUID uuid, String name, IslandRanks rank) {
        boolean needUpdate = !this.members.containsKey(uuid);
        this.members.put(uuid, rank);
        this.membersNames.put(uuid, name);

        if (needUpdate) areMembersModified = true;
        else update(true);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);

        Player player = CoreSkyblock.INSTANCE.getServer().getPlayer(uuid);
        if (player != null) {
            player.teleportAsync(CoreSkyblock.SPAWN);
            player.sendMessage(Component.text("§cVous avez été retiré de l'île."));
        }

        update(true);
    }

    public void update(boolean async) {
        if (async) CompletableFuture.runAsync(() -> IslandsDataManager.INSTANCE.update(this, areMembersModified,
                arePermsModified, areBannedPlayersModified, areSettingsModified));
        else IslandsDataManager.INSTANCE.update(this, areMembersModified, arePermsModified,
                areBannedPlayersModified, areSettingsModified);

        isModified = false;
        areMembersModified = false;
        arePermsModified = false;
        areBannedPlayersModified = false;
        areSettingsModified = false;
    }

    public boolean needUpdate() {
        if (isModified) return true;
        if (areMembersModified) return true;
        if (arePermsModified) return true;
        if (areSettingsModified) return true;
        return areBannedPlayersModified;
    }

    public boolean hasPerms(IslandRanks rank, IslandPerms perm, UUID uuid) {
        if (rank == IslandRanks.CHEF) {
            return true;
        }
        if (getPerms().get(rank) != null) {
            if (uuid != null) {
                if (IslandsManager.INSTANCE.isBypassing(uuid)) {
                    return true;
                }
            }
            if (getPerms().get(rank).contains(IslandPerms.ALL_PERMS)) return true;
            if (perm == null) return false;
            return getPerms().get(rank).contains(perm);
        }
        return false;
    }

    public UUID getOwnerUUID() {
        for (Map.Entry<UUID, IslandRanks> entry : members.entrySet()) {
            if (entry.getValue() == IslandRanks.CHEF) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void removePermsToRank(IslandRanks islandRank, IslandPerms islandPerms) {
        if (getPerms().get(islandRank) == null) {
            return;
        }
        if (!getPerms().get(islandRank).contains(islandPerms)) {
            return;
        }
        getPerms().get(islandRank).remove(islandPerms);
        arePermsModified = true;
    }

    public void addPermsToRank(IslandRanks islandRank, IslandPerms islandPerms) {
        if (getPerms().get(islandRank) == null) {
            return;
        }
        if (getPerms().get(islandRank).contains(islandPerms)) {
            return;
        }
        getPerms().get(islandRank).add(islandPerms);
        arePermsModified = true;
    }

    public boolean promote(UUID uuid) {
        if (!members.containsKey(uuid)) return false;
        IslandRanks rank = members.get(uuid);
        if (rank == IslandRanks.CHEF) return false;
        IslandRanks nextRank = IslandRanksManager.INSTANCE.getNextRank(rank);
        if (nextRank == null) return false;
        if (nextRank == IslandRanks.CHEF) return false;
        members.put(uuid, nextRank);
        areMembersModified = true;
        return true;
    }

    public boolean demote(UUID uuid) {
        if (!members.containsKey(uuid)) return false;
        IslandRanks rank = members.get(uuid);
        if (rank == IslandRanks.MEMBRE) return false;
        IslandRanks previousRank = IslandRanksManager.INSTANCE.getPreviousRank(rank);
        if (previousRank == null) return false;
        members.put(uuid, previousRank);
        areMembersModified = true;
        return true;
    }

    public void addInvite(UUID uuid) {
        invites.put(uuid, System.currentTimeMillis());
    }

    public boolean isInvited(UUID uuid) {
        if (!invites.containsKey(uuid)) return false;
        if (System.currentTimeMillis() - invites.get(uuid) > 1000 * 60 * 5) {
            invites.remove(uuid);
            return false;
        }
        return true;
    }

    public void removeInvite(UUID uuid) {
        invites.remove(uuid);
    }

    public ArrayList<Player> getOnlineMembers() {
        ArrayList<Player> onlineMembers = new ArrayList<>();
        for (Map.Entry<UUID, IslandRanks> entry : members.entrySet()) {
            Player player = CoreSkyblock.INSTANCE.getServer().getPlayer(entry.getKey());
            if (player != null) {
                onlineMembers.add(player);
            }
        }
        return onlineMembers;
    }

    public int getOnlineMembersCount() {
        return getOnlineMembers().size();
    }

    public void sendMessage(String message, IslandPerms perm) {
        for (Map.Entry<UUID, IslandRanks> entry : members.entrySet()) {
            if (hasPerms(entry.getValue(), perm, entry.getKey())) {
                Player player = CoreSkyblock.INSTANCE.getServer().getPlayer(entry.getKey());
                if (player != null) {
                    player.sendMessage(Component.text(message));
                }
            }
        }
    }

    public void sendMessageToAll(String message) {
        for (Map.Entry<UUID, IslandRanks> entry : members.entrySet()) {
            Player player = CoreSkyblock.INSTANCE.getServer().getPlayer(entry.getKey());
            if (player != null) {
                player.sendMessage(Component.text(message));
            }
        }
    }

    public String getMemberName(UUID uuid) {
        return membersNames.get(uuid);
    }

    public Map<UUID, String> getMembersNames() {
        return membersNames;
    }

    public Map<IslandRanks, ArrayList<IslandPerms>> getRanksPermsReduced() {
        Map<IslandRanks, ArrayList<IslandPerms>> reducedPerms = new HashMap<>();
        ArrayList<IslandPerms> alreadyAdded = new ArrayList<>();
        for (IslandRanks rank : IslandRanks.getRanksReverse()) {
            reducedPerms.computeIfAbsent(rank, k -> new ArrayList<>());
            ArrayList<IslandPerms> perms = new ArrayList<>(this.perms.get(rank));
            perms.removeAll(alreadyAdded);
            reducedPerms.put(rank, perms);
            alreadyAdded.addAll(perms);
        }
        return reducedPerms;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isLoadTimeout() {
        return this.loadTimeout != -1 && System.currentTimeMillis() - this.loadTimeout > 1000 * 60 * 3;
    }

    public long getLoadTimeout() {
        return this.loadTimeout;
    }

    public void setLoadTimeout(long loadTimeout) {
        this.loadTimeout = loadTimeout;
    }

    public List<IslandSettings> getSettings() {
        return settings;
    }

    public boolean hasSettingActivated(IslandSettings setting) {
        return settings.contains(setting);
    }

    public void addSetting(IslandSettings setting) {
        settings.add(setting);

        areSettingsModified = true;
    }

    public void removeSetting(IslandSettings setting) {
        settings.remove(setting);

        areSettingsModified = true;
    }

    public float getLevel() {
        return this.level;
    }

    public void setLevel(float level) {
        this.level = level;

        isModified = true;
    }
}
