package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandSettings;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class IslandSettingsInv extends FastInv {

    private static final int[] SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
    private int page = 0;

    public IslandSettingsInv(Island island) {
        super(54, "§8Paramètres de l'île");

        updateSettings(island);
    }

    private void updateSettings(Island island) {
        for (int i = 0; i < getInventory().getSize(); i++) {
            setItem(i, null);
        }

        CommonItemStacks.applyCommonPanes(Material.RED_STAINED_GLASS_PANE, getInventory());

        ArrayList<IslandSettings> settings = getSettings(island);

        int currentSlot = 0;
        for (int element = page * SLOTS.length; element < settings.size(); element++) {
            if (currentSlot >= SLOTS.length) break;
            IslandSettings setting = settings.get(element);
            ItemStack custom = IslandSettings.getItemForSetting(setting);
            if (custom.getType() == Material.CLOCK && !island.hasSettingActivated(setting)) continue;
            if (custom.getType() == Material.DAYLIGHT_DETECTOR && !island.hasSettingActivated(setting)) continue;
            ItemMeta meta = custom.getItemMeta();
            meta.lore();
            custom.setItemMeta(meta);
            IslandSettings next = IslandSettings.getNext(setting);

            boolean activated = island.hasSettingActivated(setting);
            setItem(SLOTS[currentSlot], ItemBuilder.copyOf(custom).name(setting.getDesc()).lore(List.of(activated ? "§aActivé"
                    : "§cDésactivé", "", "§eClic pour " + (next != null ? "passer à " + next.getDesc() : (activated ?
                    "§cdésactiver" : "§aactiver")))).build(), e -> {
                if (island.isReadOnly()) {
                    IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
                    return;
                }
                if (!island.hasPerms(island, IslandPerms.EDIT_SETTINGS, e.getWhoClicked().getUniqueId())) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas la permission de modifier les paramètres de l'île."));
                    return;
                }
                if (e.getCurrentItem() == null) return;
                if (e.getCurrentItem().getType() == Material.CLOCK || e.getCurrentItem().getType() == Material.DAYLIGHT_DETECTOR) {
                    island.removeSetting(setting);
                    island.addSetting(IslandSettings.getNext(setting));
                    IslandsManager.INSTANCE.applyTimeAndWeather(IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID()), island);
                    updateSettings(island);
                    return;
                }
                if (island.hasSettingActivated(setting)) {
                    island.removeSetting(setting);
                } else {
                    island.addSetting(setting);
                }

                updateSettings(island);
            });
            currentSlot++;
        }

        if (page > 0) {
            setItem(48, CommonItemStacks.getCommonPreviousPage(), e -> {
                page--;
                updateSettings(island);
            });
        }

        if (settings.size() > (page + 1) * SLOTS.length) {
            setItem(50, CommonItemStacks.getCommonNextPage(), e -> {
                page++;
                updateSettings(island);
            });
        }

        setItem(49, CommonItemStacks.getCommonBack(), e ->
                new IslandInv(island).open((Player) e.getWhoClicked()));
    }

    private ArrayList<IslandSettings> getSettings(Island island) {
        ArrayList<IslandSettings> settings = new ArrayList<>();
        for (IslandSettings setting : IslandSettings.values()) {
            if (island.hasSettingActivated(setting)) {
                settings.add(setting);
            }
        }
        return settings;
    }
}
