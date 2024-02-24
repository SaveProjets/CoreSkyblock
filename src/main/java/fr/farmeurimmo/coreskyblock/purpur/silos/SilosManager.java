package fr.farmeurimmo.coreskyblock.purpur.silos;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SilosManager {

    public static final String NAME_FORMAT = "§lSilos de %s";
    public static final String LORE_FORMAT = "§6x%d %s";
    public static final int MAX_AMOUNT = 5_000;
    public static SilosManager INSTANCE;

    public SilosManager() {
        INSTANCE = this;

        registerCrafts();
    }

    public void registerCrafts() {
        for (SilosType silosType : SilosType.values()) {
            ItemStack itemStack = createSilo(silosType);
            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(CoreSkyblock.INSTANCE,
                    "silo_" + silosType.getMaterial().name().toLowerCase()), itemStack);

            recipe.shape("ABC", "DSE", "FGH");
            recipe.setIngredient('S', new ItemStack(Material.CHEST));

            recipe.setIngredient('A', new RecipeChoice.ExactChoice(new ItemStack(silosType.getMaterial(), 48)));
            recipe.setIngredient('B', new RecipeChoice.ExactChoice(new ItemStack(silosType.getMaterial(), 48)));
            recipe.setIngredient('C', new RecipeChoice.ExactChoice(new ItemStack(silosType.getMaterial(), 48)));
            recipe.setIngredient('D', new RecipeChoice.ExactChoice(new ItemStack(silosType.getMaterial(), 48)));
            recipe.setIngredient('E', new RecipeChoice.ExactChoice(new ItemStack(silosType.getMaterial(), 48)));
            recipe.setIngredient('F', new RecipeChoice.ExactChoice(new ItemStack(silosType.getMaterial(), 48)));
            recipe.setIngredient('G', new RecipeChoice.ExactChoice(new ItemStack(silosType.getMaterial(), 48)));
            recipe.setIngredient('H', new RecipeChoice.ExactChoice(new ItemStack(silosType.getMaterial(), 48)));

            Bukkit.addRecipe(recipe);
        }
    }

    public boolean hasSiloInInventory(Player player, SilosType silosType) {
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (isASilo(itemStack, silosType)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack createSilo(SilosType silosType) {
        ItemStack itemStack = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(String.format(NAME_FORMAT, silosType.getName())));
        itemMeta.lore(List.of(Component.text(LORE_FORMAT.formatted(0, silosType.getName())),
                Component.text("§0" + UUID.randomUUID()),
                Component.text("§7Stocke tous/toute vos/votre " + silosType.getName().toLowerCase()),
                Component.text("§7cultures en un seul endroit."), Component.text(""),
                Component.text("§7Clic droit pour ouvrir.")));
        itemMeta.setUnbreakable(true);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public boolean isASilo(ItemStack itemStack, SilosType silosType) {
        return getAmount(itemStack, silosType) != -1;
    }

    public boolean isASilo(ItemStack itemStack) {
        return getSilosType(itemStack) != null;
    }

    public SilosType getSilosType(ItemStack itemStack) {
        for (SilosType silosType : SilosType.values()) {
            if (isASilo(itemStack, silosType)) {
                return silosType;
            }
        }
        return null;
    }

    public int getAmount(ItemStack itemStack, SilosType silosType) {
        if (itemStack == null) return -1;
        if (itemStack.getType() != Material.CHEST) return -1;
        if (!itemStack.hasItemMeta()) return -1;
        if (!itemStack.isUnbreakable()) return -1;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.displayName() == null) return -1;
        if (itemMeta.lore() == null || Objects.requireNonNull(itemMeta.lore()).size() < 3) return -1;
        if (!Objects.equals(itemStack.getDisplayName(), String.format(NAME_FORMAT, silosType.getName()))) return -1;
        String lore = Objects.requireNonNull(itemMeta.getLore()).get(0);
        int amount = Integer.parseInt(lore.substring(lore.indexOf("x") + 1, lore.indexOf(" ")));
        if (amount >= MAX_AMOUNT) return -2;
        return amount;
    }

    public void setAmount(ItemStack itemStack, SilosType silosType, int amount) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<Component> lore = itemMeta.lore();
        if (lore == null || lore.size() < 3) {
            return;
        }
        lore.set(0, Component.text(LORE_FORMAT.formatted(amount, silosType.getName())));
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
    }
}
