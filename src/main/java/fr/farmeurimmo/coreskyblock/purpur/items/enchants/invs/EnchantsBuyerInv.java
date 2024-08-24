package fr.farmeurimmo.coreskyblock.purpur.items.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.EnchantmentRarity;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.enums.Enchantments;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUser;
import fr.farmeurimmo.coreskyblock.storage.skyblockusers.SkyblockUsersManager;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.farmeurimmo.coreskyblock.utils.DateUtils;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.*;

public class EnchantsBuyerInv extends FastInv {

    private static final int SLOT_1 = 12;
    private static final int SLOT_2 = 14;
    private static final int COMMON_WEIGHT = 4;
    private static final int RARE_WEIGHT = 2;
    private static final int EPIC_WEIGHT = 1;
    private static final int TOTAL_WEIGHT = (8 * COMMON_WEIGHT) + (6 * RARE_WEIGHT) + (7 * EPIC_WEIGHT);
    private final Player p;
    private final LinkedList<Integer> expPrices = new LinkedList<>(Arrays.asList(5000, 6500, 8500, 10000, 12000, 13500, 15500, 17000, 19000, 20500, 22500, 25000));
    private boolean canAutoUpdate = true;
    private boolean isClosed = false;

    public EnchantsBuyerInv(Player p) {
        super(6 * 9, "§0Acheter des enchantements");

        this.p = p;
        p.setCanPickupItems(false);

        setItem(49, CommonItemStacks.getCommonBack(), e -> new EnchantsMainInv().open(p));

        CommonItemStacks.applyCommonPanes(Material.PURPLE_STAINED_GLASS_PANE, getInventory());

        setCloseFilter(e -> {
            if (getInventory().getItem(SLOT_1) != null && getInventory().getItem(SLOT_2) != null) {
                float rng = CustomEnchantmentsManager.INSTANCE.getRng();
                if (rng <= 0.5) {
                    p.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(SLOT_1)));
                } else {
                    p.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(SLOT_2)));
                }
                p.sendMessage(Component.text("§aVous avez quittez l'inventaire, vous avez reçu un livre enchanté parmi les deux."));
            }
            e.setCanPickupItems(true);
            isClosed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, (task -> {
            if (isClosed) task.cancel();
            else if (canAutoUpdate) update();
        }), 0, 20);

        update();
    }

    private void update() {
        SkyblockUser skyblockUser = SkyblockUsersManager.INSTANCE.getCachedUsers().get(p.getUniqueId());
        if (skyblockUser == null) {
            p.sendMessage(Component.text("§cUne erreur est survenue."));

            Bukkit.getScheduler().runTask(CoreSkyblock.INSTANCE, () -> p.closeInventory());
            return;
        }

        if (p.getInventory().firstEmpty() == -1) {
            p.sendMessage(Component.text("§cVotre inventaire est plein."));
            p.playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }

        String lastSpecialBooks = skyblockUser.getLastSpecialBooks();
        String[] specialBooks = lastSpecialBooks.split(",");

        ArrayList<Long> specialBooksList = new ArrayList<>();
        for (String specialBook : specialBooks) {
            if (specialBook.isEmpty()) {
                continue;
            }
            try {
                specialBooksList.add(Long.parseLong(specialBook));
            } catch (NumberFormatException ignored) {
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        LinkedList<Integer> posToRemove = new LinkedList<>();
        for (int i = 0; i < specialBooksList.size(); i++) {
            long specialBook = specialBooksList.get(i);

            Calendar dateOfBook = Calendar.getInstance();
            dateOfBook.setTimeInMillis(specialBook);

            // if the book was made before 00:00 of the previous day, remove it
            if (dateOfBook.get(Calendar.DAY_OF_YEAR) <= calendar.get(Calendar.DAY_OF_YEAR) - 1) {
                posToRemove.addFirst(i);
            }
        }

        for (int i : posToRemove) {
            specialBooksList.remove(i);
        }

        setItem(31, new ItemBuilder(Material.PAPER).name("§6Tableau des prix").lore(
                (specialBooksList.isEmpty() ? "§a§l->" : "§7") + " 1 §7échange: §e" + NumberFormat.getInstance().format(expPrices.get(0)) + "xp",
                (specialBooksList.size() == 1 ? "§a§l->" : "§7") + " 2 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(1)) + "xp",
                (specialBooksList.size() == 2 ? "§a§l->" : "§7") + " 3 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(2)) + "xp",
                (specialBooksList.size() == 3 ? "§a§l->" : "§7") + " 4 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(3)) + "xp",
                (specialBooksList.size() == 4 ? "§a§l->" : "§7") + " 5 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(4)) + "xp",
                (specialBooksList.size() == 5 ? "§a§l->" : "§7") + " 6 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(5)) + "xp",
                (specialBooksList.size() == 6 ? "§a§l->" : "§7") + " 7 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(6)) + "xp",
                (specialBooksList.size() == 7 ? "§a§l->" : "§7") + " 8 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(7)) + "xp",
                (specialBooksList.size() == 8 ? "§a§l->" : "§7") + " 9 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(8)) + "xp",
                (specialBooksList.size() == 9 ? "§a§l->" : "§7") + " 10 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(9)) + "xp",
                (specialBooksList.size() == 10 ? "§a§l->" : "§7") + " 11 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(10)) + "xp",
                (specialBooksList.size() == 11 ? "§a§l->" : "§7") + " 12 §7échanges: §e" + NumberFormat.getInstance().format(expPrices.get(11)) + "xp",
                "",
                (specialBooksList.size() == 12 ? "§c§lLimite atteinte" : "§c§lLimité à 12 échanges par jour")).build());

        if (specialBooksList.size() >= 12) {
            setItem(SLOT_1, null);
            setItem(SLOT_2, null);

            setItem(22, new ItemBuilder(Material.BARRIER).name("§cLimite de 12 échanges par jour atteinte")
                            .lore("§7De nouveau disponible dans: §c" + DateUtils.expireAt00()).build(),
                    e -> p.playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1)));
            return;
        }

        int expPrice = expPrices.get(specialBooksList.size());
        setItem(22, new ItemBuilder(Material.BOOK).name("§6Lancer un choix de livré aléatoire").lore("§7Coût: §e" +
                NumberFormat.getInstance().format(expPrice) + "xp").build(), e -> {

            if (getInventory().getItem(SLOT_1) != null && getInventory().getItem(SLOT_2) != null) {
                p.sendMessage(Component.text("§cVous devez choisir un livre avant de lancer un autre choix."));
                p.playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            if (p.calculateTotalExperiencePoints() <= expPrice) {
                p.sendMessage(Component.text("§cVous n'avez pas assez d'expérience."));
                p.playSound(Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            canAutoUpdate = false;

            p.setExperienceLevelAndProgress(p.calculateTotalExperiencePoints() - expPrice);
            p.sendMessage(Component.text("§aVous avez dépensé " + NumberFormat.getInstance().format(expPrice) + "xp."));
            p.playSound(Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, Sound.Source.PLAYER, 1, 1));

            setSlots(SLOT_1);
            setSlots(SLOT_2);

            StringBuilder updatedSpecialBooks = new StringBuilder();
            for (long specialBook : specialBooksList) {
                updatedSpecialBooks.append(specialBook).append(",");
            }
            updatedSpecialBooks.append(System.currentTimeMillis());
            skyblockUser.setLastSpecialBooks(updatedSpecialBooks.toString());
        });
    }

    private void setSlots(int slot) {
        setItem(slot, getARandomEnchant(), e1 -> {
            p.playSound(Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1, 1));

            p.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(slot)));

            getInventory().setItem(SLOT_2, null);
            getInventory().setItem(SLOT_1, null);

            canAutoUpdate = true;

            update();
        });
    }

    public ItemStack getARandomEnchant() {
        float rng = CustomEnchantmentsManager.INSTANCE.getRng() * TOTAL_WEIGHT;
        float rng2 = CustomEnchantmentsManager.INSTANCE.getRng();

        ArrayList<Enchantments> selectedEnchantments;
        if (rng < 8 * COMMON_WEIGHT) { // COMMON
            selectedEnchantments = Enchantments.getEnchantmentsByRarity(EnchantmentRarity.UNCOMMON);
        } else if (rng < (8 * COMMON_WEIGHT) + (6 * RARE_WEIGHT)) { // RARE
            selectedEnchantments = Enchantments.getEnchantmentsByRarity(EnchantmentRarity.RARE);
        } else { // EPIC
            selectedEnchantments = Enchantments.getEnchantmentsByRarity(EnchantmentRarity.EPIC);
        }

        return getItemStack(rng2, selectedEnchantments);
    }

    private ItemStack getItemStack(float rng2, ArrayList<Enchantments> enchantments) {
        Enchantments enchantment = enchantments.get((int) (rng2 * enchantments.size())); // get a random enchantment
        int level = 1;

        if (enchantment.hasMaxLevel()) {
            level = 1 + new Random().nextInt(enchantment.getMaxLevel()); // choose a random level
        }

        return CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantment, level);
    }
}
