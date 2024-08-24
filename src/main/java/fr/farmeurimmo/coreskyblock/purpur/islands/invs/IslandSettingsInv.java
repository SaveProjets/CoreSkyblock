package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandSettings;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class IslandSettingsInv extends FastInv {

    private static final int[] SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
    private int page = 0;
    private boolean closed = false;

    public IslandSettingsInv(Island island) {
        super(54, "§8Paramètres de l'île");

        updateSettings(island);

        setCloseFilter(p -> {
            closed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
            if (closed) {
                task.cancel();
                return;
            }
            updateSettings(island);
        }, 0, 40L);
    }

    private void updateSettings(Island island) {
        setItems(SLOTS, null);

        CommonItemStacks.applyCommonPanes(Material.RED_STAINED_GLASS_PANE, getInventory());

        ArrayList<IslandSettings> settings = getSettings();

        int currentSlot = 0;
        for (int element = page * SLOTS.length; element < settings.size(); element++) {
            if (currentSlot >= SLOTS.length) break;
            IslandSettings setting = settings.get(element);
            ItemStack custom = IslandSettings.getItemForSetting(setting);
            if (custom.getType() == Material.CLOCK && !island.hasSettingActivated(setting)) continue;
            if (custom.getType() == Material.DAYLIGHT_DETECTOR && !island.hasSettingActivated(setting)) continue;
            custom.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            ItemMeta meta = custom.getItemMeta();
            meta.lore();
            custom.setItemMeta(meta);

            boolean activated = island.hasSettingActivated(setting);
            setItem(SLOTS[currentSlot], ItemBuilder.copyOf(custom).name(setting.getDisplayName())
                    .lore(setting.getDescription(activated, setting)).build(), e -> {
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

        if (getSettingsOfIsland(island).size() > (page + 1) * SLOTS.length) {
            setItem(50, CommonItemStacks.getCommonNextPage(), e -> {
                page++;
                updateSettings(island);
            });
        }

        setItem(49, CommonItemStacks.getCommonBack(), e ->
                new IslandInv(island).open((Player) e.getWhoClicked()));
    }

    private ArrayList<IslandSettings> getSettings() {
        return new ArrayList<>(List.of(IslandSettings.values()));
    }

    private ArrayList<IslandSettings> getSettingsOfIsland(Island island) {
        ArrayList<IslandSettings> settings = new ArrayList<>();
        for (IslandSettings setting : IslandSettings.values()) {
            if (island.hasSettingActivated(setting)) {
                settings.add(setting);
            }
        }
        return settings;
    }
}
