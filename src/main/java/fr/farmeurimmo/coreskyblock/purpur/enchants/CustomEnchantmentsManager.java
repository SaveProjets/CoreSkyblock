package fr.farmeurimmo.coreskyblock.purpur.enchants;

import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.Enchantments;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class CustomEnchantmentsManager {

    public static final String ENCHANTMENT_LORE_SEPARATOR = "§  ";
    public static final ArrayList<FurnaceRecipe> smellRecipes = new ArrayList<>();
    public static CustomEnchantmentsManager INSTANCE;

    public CustomEnchantmentsManager() {
        INSTANCE = this;

        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                smellRecipes.add(furnaceRecipe);
            }
        });
    }

    public ItemStack getItemStackWithEnchantsApplied(List<Enchantments> enchantments, ItemStack itemStack) {
        itemStack.lore(enchantments.stream().map(enchantment -> {
            Component component = Component.text(enchantment.getDisplayName());
            if (enchantment.hasMaxLevel()) {
                component = component.append(Component.text(ENCHANTMENT_LORE_SEPARATOR + enchantment.getMaxLevel()));
            }
            return component;
        }).collect(Collectors.toList()));

        return itemStack;
    }

    public ItemStack getItemStackEnchantedBook(Enchantments enchantment, int level) {
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
        itemStack.setDisplayName(enchantment.getDisplayName() + (enchantment.getMaxLevel() > 1 ? ENCHANTMENT_LORE_SEPARATOR + level : "")
                + ENCHANTMENT_LORE_SEPARATOR + "§0" + UUID.randomUUID().toString().substring(0, 4));
        itemStack.lore(enchantment.getDescriptionFormatted(level));

        return itemStack;
    }

    private ArrayList<Pair<Enchantments, Integer>> getEnchantmentsFromLore(ItemStack itemStack) {
        ArrayList<Pair<Enchantments, Integer>> enchantments = new ArrayList<>();
        for (String lore : Objects.requireNonNull(itemStack.getLore())) {
            for (Enchantments enchantment : Enchantments.values()) {
                if (lore.contains(enchantment.getDisplayName())) {
                    int level = 0;
                    if (lore.contains(ENCHANTMENT_LORE_SEPARATOR)) {
                        level = Integer.parseInt(lore.split(ENCHANTMENT_LORE_SEPARATOR)[1]);
                    }
                    enchantments.add(Pair.of(enchantment, level));
                }
            }
        }
        return enchantments;
    }

    private ArrayList<Pair<Enchantments, Integer>> getEnchantmentsFromDisplayName(ItemStack itemStack) {
        ArrayList<Pair<Enchantments, Integer>> enchantments = new ArrayList<>();
        for (Enchantments enchantment : Enchantments.values()) {
            if (itemStack.getDisplayName().contains(enchantment.getDisplayName())) {
                int level = 0;
                if (itemStack.getDisplayName().contains(ENCHANTMENT_LORE_SEPARATOR)) {
                    level = Integer.parseInt(itemStack.getDisplayName().split(ENCHANTMENT_LORE_SEPARATOR)[1]);
                }
                enchantments.add(Pair.of(enchantment, level));
            }
        }
        return enchantments;
    }

    public Optional<ArrayList<Pair<Enchantments, Integer>>> getValidEnchantments(ItemStack item) {
        if (item.getType().isAir() || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return Optional.empty();
        }

        return (item.getType() == Material.ENCHANTED_BOOK) ? Optional.of(getEnchantmentsFromDisplayName(item)) : Optional.of(getEnchantmentsFromLore(item));
    }

    public Map<Enchantments, List<ItemStack>> getAllEnchantedBooks() {
        Map<Enchantments, List<ItemStack>> enchantedBooks = new HashMap<>();
        for (Enchantments enchantment : Enchantments.values()) {
            enchantedBooks.put(enchantment, new ArrayList<>());
            if (enchantment.getMaxLevel() == -1) {
                enchantedBooks.get(enchantment).add(getItemStackEnchantedBook(enchantment, 1));
                continue;
            }
            for (int i = 1; i <= enchantment.getMaxLevel(); i++) {
                enchantedBooks.get(enchantment).add(getItemStackEnchantedBook(enchantment, i));
            }
        }

        return enchantedBooks;
    }

}
