package fr.farmeurimmo.coreskyblock.purpur.chests;

import fr.farmeurimmo.coreskyblock.ServerType;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.shop.ShopsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandsDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class ChestsManager {

    public static ChestsManager INSTANCE;

    public ChestsManager() {
        INSTANCE = this;

        if (CoreSkyblock.SERVER_TYPE == ServerType.GAME)
            Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, this::autoSellForSellChests, 0, 20 * 60);

        registerRecipes();
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
        return ChestType.SELL_CHEST.getName() + " " + SellChestTiers.values()[tier].getNormalName();
    }

    public int getTierFromName(String name) {
        for (SellChestTiers tier : SellChestTiers.values()) {
            if (name.contains(tier.getNormalName())) {
                return tier.ordinal();
            }
        }
        return 0;
    }

    public void registerRecipes() {
        NamespacedKey keyUncommon = new NamespacedKey(CoreSkyblock.INSTANCE, "sell_chest_uncommon");
        NamespacedKey keyRare = new NamespacedKey(CoreSkyblock.INSTANCE, "sell_chest_rare");
        NamespacedKey keyEpic = new NamespacedKey(CoreSkyblock.INSTANCE, "sell_chest_epic");
        NamespacedKey keyLegendary = new NamespacedKey(CoreSkyblock.INSTANCE, "sell_chest_legendary");
        NamespacedKey keyMythic = new NamespacedKey(CoreSkyblock.INSTANCE, "sell_chest_mythic");

        ShapedRecipe recipeUncommon = new ShapedRecipe(keyUncommon, getItemStack(ChestType.SELL_CHEST, 1));
        ShapedRecipe recipeRare = new ShapedRecipe(keyRare, getItemStack(ChestType.SELL_CHEST, 2));
        ShapedRecipe recipeEpic = new ShapedRecipe(keyEpic, getItemStack(ChestType.SELL_CHEST, 3));
        ShapedRecipe recipeLegendary = new ShapedRecipe(keyLegendary, getItemStack(ChestType.SELL_CHEST, 4));
        ShapedRecipe recipeMythic = new ShapedRecipe(keyMythic, getItemStack(ChestType.SELL_CHEST, 5));

        recipeUncommon.shape("CC");
        recipeUncommon.setIngredient('C', ChestsManager.INSTANCE.getItemStack(ChestType.SELL_CHEST, 0));

        recipeRare.shape("CC");
        recipeRare.setIngredient('C', ChestsManager.INSTANCE.getItemStack(ChestType.SELL_CHEST, 1));

        recipeEpic.shape("CC");
        recipeEpic.setIngredient('C', ChestsManager.INSTANCE.getItemStack(ChestType.SELL_CHEST, 2));

        recipeLegendary.shape("CC");
        recipeLegendary.setIngredient('C', ChestsManager.INSTANCE.getItemStack(ChestType.SELL_CHEST, 3));

        recipeMythic.shape("CC");
        recipeMythic.setIngredient('C', ChestsManager.INSTANCE.getItemStack(ChestType.SELL_CHEST, 4));

        Bukkit.addRecipe(recipeUncommon);
        Bukkit.addRecipe(recipeRare);
        Bukkit.addRecipe(recipeEpic);
        Bukkit.addRecipe(recipeLegendary);
        Bukkit.addRecipe(recipeMythic);
    }

    public void autoSellForSellChests() {
        for (Island island : IslandsDataManager.INSTANCE.getCache().values()) {
            if (!island.isLoaded()) continue;
            if (island.isReadOnly()) continue;

            for (Chest chest : island.getChests()) {
                if (chest.getType() != ChestType.SELL_CHEST) continue;
                SellChestTiers tier = SellChestTiers.values()[chest.getTier()];
                double sellMultiplier = tier.getSellMultiplier();

                if (chest.getBlock() == null) continue;

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
                    if (moneyMade > 0) island.setBankMoney(island.getBankMoney() + moneyMade);
                }
            }
        }
    }
}
