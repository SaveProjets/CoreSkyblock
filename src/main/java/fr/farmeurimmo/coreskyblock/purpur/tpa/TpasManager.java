package fr.farmeurimmo.coreskyblock.purpur.tpa;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.storage.JedisManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpasManager {

    public static final long TPA_REQUEST_EXPIRE_TIME = 30_000L;
    public static TpasManager INSTANCE;
    public final Map<UUID, UUID> incomingPlayersTpa = new HashMap<>();
    private final ArrayList<TpaRequest> tpaRequests = new ArrayList<>();

    public TpasManager() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, () -> {
            tpaRequests.removeIf(request -> {
                if (System.currentTimeMillis() - request.timestamp() >= TPA_REQUEST_EXPIRE_TIME) {
                    Player sender = Bukkit.getPlayer(request.sender());
                    if (sender != null) {
                        sender.sendMessage(Component.text("§cVotre demande de téléportation à §e" + request.receiverName() + " §ca expiré."));
                    }
                    Player receiver = Bukkit.getPlayer(request.receiver());
                    if (receiver != null) {
                        receiver.sendMessage(Component.text("§eLa demande de téléportation de §c" + request.senderName() + " §ea expiré."));
                    }
                    return true;
                }
                return false;
            });
        }, 0L, 20L);
    }

    public Component getTpaComponent(String targetName) {
        return Component.text("\n§e" + targetName + " §7vous demande s'il peut §e§lSE TÉLÉPORTER §7à vous. (Expire dans 30 secondes)\n\n")
                .append(Component.text("§a§l[Accepter]").hoverEvent(Component.text("§aCliquez pour accepter sa demande de téléportation.")).clickEvent(ClickEvent.runCommand("/tpaccept tpa " + targetName)))
                .append(Component.text(" §c§l[Refuser]").hoverEvent(Component.text("§cCliquez pour refuser sa demande de téléportation.")).clickEvent(ClickEvent.runCommand("/tpadeny tpa " + targetName)))
                .append(Component.text("\n"));
    }

    public Component getTpaHereComponent(String targetName) {
        return Component.text("\n§e" + targetName + " §7vous demande s'il peut §e§lVOUS TÉLÉPORTER §7à lui. (Expire dans 30 secondes)\n\n")
                .append(Component.text("§a[Accepter]").hoverEvent(Component.text("§aCliquez pour accepter sa demande de téléportation.")).clickEvent(ClickEvent.runCommand("/tpaccept tpahere " + targetName)))
                .append(Component.text(" §c[Refuser]").hoverEvent(Component.text("§cCliquez pour refuser sa demande de téléportation.")).clickEvent(ClickEvent.runCommand("/tpadeny tpahere " + targetName)))
                .append(Component.text("\n"));
    }

    public void createTpaRequest(UUID sender, String senderName, UUID receiver, String receiverName, boolean isTpaHere) {
        addTpaRequest(new TpaRequest(sender, senderName, receiver, receiverName, System.currentTimeMillis(), isTpaHere));


        JedisManager.INSTANCE.publishToRedis("coreskyblock", "tpa_request:" + sender + ":" + senderName + ":"
                + receiver + ":" + receiverName + ":" + System.currentTimeMillis() + ":" + isTpaHere + ":" + CoreSkyblock.SERVER_NAME);
    }

    public void addTpaRequest(TpaRequest request) {
        tpaRequests.add(request);
    }

    public boolean alreadyHasTpaRequest(UUID sender, UUID receiver) {
        return tpaRequests.stream().anyMatch(request -> request.sender().equals(sender) && request.receiver().equals(receiver) && !request.isTpaHere());
    }

    public boolean alreadyHasTpaHereRequest(UUID sender, UUID receiver) {
        return tpaRequests.stream().anyMatch(request -> request.sender().equals(sender) && request.receiver().equals(receiver) && request.isTpaHere());
    }

    public long getTpaRequestExpireTime(UUID sender, UUID receiver) {
        return System.currentTimeMillis() - tpaRequests.stream().filter(request -> request.sender().equals(sender) && request.receiver().equals(receiver)).findFirst().map(TpaRequest::timestamp).orElse(-1L);
    }

    public boolean onJoin(Player p) {
        if (incomingPlayersTpa.containsKey(p.getUniqueId())) {
            UUID sender = incomingPlayersTpa.get(p.getUniqueId());
            Player teleportTo = Bukkit.getPlayer(sender);
            if (teleportTo != null) {
                p.teleport(teleportTo);
                p.sendMessage(Component.text("§7Vous avez été téléporté à §e" + teleportTo.getName() + "§7."));
            } else {
                p.sendMessage(Component.text("§cUne erreur est survenue lors de votre téléportation."));
            }
            incomingPlayersTpa.remove(p.getUniqueId());
            return true;
        }
        return false;
    }

    public void removeTpaRequest(UUID sender, UUID receiver, boolean tpaHere) {
        tpaRequests.removeIf(request -> request.sender().equals(sender) && request.receiver().equals(receiver) && request.isTpaHere() == tpaHere);
    }
}
