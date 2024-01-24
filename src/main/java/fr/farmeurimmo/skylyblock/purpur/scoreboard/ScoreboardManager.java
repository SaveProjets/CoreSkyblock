package fr.farmeurimmo.skylyblock.purpur.scoreboard;

import fr.farmeurimmo.skylyblock.common.SkyblockUser;
import fr.farmeurimmo.skylyblock.common.SkyblockUsersManager;
import fr.farmeurimmo.skylyblock.purpur.SkylyBlock;
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

        Bukkit.getScheduler().runTaskTimerAsynchronously(SkylyBlock.INSTANCE, this::updateClock, 0, 20);
    }

    public void addPlayer(Player p) {
        FastBoard board = new FastBoard(p);
        board.updateTitle("§6§lSkylyBlock");
        boards.put(p.getUniqueId(), board);
        boardNumber.put(p.getUniqueId(), 0);
    }

    public void updateBoard(UUID uuid) {
        FastBoard board = boards.get(uuid);
        int number = 0;
        if (boardNumber.containsKey(uuid)) number = boardNumber.get(uuid);
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return;
        SkyblockUser user = SkyblockUsersManager.INSTANCE.getUser(uuid);
        if (user == null) {
            board.updateTitle("§4§lErreur lors du chargement du profil");
            return;
        }
        if (number == 0) {
            board.updateTitle("§4»§c» §c§lSKYBLOCK §c«§4«");
            board.updateLines(
                    "",
                    "§6§lProfil",
                    "§8┃ §7Pseudo: §f" + p.getName(),
                    "§8┃ §7Argent: §e" + NumberFormat.getInstance().format(user.getMoney()),
                    "§8┃ §7Grade: §c????",
                    "",
                    "§6§lVotre île §8[§7#????§8]",
                    "§8┃ §7Rang: §4???",
                    "§8┃ §7Membres: §e??",
                    "§8┃ §7Cristaux: §d???",
                    "§8┃ §7Niveau: §b??",
                    "§8┃ §8[§b┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃┃§7┃§8]",
                    "",
                    "§f» §c§lplay.skyly.fr"
            );
        } else if (number == 1) {
            board.updateTitle("§6§lSkylyBlock");
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
