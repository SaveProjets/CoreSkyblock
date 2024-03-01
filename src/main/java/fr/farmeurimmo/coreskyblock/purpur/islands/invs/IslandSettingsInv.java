package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandSettings;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class IslandSettingsInv extends FastInv {

    public IslandSettingsInv(Island island) {
        super(36, "§8Paramètres de l'île");

        setItem(35, ItemBuilder.copyOf(new ItemStack(Material.ARROW))
                .name("§6Retour §8| §7(clic gauche)").build(), e ->
                new IslandInv(island).open((Player) e.getWhoClicked()));

        setItem(27, ItemBuilder.copyOf(new ItemStack(Material.KNOWLEDGE_BOOK))
                .name("§6Informations complémentaires").lore("§aActivé §7signifie que les actions peuvent s'effectuer",
                        "§cDésactivé §7signifie que les actions ne peuvent pas s'effectuer").build());

        updateSettings(island);
    }

    private void updateSettings(Island island) {
        int currentSlot = 10;
        for (IslandSettings setting : IslandSettings.values()) {
            ItemStack custom = IslandSettings.getItemForSetting(setting);
            if (custom.getType() == Material.CLOCK && !island.hasSettingActivated(setting)) continue;
            if (custom.getType() == Material.DAYLIGHT_DETECTOR && !island.hasSettingActivated(setting)) continue;
            ItemMeta meta = custom.getItemMeta();
            meta.lore();
            custom.setItemMeta(meta);
            IslandSettings next = IslandSettings.getNext(setting);

            boolean activated = island.hasSettingActivated(setting);
            setItem(currentSlot, ItemBuilder.copyOf(custom).name(setting.getDesc()).lore(List.of(activated ? "§aActivé"
                    : "§cDésactivé", "", "§eClic pour " + (next != null ? "passer à " + next.getDesc() : (activated ?
                    "§cdésactiver" : "§aactiver")))).build(), e -> {
                if (island.isReadOnly()) {
                    IslandsManager.INSTANCE.sendPlayerIslandReadOnly((Player) e.getWhoClicked());
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
    }
}
