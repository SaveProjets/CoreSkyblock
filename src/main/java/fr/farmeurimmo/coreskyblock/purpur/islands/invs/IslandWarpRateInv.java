package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsWarpManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandWarp;
import fr.farmeurimmo.coreskyblock.utils.DateUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class IslandWarpRateInv extends FastInv {

    public IslandWarpRateInv() {
        super(27, "§8Notez ce warp");

        int slot = 10;
        for (int i = -2; i < 3; i++) {
            int finalI = i;
            setItem(slot, new ItemBuilder(new ItemStack(getMaterialFromRate(i)))
                    .name("§6Note: " + getRateName(i))
                    .lore("§7Cliquez pour attribuer cette note")
                    .build(), e -> {
                e.getWhoClicked().closeInventory();

                Island island = IslandsManager.INSTANCE.getIslandByLoc(e.getWhoClicked().getWorld());
                if (island == null) {
                    e.getWhoClicked().sendMessage(Component.text("§cErreur: Vous n'êtes pas sur une île."));
                    return;
                }
                IslandWarp warp = IslandsWarpManager.INSTANCE.getByIslandUUID(island.getIslandUUID());
                if (warp == null) {
                    e.getWhoClicked().sendMessage(Component.text("§cErreur: Aucun warp trouvé pour cette île."));
                    return;
                }
                if (!warp.isActivated()) {
                    e.getWhoClicked().sendMessage(Component.text("§cErreur: Ce warp n'est pas activé."));
                    return;
                }
                if (!warp.canRate(e.getWhoClicked().getUniqueId())) {
                    e.getWhoClicked().sendMessage(Component.text("§cErreur: Vous êtes en cooldown pour " +
                            "noter ce warp. De nouveau dans " + DateUtils.getFormattedTimeLeft((int)
                            (warp.timeBeforeNextRate(e.getWhoClicked().getUniqueId()) / 1000L)) + "."));
                    return;
                }
                warp.applyRate(e.getWhoClicked().getUniqueId(), finalI);
                e.getWhoClicked().sendMessage(Component.text("§aNote attribuée avec succès."));
            });

            slot++;
            if (i == -1) slot++;
            if (i == 0) slot++;
        }

        setItem(22, new ItemBuilder(Material.IRON_DOOR).name("§6Fermer").build(),
                e -> e.getWhoClicked().closeInventory());
    }

    private Material getMaterialFromRate(int rate) {
        if (rate == -2) return Material.RED_WOOL;
        if (rate == -1) return Material.ORANGE_WOOL;
        if (rate == 0) return Material.YELLOW_WOOL;
        if (rate == 1) return Material.LIME_WOOL;
        if (rate == 2) return Material.GREEN_WOOL;
        return Material.GRAY_WOOL;
    }

    private String getRateName(int rate) {
        if (rate == -2) return "§4Très mauvais";
        if (rate == -1) return "§cMauvais";
        if (rate == 0) return "§eMoyen";
        if (rate == 1) return "§aBon";
        if (rate == 2) return "§2Très bon";
        return "§4§lErreur";
    }

}
