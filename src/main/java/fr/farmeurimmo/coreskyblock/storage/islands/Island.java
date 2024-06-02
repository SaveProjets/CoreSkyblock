package fr.farmeurimmo.coreskyblock.storage.islands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.chests.Chest;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsCoopsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.upgrades.IslandsSizeManager;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandRanks;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandSettings;
import fr.farmeurimmo.coreskyblock.utils.LocationTranslator;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Island {

    private final UUID islandUUID;
    private final Map<UUID, IslandRanks> members;
    private final Map<IslandRanks, ArrayList<IslandPerms>> perms;
    private final Map<UUID, String> membersNames = new HashMap<>();
    private final ArrayList<UUID> bannedPlayers;
    private final Map<UUID, Long> invites = new HashMap<>(); // not saved
    private final List<IslandSettings> settings;
    private final List<Chest> chests;
    private final Map<UUID, UUID> coops = new HashMap<>(); // not saved
    private String name;
    private Location spawn;
    private int maxSize;
    private int maxMembers;
    private int generatorLevel;
    private double bankMoney;
    private boolean isPublic;
    private double exp;
    private float level;
    private boolean loaded = false; // not saved
    private boolean readOnly = true; // not saved
    private long loadTimeout = -1; // not saved
    private boolean isModified = false; // not saved
    private boolean areMembersModified = false; // not saved
    private boolean arePermsModified = false; // not saved
    private boolean areBannedPlayersModified = false; // not saved
    private boolean areSettingsModified = false; // not saved
    private boolean areChestsModified = false; // not saved
    private boolean isBankModified = false; // not saved

    public Island(UUID islandUUID, String name, Location spawn, Map<UUID, IslandRanks> members, Map<UUID,
            String> membersNames, Map<IslandRanks, ArrayList<IslandPerms>> perms, int maxSize, int maxMembers,
                  int generatorLevel, double bankMoney, ArrayList<UUID> bannedPlayers, boolean isPublic, double exp,
                  List<IslandSettings> settings, float level, List<Chest> chests, boolean readOnly) {
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
        this.chests = chests;
        this.readOnly = readOnly;
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
        this.chests = new ArrayList<>();
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

    public static Island fromJson(JsonObject json) {
        try {
            UUID islandUUID = UUID.fromString(json.get("islandUUID").getAsString());
            String name = json.get("name").getAsString();
            Location spawn = LocationTranslator.fromString(json.get("spawn").getAsString());
            int maxSize = json.get("maxSize").getAsInt();
            int maxMembers = json.get("maxMembers").getAsInt();
            int generatorLevel = json.get("generatorLevel").getAsInt();
            double bankMoney = json.get("bankMoney").getAsDouble();
            boolean isPublic = json.get("isPublic").getAsBoolean();
            double exp = json.get("exp").getAsDouble();
            float level = json.get("level").getAsFloat();
            //members
            Map<UUID, IslandRanks> members = new HashMap<>();
            Map<UUID, String> membersNames = new HashMap<>();
            JsonObject membersJson = json.getAsJsonObject("members");
            for (Map.Entry<String, JsonElement> entry : membersJson.entrySet()) {
                JsonObject memberJson = entry.getValue().getAsJsonObject();
                members.put(UUID.fromString(entry.getKey()), IslandRanks.valueOf(memberJson.get("rank").getAsString()));
                membersNames.put(UUID.fromString(entry.getKey()), memberJson.get("name").getAsString());
            }
            //bannedPlayers
            ArrayList<UUID> bannedPlayers = new ArrayList<>();
            JsonObject bannedPlayersJson = json.getAsJsonObject("bannedPlayers");
            for (Map.Entry<String, JsonElement> entry : bannedPlayersJson.entrySet()) {
                bannedPlayers.add(UUID.fromString(entry.getKey()));
            }
            //settings
            List<IslandSettings> settings = new ArrayList<>();
            JsonObject settingsJson = json.getAsJsonObject("settings");
            for (Map.Entry<String, JsonElement> entry : settingsJson.entrySet()) {
                settings.add(IslandSettings.valueOf(entry.getKey()));
            }
            //chests
            List<Chest> chests = new ArrayList<>();
            JsonObject chestsJson = json.getAsJsonObject("chests");
            for (Map.Entry<String, JsonElement> entry : chestsJson.entrySet()) {
                chests.add(Chest.fromJson(entry.getValue().getAsJsonObject()));
            }
            //perms
            Map<IslandRanks, ArrayList<IslandPerms>> perms = new HashMap<>();
            JsonObject permsJson = json.getAsJsonObject("perms");
            for (Map.Entry<String, JsonElement> entry : permsJson.entrySet()) {
                IslandRanks rank = IslandRanks.valueOf(entry.getKey());
                ArrayList<IslandPerms> permsList = new ArrayList<>();
                JsonObject permsRankJson = (JsonObject) entry.getValue();
                for (Map.Entry<String, JsonElement> permEntry : permsRankJson.entrySet()) {
                    permsList.add(IslandPerms.valueOf(permEntry.getKey()));
                }
                perms.put(rank, permsList);
            }
            //get dereduced perms
            perms = getRanksPermsFromReduced(perms);

            return new Island(islandUUID, name, spawn, members, membersNames, perms, maxSize, maxMembers, generatorLevel,
                    bankMoney, bannedPlayers, isPublic, exp, settings, level, chests, true);
        } catch (Exception ignored) {
        }
        return null;
    }

    public void setDefaultPerms(boolean update) {
        ArrayList<IslandPerms> permsVisit = new ArrayList<>(Arrays.asList(IslandPerms.KILL_ANIMALS,
                IslandPerms.KILL_MOBS, IslandPerms.DROP_ITEMS, IslandPerms.FISH, IslandPerms.FLY,
                IslandPerms.PICKUP_ITEMS, IslandPerms.INTERACT_WITH_VILLAGERS));
        perms.put(IslandRanks.VISITEUR, permsVisit);

        ArrayList<IslandPerms> perms = new ArrayList<>();
        perms.addAll(permsVisit);
        perms.addAll(Arrays.asList(IslandPerms.BUILD, IslandPerms.BREAK, IslandPerms.INTERACT, IslandPerms.CONTAINER,
                IslandPerms.FEED_ANIMALS, IslandPerms.INTERACT_WITH_MOUNTS, IslandPerms.USE_SPAWN_EGG,
                IslandPerms.INTERACT_WITH_ITEM_FRAMES));
        this.perms.put(IslandRanks.COOP, perms);

        ArrayList<IslandPerms> permsMembre = new ArrayList<>();
        permsMembre.addAll(perms);
        permsMembre.addAll(Arrays.asList(IslandPerms.EDIT_MINIONS, IslandPerms.BANK_ADD,
                IslandPerms.CALCULATE_ISLAND_LEVEL));
        this.perms.put(IslandRanks.MEMBRE, permsMembre);

        ArrayList<IslandPerms> permsMod = new ArrayList<>();
        permsMod.addAll(permsMembre);
        permsMod.addAll(Arrays.asList(IslandPerms.KICK, IslandPerms.EDIT_BANS, IslandPerms.EXPEL,
                IslandPerms.BREAK_SPAWNERS));
        this.perms.put(IslandRanks.MODERATEUR, permsMod);

        ArrayList<IslandPerms> permsCoChef = new ArrayList<>();
        permsCoChef.addAll(permsMod);
        permsCoChef.addAll(Arrays.asList(IslandPerms.INVITES, IslandPerms.EDIT_BANS, IslandPerms.CHANGE_RANK,
                IslandPerms.EDIT_PUBLIC, IslandPerms.SET_HOME, IslandPerms.CHANGE_PERMS, IslandPerms.EDIT_COOP,
                IslandPerms.BANK_REMOVE, IslandPerms.SECURED_CHEST, IslandPerms.EDIT_ISLAND_WARP,
                IslandPerms.EDIT_ISLAND_CHARACTERISTICS, IslandPerms.UPGRADE_ISLAND, IslandPerms.EDIT_SETTINGS,
                IslandPerms.BREAK_ISLAND_LEVEL_BLOCKS, IslandPerms.CHANGE_ISLAND_BIOME, IslandPerms.EDIT_MINIONS));
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
        isModified = true;
    }

    public Location getSpawn() {
        if (this.spawn == null || this.spawn.getWorld() == null) {
            if (isLoaded()) {
                this.spawn.setWorld(IslandsManager.INSTANCE.getIslandWorld(this.islandUUID));
            }
        }
        Location temp = this.spawn.clone();
        while (temp.getBlock().getType() != Material.AIR) {
            temp.add(0, 1, 0);
        }
        return temp;
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

        isBankModified = true;
    }

    public ArrayList<UUID> getBannedPlayers() {
        return this.bannedPlayers;
    }

    public void addBannedPlayer(UUID uuid) {
        this.bannedPlayers.add(uuid);
        areBannedPlayersModified = true;
    }

    public void removeBannedPlayer(UUID uuid) {
        this.bannedPlayers.remove(uuid);

        CompletableFuture.runAsync(() -> IslandsDataManager.INSTANCE.deleteBanned(islandUUID, uuid));
        areBannedPlayersModified = true;
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

        CompletableFuture.runAsync(() -> {
            if (needUpdate) update(false);
            JedisManager.INSTANCE.sendToRedis("coreskyblock:island:members:" + uuid, islandUUID.toString());
        });
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);

        CompletableFuture.runAsync(() -> {
            IslandsDataManager.INSTANCE.deleteMember(islandUUID, uuid);
            JedisManager.INSTANCE.removeFromRedis("coreskyblock:island:members:" + uuid);
            JedisManager.INSTANCE.sendToRedis("coreskyblock:island:" + islandUUID, CoreSkyblock.INSTANCE.gson
                    .toJson(toJson()));
            pubSub(false);
        });

        Player player = CoreSkyblock.INSTANCE.getServer().getPlayer(uuid);
        if (player != null) {
            player.teleportAsync(CoreSkyblock.SPAWN);
            player.sendMessage(Component.text("§cVous avez été retiré de l'île."));
        }

        areMembersModified = true;
    }

    public UUID getMemberUUIDFromName(String name) {
        for (Map.Entry<UUID, String> entry : membersNames.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void changeOwner(UUID newOwner) {
        if (members.containsKey(newOwner)) {
            members.put(getOwnerUUID(), IslandRanks.COCHEF);
            members.put(newOwner, IslandRanks.CHEF);
            areMembersModified = true;
            update(true);
        }
    }

    public void update(boolean async) {
        if (async) {
            CompletableFuture.runAsync(() -> {
                Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                    setUpdates(false);
                    return null;
                });

                try {
                    JedisManager.INSTANCE.sendToRedis("coreskyblock:island:" + islandUUID, CoreSkyblock.INSTANCE.gson
                            .toJson(toJson()));
                    pubSub(false);
                    IslandsDataManager.INSTANCE.update(this, areMembersModified,
                            arePermsModified, areBannedPlayersModified, areSettingsModified, areChestsModified);
                } catch (Exception e) {
                    setUpdates(true);
                    e.printStackTrace();
                }

            });
        } else {
            try {
                setUpdates(false);

                JedisManager.INSTANCE.sendToRedis("coreskyblock:island:" + islandUUID, CoreSkyblock.INSTANCE.gson
                        .toJson(toJson()));
                IslandsDataManager.INSTANCE.update(this, areMembersModified, arePermsModified,
                        areBannedPlayersModified, areSettingsModified, areChestsModified);
                pubSub(true);
            } catch (Exception e) {
                setUpdates(true);
                e.printStackTrace();
            }
        }
    }

    private void setUpdates(boolean state) {
        isModified = state;
        areMembersModified = state;
        arePermsModified = state;
        areBannedPlayersModified = state;
        areSettingsModified = state;
        areChestsModified = state;
        isBankModified = state;
    }

    public void pubSub(boolean async) {
        if (async) {
            CompletableFuture.runAsync(() -> {
                JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:pubsub:" + islandUUID.toString()
                        + ":" + CoreSkyblock.SERVER_NAME);
            });
        } else {
            JedisManager.INSTANCE.publishToRedis("coreskyblock", "island:pubsub:" +
                    islandUUID.toString() + ":" + CoreSkyblock.SERVER_NAME);
        }
    }

    public boolean needUpdate() {
        if (isModified) return true;
        if (areMembersModified) return true;
        if (arePermsModified) return true;
        if (areSettingsModified) return true;
        if (areChestsModified) return true;
        if (isBankModified) return true;
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

    public boolean hasPerms(Island island, IslandPerms perms, UUID uuid) {
        if (island.getPlayerRank(uuid) == IslandRanks.CHEF) {
            return true;
        }
        if (island.getPerms().get(island.getPlayerRank(uuid)) != null) {
            if (IslandsManager.INSTANCE.isBypassing(uuid)) return true;
            if (island.getPerms().get(island.getPlayerRank(uuid)).contains(IslandPerms.ALL_PERMS)) return true;
            return island.getPerms().get(island.getPlayerRank(uuid)).contains(perms);
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
        sendMessageToLocals(message, perm);
        CompletableFuture.runAsync(() -> JedisManager.INSTANCE.publishToRedis("coreskyblock",
                "island:chat_message_with_perms:" + islandUUID + ":" + CoreSkyblock.SERVER_NAME + ":" +
                        perm.getId() + ":" + message));
    }

    public void sendMessageToLocals(String message, IslandPerms perm) {
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
        CompletableFuture.runAsync(() -> sendMessageToAllSync(message));
    }

    public void sendMessageToAllSync(String message) {
        sendMessageToAllLocals(message);
        JedisManager.INSTANCE.publishToRedis("coreskyblock",
                "island:chat_message:" + islandUUID + ":" + CoreSkyblock.SERVER_NAME + ":" + message);
    }

    public void sendMessageToAllLocals(String message) {
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
        if (loaded) {
            this.loadTimeout = System.currentTimeMillis();
        } else {
            this.loadTimeout = -1;
        }

        this.loaded = loaded;
    }

    public boolean isLoadTimeout() {
        return getLoadTimeout() != -1 && System.currentTimeMillis() - getLoadTimeout() > 1000 * 60 * 5;
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

        update(true);
    }

    public List<Chest> getChests() {
        return this.chests;
    }

    public void addChest(Chest chest) {
        this.chests.add(chest);

        areChestsModified = true;
    }

    public void removeChest(Chest chest) {
        this.chests.remove(chest);

        CompletableFuture.runAsync(() -> IslandsDataManager.INSTANCE.deleteChest(chest.getUuid()));

        areChestsModified = true;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("islandUUID", islandUUID.toString());
        json.addProperty("name", name);
        json.addProperty("spawn", LocationTranslator.fromLocation(spawn));
        json.addProperty("maxSize", maxSize);
        json.addProperty("maxMembers", maxMembers);
        json.addProperty("generatorLevel", generatorLevel);
        json.addProperty("bankMoney", bankMoney);
        json.addProperty("isPublic", isPublic);
        json.addProperty("exp", exp);
        json.addProperty("level", level);
        //members
        JsonObject membersJson = new JsonObject();
        for (Map.Entry<UUID, IslandRanks> entry : members.entrySet()) {
            JsonObject memberJson = new JsonObject();
            memberJson.addProperty("rank", entry.getValue().toString());
            memberJson.addProperty("name", membersNames.get(entry.getKey()));
            membersJson.add(entry.getKey().toString(), memberJson);
        }
        json.add("members", membersJson);
        //bannedPlayers
        JsonObject bannedPlayersJson = new JsonObject();
        for (UUID uuid : bannedPlayers) {
            bannedPlayersJson.addProperty(uuid.toString(), true);
        }
        json.add("bannedPlayers", bannedPlayersJson);
        //settings
        JsonObject settingsJson = new JsonObject();
        for (IslandSettings setting : settings) {
            settingsJson.addProperty(setting.toString(), true);
        }
        json.add("settings", settingsJson);
        //chests
        JsonObject chestsJson = new JsonObject();
        for (Chest chest : chests) {
            chestsJson.add(chest.getUuid().toString(), chest.toJson());
        }
        json.add("chests", chestsJson);
        //perms
        JsonObject permsJson = new JsonObject();
        for (Map.Entry<IslandRanks, ArrayList<IslandPerms>> entry : getRanksPermsReduced().entrySet()) {
            JsonObject perms = new JsonObject();
            for (IslandPerms perm : entry.getValue()) {
                perms.addProperty(perm.toString(), true);
            }
            permsJson.add(entry.getKey().toString(), perms);
        }
        json.add("perms", permsJson);
        return json;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public final Map<UUID, UUID> getCoops() {
        return this.coops;
    }

    public void addCoop(UUID uuid, UUID adderUUID) {
        this.coops.put(uuid, adderUUID);

        IslandsCoopsManager.INSTANCE.addCoop(uuid, adderUUID);
    }

    public void removeCoop(UUID uuid) {
        this.coops.remove(uuid);

        IslandsCoopsManager.INSTANCE.removeCoop(uuid);
    }

    public IslandRanks getPlayerRank(UUID uuid) {
        if (members.containsKey(uuid)) {
            return members.get(uuid);
        }
        if (coops.containsKey(uuid)) {
            return IslandRanks.COOP;
        }
        return IslandRanks.VISITEUR;
    }
}
