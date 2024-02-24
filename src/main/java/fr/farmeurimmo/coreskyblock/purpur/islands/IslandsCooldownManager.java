package fr.farmeurimmo.coreskyblock.purpur.islands;

import it.unimi.dsi.fastutil.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandsCooldownManager {

    private static final long DEFAULT_COOLDOWN = 5_000;
    public static IslandsCooldownManager INSTANCE;
    private final Map<UUID, ArrayList<Pair<String, Long>>> cooldowns = new HashMap<>();
    private final Map<String, Long> defaultCooldowns = new HashMap<>();

    public IslandsCooldownManager() {
        INSTANCE = this;

        defaultCooldowns.put("island-accessibility", 10_000L);
        defaultCooldowns.put("island-calculation-of-level", 20_000L);
    }

    public void addCooldown(UUID uuid, String cooldown) {
        ArrayList<Pair<String, Long>> playerCooldowns = cooldowns.computeIfAbsent(uuid, k -> new ArrayList<>());
        playerCooldowns.add(Pair.of(cooldown, System.currentTimeMillis()));
    }

    public long getCooldownLeft(UUID uuid, String cooldown) {
        ArrayList<Pair<String, Long>> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return -1;
        for (Pair<String, Long> pair : playerCooldowns) {
            if (pair.left().equals(cooldown)) {
                long result = defaultCooldowns.getOrDefault(cooldown, DEFAULT_COOLDOWN) - (System.currentTimeMillis() - pair.right());
                return result < 0 ? -1 : result / 1000;
            }
        }
        return -1;
    }
}
