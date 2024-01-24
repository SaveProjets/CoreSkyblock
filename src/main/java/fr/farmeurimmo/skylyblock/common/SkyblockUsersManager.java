package fr.farmeurimmo.skylyblock.common;

import java.util.ArrayList;
import java.util.UUID;

public class SkyblockUsersManager {

    public static SkyblockUsersManager INSTANCE;
    private final ArrayList<SkyblockUser> users = new ArrayList<>();

    public SkyblockUsersManager() {
        INSTANCE = this;
    }

    public SkyblockUser getUser(UUID uuid) {
        return users.stream().filter(user -> user.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public SkyblockUser getLocalUser(String name) {
        return users.stream().filter(user -> user.getName().equals(name)).findFirst().orElse(null);
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
