package fr.farmeurimmo.coreskyblock.purpur.items.enchants;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.EnchantmentRarity;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import fr.farmeurimmo.coreskyblock.purpur.items.sacs.SacsManager;
import fr.farmeurimmo.coreskyblock.purpur.items.sacs.SacsType;
import fr.farmeurimmo.coreskyblock.utils.RomanNumberUtils;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class CustomEnchantmentsManager {

    public static final String ENCHANTMENT_LORE_SEPARATOR = "§  ";
    public static final ArrayList<FurnaceRecipe> SMELL_RECIPES = new ArrayList<>();
    public static CustomEnchantmentsManager INSTANCE;
    public static ArrayList<Material> SMELTING_ALLOWED_MATERIALS = new ArrayList<>(Arrays.asList(
            Material.COBBLESTONE, Material.STONE, Material.RAW_IRON, Material.RAW_GOLD, Material.RAW_COPPER,
            Material.RAW_COPPER));
    public final Map<UUID, Map<String, Long>> abilityCooldowns = new HashMap<>();

    public CustomEnchantmentsManager() {
        INSTANCE = this;

        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                SMELL_RECIPES.add(furnaceRecipe);
            }
        });

        Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                checkForArmorEffects(p);
            }
        }, 0, 20 * 15);
    }

    public ItemStack getItemStackWithEnchantsApplied(ArrayList<Pair<Enchantments, Integer>> enchantments, ItemStack itemStack) {
        if (enchantments.isEmpty()) {
            itemStack.lore(null);

            return itemStack;
        }
        List<Component> existingLore = itemStack.lore() != null ? new ArrayList<>(Objects.requireNonNull(itemStack.lore())) : new ArrayList<>();
        if (!existingLore.isEmpty()) {
            List<String> loreString = itemStack.getLore();
            assert loreString != null;
            for (String lore : loreString) {
                for (Enchantments enchantment : Enchantments.values()) {
                    if (lore.contains(enchantment.getDisplayName())) {
                        existingLore.remove(Component.text(lore));
                    }
                }
            }
        }

        List<Component> newLore = getEnchantmentsOrderedByRarityFromList(enchantments).stream()
                .map(enchantment -> Component.text(enchantment.left().getDisplayName() +
                        (enchantment.left().getMaxLevel() > 1 ? ENCHANTMENT_LORE_SEPARATOR + RomanNumberUtils.toRoman(enchantment.right()) : "")))
                .collect(Collectors.toList());
        existingLore.addAll(newLore);

        itemStack.lore(existingLore);

        for (Pair<Enchantments, Integer> enchantment : enchantments) {
            /*if (enchantment.left().equals(Enchantments.GAIN_DE_VIE)) { //FIXME: Custom stats system to fix GAIN_DE_VIE
                itemStack.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier("gain_de_vie",
                        enchantment.left().getValueForLevel(enchantment.right()), AttributeModifier.Operation.ADD_NUMBER));
            }*/
        }

        return itemStack;
    }

    public LinkedList<Pair<Enchantments, Integer>> getEnchantmentsOrderedByRarityFromList(ArrayList<Pair<Enchantments, Integer>> enchantments) {
        LinkedList<Pair<Enchantments, Integer>> orderedEnchantments = new LinkedList<>();
        for (EnchantmentRarity rarity : EnchantmentRarity.values()) {
            for (Pair<Enchantments, Integer> enchantment : enchantments) {
                if (enchantment.left().getRarity() == rarity) {
                    orderedEnchantments.add(enchantment);
                }
            }
        }
        return orderedEnchantments;
    }

    public ItemStack getItemStackEnchantedBook(Enchantments enchantment, int level) {
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
        itemStack.editMeta(meta -> meta.displayName(Component.text(enchantment.getDisplayName() + (enchantment.getMaxLevel() > 1 ?
                ENCHANTMENT_LORE_SEPARATOR + RomanNumberUtils.toRoman(level) : ""))));
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
                        level = RomanNumberUtils.fromRoman(lore.split(ENCHANTMENT_LORE_SEPARATOR)[1]);
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
            if (itemStack.getItemMeta().getDisplayName().contains(enchantment.getDisplayName())) {
                int level = 0;
                if (itemStack.getItemMeta().getDisplayName().contains(ENCHANTMENT_LORE_SEPARATOR)) {
                    level = RomanNumberUtils.fromRoman(itemStack.getItemMeta().getDisplayName().split(ENCHANTMENT_LORE_SEPARATOR)[1]);
                }
                enchantments.add(Pair.of(enchantment, level));
            }
        }
        return enchantments;
    }

    public Optional<ArrayList<Pair<Enchantments, Integer>>> getValidEnchantments(ItemStack item) {
        if (item == null) {
            return Optional.empty();
        }
        if (item.getType().isAir() || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return Optional.empty();
        }

        return (item.getType() == Material.ENCHANTED_BOOK) ? Optional.of(getEnchantmentsFromDisplayName(item)) : Optional.of(getEnchantmentsFromLore(item));
    }

    public LinkedHashMap<Enchantments, List<ItemStack>> getAllEnchantedBooks() {
        LinkedHashMap<Enchantments, List<ItemStack>> enchantedBooks = new LinkedHashMap<>();
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

    public void applyAimant(ArrayList<ItemStack> drops, Player p) {
        for (ItemStack drop : drops) {
            SacsType sacsType = SacsType.getByMaterial(drop.getType());
            if (sacsType != null) {
                for (ItemStack itemStack : p.getInventory().getStorageContents()) {
                    if (itemStack == null) continue;
                    if (!itemStack.hasItemMeta()) continue;
                    if (itemStack.lore() == null) continue;
                    if (!itemStack.getItemFlags().contains(ItemFlag.HIDE_UNBREAKABLE)) continue;

                    if (SacsManager.INSTANCE.isASacs(itemStack, sacsType)) {
                        int amount = SacsManager.INSTANCE.getAmount(itemStack, sacsType);

                        if (amount == -1) continue;

                        int newAmount = amount + drop.getAmount();

                        if (newAmount > SacsManager.MAX_AMOUNT) {
                            amount -= newAmount - SacsManager.MAX_AMOUNT;
                            newAmount = SacsManager.MAX_AMOUNT;
                            drop.setAmount(amount);
                        } else {
                            drop.setAmount(0);
                        }

                        SacsManager.INSTANCE.setAmount(itemStack, sacsType, newAmount);
                    }
                }
            }
            if (drop.getAmount() == 0) continue;
            if (p.getInventory().firstEmpty() == -1) {
                p.getWorld().dropItem(p.getLocation(), drop);
            } else {
                p.getInventory().addItem(drop);
            }
        }
    }

    public void checkForArmorEffects(Player p) {
        for (ItemStack itemStack : p.getInventory().getArmorContents()) {
            Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments = getValidEnchantments(itemStack);
            if (enchantments.isEmpty()) {
                Optional<PotionEffect> potionEffect = p.getActivePotionEffects().stream().filter(potionEffect1 ->
                        potionEffect1.getType() == PotionEffectType.NIGHT_VISION).findFirst();

                if (potionEffect.isPresent()) {
                    if (!potionEffect.get().hasParticles()) {
                        p.removePotionEffect(PotionEffectType.NIGHT_VISION);
                    }
                }
                continue;
            }

            ArrayList<Pair<Enchantments, Integer>> enchantmentsList = enchantments.get();
            if (enchantmentsList.stream().anyMatch(enchantmentsIntegerPair -> enchantmentsIntegerPair.left() == Enchantments.LAMPE_TORCHE)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, false, false));
            } else {
                Optional<PotionEffect> potionEffect = p.getActivePotionEffects().stream().filter(potionEffect1 ->
                        potionEffect1.getType() == PotionEffectType.NIGHT_VISION).findFirst();

                if (potionEffect.isPresent()) {
                    if (!potionEffect.get().hasParticles()) {
                        p.removePotionEffect(PotionEffectType.NIGHT_VISION);
                    }
                }
            }
        }
    }

    public float getRng() {
        return new Random().nextFloat();
    }

    public boolean isInCooldown(UUID uuid, String ability) {
        return getCooldownLeft(uuid, ability) > 0;
    }

    public void addAbilityCooldown(UUID uuid, String ability, long cooldown) {
        if (!abilityCooldowns.containsKey(uuid)) {
            abilityCooldowns.put(uuid, new HashMap<>());
        }
        abilityCooldowns.get(uuid).put(ability, System.currentTimeMillis() + cooldown * 1_000);
    }

    public int getCooldownLeft(UUID uuid, String ability) {
        if (!abilityCooldowns.containsKey(uuid)) {
            return 0;
        }
        if (!abilityCooldowns.get(uuid).containsKey(ability)) {
            return 0;
        }
        return (int) Math.max(0, (abilityCooldowns.get(uuid).get(ability) - System.currentTimeMillis()) / 1_000);
    }
}
