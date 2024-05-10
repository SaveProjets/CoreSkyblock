package fr.farmeurimmo.coreskyblock.purpur.chests;

import fr.farmeurimmo.coreskyblock.ServerType;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.shop.ShopsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class ChestsManager {

    public static ChestsManager INSTANCE;

    public ChestsManager() {
        INSTANCE = this;

        if (CoreSkyblock.SERVER_TYPE == ServerType.GAME)
            Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, this::autoSellForSellChests, 0, 20 * 60);
    }

    public void giveItem(Player p, ChestType type, int tier) {
        p.getInventory().addItem(getItemStack(type, tier));
    }

    public ItemStack getItemStack(ChestType type, int tier) {
        ItemStack item = new ItemStack(type.getMaterial());

        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text((type == ChestType.SELL_CHEST ? getNameFromTier(tier) : type.getName())));
        ArrayList<Component> lore = new ArrayList<>();
        type.getLore().forEach(s -> lore.add(Component.text(s)));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        item.setUnbreakable(true);

        return item;
    }

    public String getNameFromTier(int tier) {
        return SellChestTiers.values()[tier].getNormalName() + " Sell Chest";
    }

    public int getTierFromName(String name) {
        for (SellChestTiers tier : SellChestTiers.values()) {
            if (name.contains(tier.getNormalName())) {
                return tier.ordinal();
            }
        }
        return 0;
    }

    public void autoSellForSellChests() {
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (!island.isLoaded()) continue;
            if (island.isReadOnly()) continue;

            for (Chest chest : island.getChests()) {
                if (chest.getType() != ChestType.SELL_CHEST) continue;
                SellChestTiers tier = SellChestTiers.values()[chest.getTier()];
                double sellMultiplier = tier.getSellMultiplier();

                Block block = chest.getBlock().getBlock();
                if (block.getState() instanceof org.bukkit.block.Chest c) {
                    double moneyMade = 0;
                    for (ItemStack itemStack : c.getInventory().getContents()) {
                        if (itemStack == null) continue;
                        if (ShopsManager.INSTANCE.getSellPrice(itemStack) == 0) continue;

                        moneyMade += ShopsManager.INSTANCE.getSellPrice(itemStack) * itemStack.getAmount();
                        c.getInventory().remove(itemStack);
                    }
                    moneyMade *= sellMultiplier;
                    island.setBankMoney(island.getBankMoney() + moneyMade);
                }
            }
        }
    }
}
