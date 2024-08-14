package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsWarpManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.storage.islands.IslandWarp;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandPerms;
import fr.farmeurimmo.coreskyblock.storage.islands.enums.IslandWarpCategories;
import fr.farmeurimmo.coreskyblock.utils.DateUtils;
import fr.farmeurimmo.coreskyblock.utils.LocationTranslator;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.ArrayList;

public class IslandWarpInv extends FastInv {

    private static final long COOLDOWN = 4_000;
    private boolean gotUpdate = false;
    private long lastAction = System.currentTimeMillis() - COOLDOWN;
    private boolean closed = false;
    private boolean awaitingMaterial = false;
    private IslandWarp warp;

    public IslandWarpInv(Island island, IslandWarp warp) {
        super(36, "§0Warp de l'île");

        setItem(35, ItemBuilder.copyOf(new ItemStack(Material.ARROW))
                .name("§6Retour §8| §7(clic gauche)").build(), e -> {
            new IslandInv(island).open((Player) e.getWhoClicked());
            gotUpdate = true;
        });

        update(island, warp);

        setCloseFilter(p -> {
            gotUpdate = true;
            closed = true;
            return false;
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(CoreSkyblock.INSTANCE, (task) -> {
            if (closed) {
                task.cancel();
                return;
            }
            if (gotUpdate) return;
            gotUpdate = true;
            update(island, warp);
        }, 0, 40L);
    }

    private void update(Island island, IslandWarp warp) {
        gotUpdate = false;

        if (warp != null) {
            setItem(10, ItemBuilder.copyOf(new ItemStack(Material.NAME_TAG))
                    .name("§6Nom §8| §7(clic gauche)")
                    .lore("§7" + warp.getName()).build(), e -> {
                if (checkForPermsAndIfLoaded(island, e)) return;

                if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                    e.getWhoClicked().sendMessage(Component.text("§cMerci d'attendre un peu avant de modifier le nom."));
                    return;
                }
                lastAction = System.currentTimeMillis();
                IslandsWarpManager.INSTANCE.addProcessInput((Player) e.getWhoClicked(), "name");
                e.getWhoClicked().sendMessage(Component.text("\n§6Merci de définir un nom pour le warp.\n§7Tapez 'cancel' pour annuler."));
                e.getWhoClicked().sendMessage(Component.text("§7Exemple: §fMon warp"));
                e.getWhoClicked().closeInventory();
            });

            setItem(11, ItemBuilder.copyOf(new ItemStack(Material.BOOK))
                    .name("§6Description §8| §7(clic gauche)")
                    .lore(IslandsWarpManager.INSTANCE.getLore(warp)).build(), e -> {
                if (checkForPermsAndIfLoaded(island, e)) return;

                if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                    e.getWhoClicked().sendMessage(Component.text("§cMerci d'attendre un peu avant de modifier la description."));
                    return;
                }
                lastAction = System.currentTimeMillis();
                IslandsWarpManager.INSTANCE.addProcessInput((Player) e.getWhoClicked(), "description");
                e.getWhoClicked().sendMessage(Component.text("\n§6Merci de définir une description pour le warp (pour changer de ligne, utilisez un \\n.\n§7Tapez 'cancel' pour annuler."));
                e.getWhoClicked().sendMessage(Component.text("§7Exemple: §fCeci est une description\n§fCeci est une autre ligne."));
                e.getWhoClicked().closeInventory();
            });

            ArrayList<String> categories = new ArrayList<>();
            for (IslandWarpCategories category : warp.getCategories()) {
                categories.add("§7" + category.getName());
            }
            if (categories.isEmpty()) {
                categories.add("§7Aucune catégorie");
            }
            setItem(12, ItemBuilder.copyOf(new ItemStack(Material.PAPER))
                    .name("§6Catégories §8| §7(clic gauche)")
                    .lore(categories).build(), e -> {
                if (checkForPermsAndIfLoaded(island, e)) return;

                if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                    e.getWhoClicked().sendMessage(Component.text("§cEn développement..."));
                    return;
                }
                lastAction = System.currentTimeMillis();

                new IslandWarpCategoriesSelectionInv(island, warp).open((Player) e.getWhoClicked());
            });

            setItem(13, ItemBuilder.copyOf(new ItemStack(Material.COMPASS))
                    .name("§6Localisation §8| §7(clic gauche pour définir sur vous)")
                    .lore("§7" + LocationTranslator.readableLocation(warp.getLocation())).build(), e -> {
                if (e.getWhoClicked().getWorld() != IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID())) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous devez être sur l'île pour définir la localisation."));
                    return;
                }
                if (checkForPermsAndIfLoaded(island, e)) return;

                if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                    e.getWhoClicked().sendMessage(Component.text("§cVeuillez attendre un peu avant de définir la localisation."));
                    return;
                }
                lastAction = System.currentTimeMillis();
                warp.setLocation(e.getWhoClicked().getLocation());
                e.getWhoClicked().sendMessage(Component.text("§aLocation définie sur vous."));
                gotUpdate = true;
                update(island, warp);
            });

            setItem(14, ItemBuilder.copyOf(new ItemStack(Material.ITEM_FRAME))
                    .name("§6Changer l'item §8| §7(clic gauche)").build(), e -> {
                if (checkForPermsAndIfLoaded(island, e)) return;

                if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                    e.getWhoClicked().sendMessage(Component.text("§cMerci d'attendre un peu avant de changer l'item."));
                    return;
                }
                lastAction = System.currentTimeMillis();

                if (awaitingMaterial) {
                    awaitingMaterial = false;
                    this.warp = null;
                    e.getWhoClicked().sendMessage(Component.text("§cOpération annulée."));
                    return;
                }
                awaitingMaterial = true;
                this.warp = warp;
                e.getWhoClicked().sendMessage(Component.text("§6Cliquez sur un item de votre inventaire pour " +
                        "le définir comme item du warp. Re cliquez sur l'item pour annuler."));
            });

            setItem(16, ItemBuilder.copyOf(new ItemStack(Material.REDSTONE_TORCH))
                    .name("§6Activation §8| §7(clic gauche)")
                    .lore("§7" + (warp.isActivated() ? "Activé" : "Désactivé")).build(), e -> {
                if (checkForPermsAndIfLoaded(island, e)) return;

                if (warp.getLocation() == null) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous devez définir une localisation avant d'activer le warp."));
                    return;
                }
                if (warp.getCategories().isEmpty()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous devez définir au moins une catégorie avant d'activer le warp."));
                    return;
                }
                if (warp.getName() == null || warp.getName().isEmpty()) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous devez définir un nom avant d'activer le warp."));
                    return;
                }

                if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                    e.getWhoClicked().sendMessage(Component.text("§cMerci d'attendre un peu avant d'activer/désactiver le warp."));
                    return;
                }
                lastAction = System.currentTimeMillis();

                warp.setActivated(!warp.isActivated());
                e.getWhoClicked().sendMessage(Component.text("§aWarp " + (warp.isActivated() ? "activé" : "désactivé") + "."));
                gotUpdate = true;
                update(island, warp);
            });

            String expiry = (warp.isStillForwarded() ? (("§7Expire dans " + DateUtils.getFormattedTimeLeft(
                    (int) ((warp.getForwardedWarp() - System.currentTimeMillis()) / 1000)) + " puis 24H de cooldown"))
                    : (warp.isInCooldownForForward() ? "§cFin du cooldown dans " + DateUtils.getFormattedTimeLeft(
                    (int) ((warp.getForwardedWarp() - System.currentTimeMillis()) / 1000)) + " avant de pouvoir remettre en avant"
                    : "§7Pas de mise en avant"));
            ItemStack forwardItem = ItemBuilder.copyOf(new ItemStack(Material.GOLD_INGOT)).name(
                    "§6Mise en avant §8| §7(clic gauche)").lore("§7" + (warp.getForwardedWarp() > System.currentTimeMillis()
                    ? "§aOui" : "§cNon"), expiry, "", "§7Coût: §e25 000$").flags(ItemFlag.HIDE_ENCHANTS).build();
            if (warp.isStillForwarded()) forwardItem.addEnchantment(Enchantment.CHANNELING, 1);
            setItem(15, forwardItem, e -> {
                if (checkForPermsAndIfLoaded(island, e)) return;
                if (warp.isStillForwarded()) {
                    e.getWhoClicked().sendMessage(Component.text("§aWarp déja mis en avant."));
                    return;
                }
                if (warp.isInCooldownForForward()) {
                    e.getWhoClicked().sendMessage(Component.text("§cLe warp est en cooldown pour la mise en avant."));
                    return;
                }

                if (island.getBankMoney() < 25_000) {
                    e.getWhoClicked().sendMessage(Component.text("§cLa banque de l'île n'a pas assez d'argent " +
                            "pour mettre en avant le warp. (Il manque " +
                            NumberFormat.getInstance().format(25_000 - island.getBankMoney()) + ")"));
                    return;
                }

                if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                    e.getWhoClicked().sendMessage(Component.text("§cMerci d'attendre un peu avant de mettre en avant le warp."));
                    return;
                }
                lastAction = System.currentTimeMillis();

                if (IslandsWarpManager.INSTANCE.getForwardedWarps().size() >= 4) {
                    e.getWhoClicked().sendMessage(Component.text("§cIl n'y a plus de place pour mettre en avant un warp."));
                    return;
                }

                island.setBankMoney(island.getBankMoney() - 25_000);
                warp.setForwardedWarp(System.currentTimeMillis() + 86_400_000);
                e.getWhoClicked().sendMessage(Component.text("§aWarp mis en avant."));
                island.sendMessageToAll("§eLe warp de l'île a été mis en avant.");
            });

            setItem(19, ItemBuilder.copyOf(new ItemStack(Material.ENDER_PEARL)).name("§6Évaluation du warp")
                    .lore("§7" + NumberFormat.getInstance().format(warp.getRate())).build());

            setItem(20, ItemBuilder.copyOf(new ItemStack(Material.BOOKSHELF)).name("§6Dernières évaluations")
                    .lore(IslandsWarpManager.INSTANCE.getLastRates(warp)).build());
        } else {
            setItem(13, ItemBuilder.copyOf(new ItemStack(Material.COMPASS))
                    .name("§6Créer un warp §8| §7(clic gauche pour définir sur vous)")
                    .lore("§7L'édition du nom, la description, les catégories,", "§7la mise en avant et l'activation du warp",
                            "§7seront disponible après cette étape").build(), e -> {
                if (!island.isLoaded()) {
                    e.getWhoClicked().sendMessage(Component.text("§cL'île n'est pas chargée ici."));
                    return;
                }

                if (e.getWhoClicked().getWorld() != IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID())) {
                    e.getWhoClicked().sendMessage(Component.text("§cVous devez être sur l'île pour définir la localisation."));
                    return;
                }

                if (System.currentTimeMillis() - lastAction < COOLDOWN) {
                    e.getWhoClicked().sendMessage(Component.text("§cVeuillez attendre un peu avant de créer un nouveau warp."));
                    return;
                }
                lastAction = System.currentTimeMillis();

                IslandWarp newWarp = new IslandWarp(island.getIslandUUID(), e.getWhoClicked().getName(),
                        e.getWhoClicked().getLocation(), true);
                IslandsWarpManager.INSTANCE.updateWarpWithId(newWarp.getUuid(), newWarp);
                e.getWhoClicked().sendMessage(Component.text("§aWarp créé."));
                gotUpdate = true;
                new IslandWarpInv(island, newWarp).open((Player) e.getWhoClicked());
            });
        }

        setItem(27, ItemBuilder.copyOf(new ItemStack(Material.MAP))
                        .name("§6Voir la liste des warps disponibles §8| §7(clic gauche)").build(),
                e -> new IslandsWarpBrowserInv().open((Player) e.getWhoClicked()));
    }

    private boolean checkForPermsAndIfLoaded(Island island, InventoryClickEvent e) {
        if (!island.isLoaded()) {
            e.getWhoClicked().sendMessage(Component.text("§cL'île n'est pas chargée ici."));
            return true;
        }
        if (!island.hasPerms(island, IslandPerms.EDIT_ISLAND_WARP, e.getWhoClicked().getUniqueId())) {
            e.getWhoClicked().sendMessage(Component.text("§cVous n'avez pas la permission de modifier le warp."));
            return true;
        }
        return false;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (!awaitingMaterial) return;
        if (e.getRawSlot() < 27) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (warp == null) return;
        warp.setMaterial(item.getType());
        e.getWhoClicked().sendMessage(Component.text("§aItem défini."));
    }
}
