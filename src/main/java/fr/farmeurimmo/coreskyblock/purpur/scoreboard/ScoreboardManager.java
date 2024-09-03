package fr.farmeurimmo.coreskyblock.purpur.scoreboard;

import fr.farmeurimmo.coreskyblock.ServerType;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsTopManager;
import fr.farmeurimmo.coreskyblock.purpur.prestige.PrestigesManager;
import fr.farmeurimmo.coreskyblock.purpur.pvp.PvpPlanningManager;
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
    /*private String timeUntilNextWeek = "00:00:00";
    private String cycleWithWeek = "Cycle 1, Semaine 1";
    private String crop = "...";*/

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
        if (user == null) {
            board.updateTitle("§c§lChargement...");
            board.updateLines("§c§lVeuillez patienter,", "§c§lvos données sont en", "§c§lcours de chargement...", "", "§f» §c§lplay.edmine.fr");
            return;
        }
        board.updateTitle("§8● §6§lSkyblock §8●");
        if (CoreSkyblock.SERVER_TYPE == ServerType.GAME || CoreSkyblock.SERVER_TYPE == ServerType.SPAWN) {
            Island island = IslandsManager.INSTANCE.getIslandOf(p.getUniqueId());
            ArrayList<String> islandLines = new ArrayList<>();
            if (island != null) {
                int position = IslandsTopManager.INSTANCE.getPosition(island.getIslandUUID(), 0);
                islandLines.add("➡ §a§l" + island.getName().replace("&", "§") + " " + (position > 0 ?
                        "§8(§6#" + position + "§8)" : "§8(§7#?§8)"));
                islandLines.add("§e §8• §7Argent: §d" + NumberFormat.getInstance().format(island.getBankMoney()));
                islandLines.add("§e §8• §7Niveau: §b" + NumberFormat.getInstance().format(island.getLevel()));
            } else {
                islandLines.add("➡ §a§lVous n'avez pas d'île");
                islandLines.add("§e §8• §7/is create");
                islandLines.add("§e §8• §7/is accept <joueur>");
            }
            if (number == 0) {
                board.updateLines(
                        "",
                        "➡ §d§lInformations:",
                        "§e §8• §7Grade: §c§lADMIN",
                        "§e §8• §7Argent: §e" + NumberFormat.getInstance().format(user.getMoney()),
                        "§e §8• §7Prestige: §4" + PrestigesManager.INSTANCE.getColorCode(user.getLastPrestigeLevelClaimed()) +
                                NumberFormat.getInstance().format(user.getLastPrestigeLevelClaimed()) + " " +
                                PrestigesManager.INSTANCE.getProgressBar(p.getUniqueId()),
                        "",
                        islandLines.get(0),
                        islandLines.get(1),
                        islandLines.get(2),
                        "",
                        "§8➡ §eplay.edmine.fr"
                );
            } else if (number == 1) {
                board.updateTitle("§6§lCoreSkyblock");
            }
        } else if (CoreSkyblock.SERVER_TYPE == ServerType.PVP) {
            if (PvpPlanningManager.INSTANCE.currentEvent == null) {
                board.updateLines(
                        "",
                        "➡ §d§lInformations:",
                        "§e §8• §7En pvp: §c(N/A)",
                        "§e §8• §7Kills: §a" + user.getPvpKills(),
                        "§e §8• §7Morts: §c" + user.getPvpDeaths(),
                        "§e §8• §7KDR: §e" + NumberFormat.getInstance().format(user.getPvpKills() / (user.getPvpDeaths() == 0 ? 1 : user.getPvpDeaths())),
                        "",
                        "➡ §a§lEvents",
                        "§e §8• §7Prochain: §e" + PvpPlanningManager.INSTANCE.getNextEvent().getType().name(),
                        "§e §8• §7Dans: §e" + PvpPlanningManager.INSTANCE.getNextInString(),
                        "",
                        "§8➡ §eplay.edmine.fr");
            } else {
                board.updateLines(
                        "",
                        "➡ §d§lInformations:",
                        "§e §8• §7En pvp: §a" + PvpPlanningManager.INSTANCE.currentEvent.getType().name(),
                        "§e §8• §7Kills: §a" + user.getPvpKills(),
                        "§e §8• §7Morts: §c" + user.getPvpDeaths(),
                        "§e §8• §7KDR: §e" + NumberFormat.getInstance().format(user.getPvpKills() / (user.getPvpDeaths() == 0 ? 1 : user.getPvpDeaths())),
                        "",
                        "➡ §a§lEvents",
                        "§e §8• §7En cours: §a" + PvpPlanningManager.INSTANCE.currentEvent.getType().name(),
                        "§e §8• §7Temps restant: §e" + PvpPlanningManager.INSTANCE.getTimeLeftForCurrentEvent(),
                        "",
                        "§8➡ §eplay.edmine.fr");
            }
        } else if (CoreSkyblock.SERVER_TYPE == ServerType.PVE) {
            board.updateLines("§6§l" + CoreSkyblock.SERVER_NAME,
                    "",
                    "➡ §d§lInformations:",
                    "",
                    "➡ §a§lDonjons: §cEN DEV",
                    "",
                    "§8➡ §eplay.edmine.fr");
        }
    }

    public void updateClock() {
        /*try {
            timeUntilNextWeek = DateUtils.getFormattedTimeLeft((int) (AgricultureCycleManager.INSTANCE.getCurrentSeason()
                    .getTimeUntilNextWeek() / 1000));
            cycleWithWeek = AgricultureCycleManager.INSTANCE.getCurrentSeason().getCycleWithWeek();
            if (AgricultureCycleManager.INSTANCE.getCurrentSeason().getCrop() != null)
                crop = AgricultureCycleManager.INSTANCE.getCurrentSeason().getCrop().getName();
            else crop = "Aucune";
        } catch (Exception e) {
            timeUntilNextWeek = "Chargement...";
            cycleWithWeek = "Chargement...";
            crop = "Chargement...";
        }*/
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
