package fr.farmeurimmo.skylyblock.common;

import java.util.ArrayList;
import java.util.UUID;

public class SkyblockUserManager {

    public static SkyblockUserManager INSTANCE;
    private final ArrayList<SkyblockUser> users = new ArrayList<>();

    public SkyblockUserManager() {
        INSTANCE = this;
    }

    public SkyblockUser getUser(UUID uuid) {
        return users.stream().filter(user -> user.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public void addUser(SkyblockUser user) {
        users.add(user);
    }

    public void removeUser(SkyblockUser user) {
        users.remove(user);
    }

    public SkyblockUser checkForAccountOrCreate(UUID uuid, String name) {
        SkyblockUser user = getUser(uuid);
        if (user == null) {
            user = new SkyblockUser(uuid, name);
            addUser(user);
        }
        return user;
    }

    public ArrayList<SkyblockUser> getLocalUsers() {
        return users;
    }
}
