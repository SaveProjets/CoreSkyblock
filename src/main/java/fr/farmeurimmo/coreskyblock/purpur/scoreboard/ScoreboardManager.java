package fr.farmeurimmo.coreskyblock.purpur.scoreboard;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    public static ScoreboardManager INSTANCE;
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final Map<UUID, Integer> boardNumber = new HashMap<>();

    public ScoreboardManager() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, this::updateClock, 0, 20);
    }

    public void addPlayer(Player p) {
        FastBoard board = new FastBoard(p);
        board.updateTitle("§6§lCoreSkyblock");
        boards.put(p.getUniqueId(), board);
        boardNumber.put(p.getUniqueId(), 0);
    }

    public void updateBoard(UUID uuid) {
        FastBoard board = boards.get(uuid);
        int number = 0;
        if (boardNumber.containsKey(uuid)) number = boardNumber.get(uuid);
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return;
        SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
        Island island = IslandsManager.INSTANCE.getIslandOf(p.getUniqueId());
        if (user == null) {
            board.updateTitle("§a§lChargement...");
            return;
        }
        ArrayList<String> islandLines = new ArrayList<>();
        if (island != null) {
            islandLines.add("§6§l" + island.getName().replace("&", "§"));
            islandLines.add("§8┃ §7Rang: §4" + island.getMembers().get(p.getUniqueId()).getName());
            islandLines.add("§8┃ §7Membres: §e" + island.getMembers().size());
            islandLines.add("§8┃ §7Argent: §d" + NumberFormat.getInstance().format(island.getBankMoney()));
            islandLines.add("§8┃ §7Expérience: §b" + NumberFormat.getInstance().format(island.getExp()));
            islandLines.add("§8┃ §7Niveau: §3" + NumberFormat.getInstance().format(island.getLevel()));
        } else {
            islandLines.add("§6§lVous n'avez pas d'île");
            islandLines.add("§8┃ §7/is create");
            islandLines.add("§8┃ §7Créer une île");
            islandLines.add("§8┃ §7/is accept <joueur>");
            islandLines.add("§8┃ §7Accepter une invitation");
            islandLines.add("§8┃ §7d'un joueur");
        }
        if (number == 0) {
            board.updateTitle("§4»§c» §c§lSKYBLOCK §c«§4«");
            board.updateLines(
                    "§8" + CoreSkyblock.SERVER_NAME,
                    "",
                    "§6§lProfil",
                    "§8┃ §7Pseudo: §f" + p.getName(),
                    "§8┃ §7Argent: §e" + NumberFormat.getInstance().format(user.getMoney()),
                    "§8┃ §7Grade: §c????",
                    "",
                    islandLines.get(0),
                    islandLines.get(1),
                    islandLines.get(2),
                    islandLines.get(3),
                    islandLines.get(4),
                    islandLines.get(5),
                    "",
                    "§f» §c§lplay.edmine.net"
            );
        } else if (number == 1) {
            board.updateTitle("§6§lCoreSkyblock");
        }
    }

    public void updateClock() {
        ArrayList<UUID> toRemove = new ArrayList<>();
        for (UUID uuid : boards.keySet()) {
            if (Bukkit.getPlayer(uuid) == null) {
                toRemove.add(uuid);
                continue;
            }
            updateBoard(uuid);
        }
        for (UUID uuid : toRemove) {
            boards.remove(uuid);
            boardNumber.remove(uuid);
        }
    }
}
