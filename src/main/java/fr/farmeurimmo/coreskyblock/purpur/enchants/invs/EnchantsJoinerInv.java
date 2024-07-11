package fr.farmeurimmo.coreskyblock.purpur.enchants.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.enchants.CustomEnchantmentsManager;
import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.Enchantments;
import fr.farmeurimmo.coreskyblock.purpur.enchants.enums.EnchantmentsRecipients;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class EnchantsJoinerInv extends FastInv {

    private static final int SLOT_1 = 12;
    private static final int SLOT_2 = 14;
    private static final int FINAL_SLOT = 31;
    private final Player p;
    private boolean hasConfirmed = false;
    private boolean isClosed = false;

    public EnchantsJoinerInv(Player p) {
        super(45, "§0Table de fusion d'enchantements");

        this.p = p;
        p.setCanPickupItems(false);

        setItem(0, new ItemBuilder(Material.IRON_DOOR).name("§cFermer").build(), e -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
        });

        for (int i = 0; i < getInventory().getSize(); i++) {
            if (i == SLOT_1 || i == SLOT_2 || i == FINAL_SLOT) {
                continue;
            }
            if (getInventory().getItem(i) == null)
                setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§0").build(), e -> e.setCancelled(true));
        }

        setCloseFilter(e -> {
            if (hasConfirmed) {
                e.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(FINAL_SLOT)));
                e.getInventory().setItem(FINAL_SLOT, null);
                e.getInventory().setItem(SLOT_1, null);
                e.getInventory().setItem(SLOT_2, null);
            } else {
                if (getInventory().getItem(SLOT_1) != null) {
                    e.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(SLOT_1)));
                }
                if (getInventory().getItem(SLOT_2) != null) {
                    e.getInventory().addItem(Objects.requireNonNull(getInventory().getItem(SLOT_2)));
                }
            }
            e.setCanPickupItems(true);
            isClosed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimer(CoreSkyblock.INSTANCE, () -> {
            if (!isClosed) update(false);
        }, 0, 15L);
    }

    private void update(boolean notify) {
        ItemStack item1 = getInventory().getItem(SLOT_1);
        ItemStack item2 = getInventory().getItem(SLOT_2);

        if (item1 == null) {
            hasConfirmed = false;
            setItemResultAsError();
            sendFeedback(notify, p, Component.text("§cVeuillez mettre un item dans la première case"),
                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }
        if (item2 == null) {
            hasConfirmed = false;
            setItemResultAsError();
            sendFeedback(notify, p, Component.text("§cVeuillez mettre un item dans la deuxième case"),
                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }

        if (!item1.equals(item2)) {
            final ItemStack enchantmentBook = item1.getType() == Material.ENCHANTED_BOOK ? item1.clone() : item2.clone();
            final ItemStack item = item1.getType() == Material.ENCHANTED_BOOK ? item2.clone() : item1.clone();

            if (enchantmentBook.getType() != Material.ENCHANTED_BOOK) {
                setItemResultAsError();
                sendFeedback(notify, p, Component.text("§cIl doit y avoir un livre enchanté dans une des deux cases."),
                        Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }
            if (item.getType() == Material.ENCHANTED_BOOK) {
                setItemResultAsError();
                sendFeedback(notify, p, Component.text("§cVous ne pouvez pas fusionner deux livres enchantés."),
                        Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            Optional<ArrayList<Pair<Enchantments, Integer>>> bookEnchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(enchantmentBook);
            Optional<ArrayList<Pair<Enchantments, Integer>>> itemEnchantments = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(item);

            if (bookEnchantments.isEmpty()) {
                setItemResultAsError();
                sendFeedback(notify, p, Component.text("§cLe livre enchanté ne contient pas d'enchantement spécial."),
                        Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            if (itemEnchantments.isPresent()) {
                for (int i = 0; i < itemEnchantments.get().size(); i++) {
                    Pair<Enchantments, Integer> enchantment = itemEnchantments.get().get(i);
                    for (Pair<Enchantments, Integer> bookEnchantment : bookEnchantments.get()) {
                        if (enchantment.left().equals(bookEnchantment.left())) {
                            if (enchantment.right() <= bookEnchantment.right()) {
                                if (!enchantment.left().hasMaxLevel()) {
                                    setItemResultAsError();
                                    sendFeedback(notify, p, Component.text("§cVous ne pouvez pas fusionner un item avec un livre enchanté sans niveau."),
                                            Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                                    return;
                                }
                                if (enchantment.right() + 1 > enchantment.left().getMaxLevel()) {
                                    setItemResultAsError();
                                    sendFeedback(notify, p, Component.text("§cVous avez atteint le niveau maximum pour cet enchantement."),
                                            Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                                    return;
                                }
                                int diffLevel = bookEnchantment.right() - enchantment.right();
                                int highestLevel = Math.max(enchantment.right(), bookEnchantment.right());
                                if (diffLevel > 1 || diffLevel < 0) {
                                    enchantment = Pair.of(enchantment.left(), highestLevel);
                                } else {
                                    enchantment = Pair.of(enchantment.left(), enchantment.right() + 1);
                                }
                                itemEnchantments.get().set(i, enchantment);

                                setFinalItem(CustomEnchantmentsManager.INSTANCE.getItemStackWithEnchantsApplied(itemEnchantments.get(), item));

                                sendFeedback(notify, p, Component.text("§aVous avez fusionné un item avec un livre enchanté."),
                                        Sound.sound(org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, Sound.Source.PLAYER, 1, 1));
                                return;
                            }
                            sendFeedback(notify, p, Component.text("§cLes deux enchantements doivent être du même niveau."),
                                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                            return;
                        }
                    }
                }
                itemEnchantments.get().addAll(bookEnchantments.get());

                setFinalItem(CustomEnchantmentsManager.INSTANCE.getItemStackWithEnchantsApplied(itemEnchantments.get(), item));

                sendFeedback(notify, p, Component.text("§aVous avez fusionné un item avec un livre enchanté."),
                        Sound.sound(org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, Sound.Source.PLAYER, 1, 1));
                return;
            }
            itemEnchantments = bookEnchantments;

            if (!itemEnchantments.get().get(0).left().isAllowed(EnchantmentsRecipients.getFromItemStack(item))) {
                setItemResultAsError();
                sendFeedback(notify, p, Component.text("§cCet enchantement ne peut pas être appliqué sur cet item."),
                        Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
                return;
            }

            setFinalItem(CustomEnchantmentsManager.INSTANCE.getItemStackWithEnchantsApplied(bookEnchantments.get(), item));

            sendFeedback(notify, p, Component.text("§aVous avez fusionné un item avec un livre enchanté."),
                    Sound.sound(org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, Sound.Source.PLAYER, 1, 1));
            return;
        }

        // Fusion de livres

        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments1 = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(item1);
        Optional<ArrayList<Pair<Enchantments, Integer>>> enchantments2 = CustomEnchantmentsManager.INSTANCE.getValidEnchantments(item2);

        if (enchantments1.isEmpty()) {
            setItemResultAsError();
            sendFeedback(notify, p, Component.text("§cL'item dans la première case n'est pas un enchantement spécial."),
                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }
        if (enchantments2.isEmpty()) {
            setItemResultAsError();
            sendFeedback(notify, p, Component.text("§cL'item dans la deuxième case n'est pas un enchantement spécial."),
                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }

        if (enchantments1.get().size() != 1) {
            setItemResultAsError();
            sendFeedback(notify, p, Component.text("§cL'item dans la première case ne doit contenir qu'un seul enchantement."),
                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }
        if (enchantments2.get().size() != 1) {
            setItemResultAsError();
            sendFeedback(notify, p, Component.text("§cL'item dans la deuxième case ne doit contenir qu'un seul enchantement."),
                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }

        Pair<Enchantments, Integer> enchantment1 = enchantments1.get().get(0);
        Pair<Enchantments, Integer> enchantment2 = enchantments2.get().get(0);

        if (!enchantment1.left().equals(enchantment2.left())) {
            setItemResultAsError();
            sendFeedback(notify, p, Component.text("§cLes deux livres d'enchantements doivent contenir le même enchantement."),
                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }
        if (!enchantment1.right().equals(enchantment2.right())) {
            setItemResultAsError();
            sendFeedback(notify, p, Component.text("§cLes deux livres d'enchantements doivent contenir le même niveau."),
                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }

        if (!enchantment1.left().hasMaxLevel()) {
            setItemResultAsError();
            sendFeedback(notify, p, Component.text("§cVous ne pouvez pas fusionner deux livres enchantés sans niveau."),
                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }

        if (enchantment1.right() + 1 > enchantment1.left().getMaxLevel()) {
            setItemResultAsError();
            sendFeedback(notify, p, Component.text("§cVous avez atteint le niveau maximum pour cet enchantement."),
                    Sound.sound(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.PLAYER, 1, 1));
            return;
        }

        setFinalItem(CustomEnchantmentsManager.INSTANCE.getItemStackEnchantedBook(enchantment1.left(), enchantment1.right() + 1));

        sendFeedback(notify, p, Component.text("§aVous avez fusionné deux livres enchantés."),
                Sound.sound(org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, Sound.Source.PLAYER, 1, 1));
    }

    private void setItemResultAsError() {
        setItem(FINAL_SLOT, new ItemBuilder(Material.BARRIER).name("§cImpossible").build(), e -> e.setCancelled(true));
    }

    private void setFinalItem(ItemStack newItem) {
        hasConfirmed = true;
        setItem(FINAL_SLOT, newItem, e -> {
            if (hasConfirmed) {
                e.setCancelled(false);
                getInventory().setItem(SLOT_1, null);
                getInventory().setItem(SLOT_2, null);
                hasConfirmed = false;
                return;
            }
            e.setCancelled(true);
        });
    }

    private void sendFeedback(boolean shouldSend, Player player, Component message, Sound sound) {
        /*if (!shouldSend) {
            return;
        }
        player.sendMessage(message);
        if (sound != null) {
            player.playSound(sound);
        }*/
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(false);
        if (!isClosed) {
            update(false);
        }
    }
}
