package fr.farmeurimmo.mineblock.purpur.events;

import fr.farmeurimmo.mineblock.common.skyblockusers.SkyblockUser;
import fr.farmeurimmo.mineblock.common.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.mineblock.purpur.MineBlock;
import fr.farmeurimmo.mineblock.utils.DateUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatReactionManager {

    public static ChatReactionManager INSTANCE;
    private final List<String> chatReactions = new ArrayList<>(List.of("Farmeurimmo", "MineBlock", "VeryMc"));
    public boolean isRunning = false;
    private long timeStart = 0L;
    private long timeEnd = 0L;
    private int selectedWord = 0;

    public ChatReactionManager() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskTimerAsynchronously(MineBlock.INSTANCE, () -> {
            if (System.currentTimeMillis() >= timeEnd) {
                if (isRunning) {
                    Bukkit.broadcast(Component.text("§6§lChatReaction §8» §fPersonne n'a recopié le mot à temps !"));
                    isRunning = false;
                }
            }
        }, 0L, 10L);

        Bukkit.getScheduler().runTaskLater(MineBlock.INSTANCE, this::startChatReaction, 20L * 120L);
    }

    public void startChatReaction() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        timeStart = System.currentTimeMillis();
        selectedWord = (int) (Math.random() * chatReactions.size());
        timeEnd = timeStart + 60 * 1000L;

        Component message = Component.text("§6§lChatReaction §8» §fPassez votre souris ici pour voir le mot à recopier !");
        message = message.hoverEvent(HoverEvent.showText(Component.text("§fMot à recopier: §6" + chatReactions.get(selectedWord))));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(MineBlock.INSTANCE, () -> {
            if (isRunning) {
                isRunning = false;
                startChatReaction();
            }
        }, 12000L);
    }

    public void end(Player p, String message) {
        if (message.equalsIgnoreCase(chatReactions.get(selectedWord))) {
            Bukkit.broadcast(Component.text("§6§lChatReaction §8» §f" + p.getName() + " a recopié le mot en §a" +
                    DateUtils.getFormattedTimeLeft2(System.currentTimeMillis() - timeStart) + "§f."));
            isRunning = false;

            SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
            if (user == null) {
                return;
            }
            user.setMoney(user.getMoney() + 5_000);
            p.sendMessage(Component.text("§6§lChatReaction §8» §fVous avez gagné §e" +
                    NumberFormat.getInstance().format(5_000) + " §f$ !"));
        }
    }


}
