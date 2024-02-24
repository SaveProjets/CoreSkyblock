package fr.farmeurimmo.coreskyblock.purpur.chat;

import fr.farmeurimmo.coreskyblock.common.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.common.skyblockusers.SkyblockUsersManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class ChatDisplayManager {

    public static ChatDisplayManager INSTANCE;

    public ChatDisplayManager() {
        INSTANCE = this;
    }

    public Component getComponentForItem(ItemStack item) {
        return Component.text("[" + (item.hasDisplayName() ? item.getDisplayName() :
                        Objects.requireNonNull(item.getType().getKey()).getKey()) + "]").color(NamedTextColor.GOLD)
                .hoverEvent(item.asHoverEvent(UnaryOperator.identity()));
    }

    public Component getComponentForMoney(Player p) {
        SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
        if (user == null) {
            return Component.text("§6[Argent : §cErreur INTERNE$§6]§f");
        }
        return Component.text("§6[Argent]§f").hoverEvent(HoverEvent.showText(Component.text(
                "§7Solde : §6" + NumberFormat.getInstance().format(user.getMoney()) + " §6$")));
    }
}
