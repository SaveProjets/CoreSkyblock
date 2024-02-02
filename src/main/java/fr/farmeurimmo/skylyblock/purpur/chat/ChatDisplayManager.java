package fr.farmeurimmo.skylyblock.purpur.chat;

import fr.farmeurimmo.skylyblock.common.SkyblockUser;
import fr.farmeurimmo.skylyblock.common.SkyblockUsersManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.Objects;

public class ChatDisplayManager {

    public static ChatDisplayManager INSTANCE;

    public ChatDisplayManager() {
        INSTANCE = this;
    }

    public Component getComponentForItem(ItemStack item) {
        if (!item.hasItemMeta()) {
            return Component.text("[" + item.getType().getKey().getKey() + "]").color(NamedTextColor.GOLD);
        }
        ItemMeta itemMeta = item.getItemMeta();

        Component displayName = Component.text((item.hasDisplayName() ? item.getDisplayName() :
                Objects.requireNonNull(item.getType().getKey()).getKey())).color(NamedTextColor.GOLD);

        Component lore = Component.text("");
        if (itemMeta.lore() != null) {
            for (Component component : Objects.requireNonNull(itemMeta.lore())) {
                lore = lore.append(component).append(Component.newline());
            }
        }

        return Component.text()
                .append(Component.text("[").color(NamedTextColor.GRAY))
                .append(Component.text("x" + item.getAmount()).color(NamedTextColor.GOLD))
                .append(Component.text(" "))
                .append(displayName)
                .append(Component.text("]").color(NamedTextColor.GRAY))
                .hoverEvent(HoverEvent.showText(lore))
                .build();
    }

    public Component getComponentForMoney(Player p) {
        SkyblockUser user = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
        if (user == null) {
            return Component.text("§6[Argent : §cErreur INTERNE$§6]§f");
        }
        return Component.text("§6[Argent : " + NumberFormat.getInstance().format(user.getMoney()) + "$]§f");
    }
}
