package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.farmeurimmo.coreskyblock.utils.CommonItemStacks;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class IslandInv extends FastInv {

    public IslandInv(Island island) {
        super(6 * 9, "§8Menu de l'île");

        CommonItemStacks.applyCommonPanes(Material.LIME_STAINED_GLASS_PANE, getInventory());

        ItemStack member = CommonItemStacks.getCached(island.getOwnerUUID(), island.getMemberName(island.getOwnerUUID()));
        if (member == null) {
            CommonItemStacks.getHead(island.getOwnerUUID(), island.getMemberName(island.getOwnerUUID()))
                    .thenAccept(item -> setItem(12, ItemBuilder.copyOf(item).name("§6Membres §8| §7(clic gauche)").build(),
                            e -> new IslandMembersInv(island, (Player) e.getWhoClicked()).open((Player) e.getWhoClicked())));
        } else {
            setItem(12, ItemBuilder.copyOf(member).name("§6Membres §8| §7(clic gauche)").build(), e -> new IslandMembersInv(island, (Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));
        }

        setItem(13, ItemBuilder.copyOf(new ItemStack(Material.BRUSH))
                .name("§6Améliorations §8| §7(clic gauche)").build(), e ->
                new IslandUpgradesInv(island, (Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(14, ItemBuilder.copyOf(new ItemStack(Material.MACE))
                .name("§6Permissions §8| §7(clic gauche)").build(), e ->
                new IslandRankEditInv(island, (Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));


        setItem(20, ItemBuilder.copyOf(new ItemStack(Material.ITEM_FRAME))
                .name("§6Challenges §8| §7(clic gauche)").build(), e ->
                e.getWhoClicked().sendMessage(Component.text("§cEn développement...")));

        setItem(21, ItemBuilder.copyOf(new ItemStack(Material.GOLD_INGOT))
                .name("§6Banque §8| §7(clic gauche)").build(), e ->
                new IslandBankInv(island).open((Player) e.getWhoClicked()));

        setItem(22, ItemBuilder.copyOf(new ItemStack(Material.SPYGLASS))
                .name("§6Valeur des blocs §8| §7(clic gauche)").build(), e ->
                new IslandBlocksValues(island).open((Player) e.getWhoClicked()));

        setItem(23, ItemBuilder.copyOf(new ItemStack(Material.AMETHYST_CLUSTER)).name("§6Navigateur de warps §8| §7(clic gauche)").build(), e -> {
            Player p = (Player) e.getWhoClicked();
            p.chat("/is warpbrowser");
        });

        setItem(24, ItemBuilder.copyOf(new ItemStack(Material.TOTEM_OF_UNDYING))
                .name("§6Coops §8| §7(clic gauche)").build(), e ->
                new IslandCoopsInv(island).open((Player) e.getWhoClicked()));


        setItem(30, ItemBuilder.copyOf(new ItemStack(Material.BEACON))
                .name("§6Classement §8| §7(clic gauche)").build(), e ->
                new IslandsTopInv().open((Player) e.getWhoClicked()));

        setItem(31, ItemBuilder.copyOf(new ItemStack(Material.COMPARATOR))
                .name("§6Paramètres d'îles §8| §7(clic gauche)").build(), e ->
                new IslandSettingsInv(island).open((Player) e.getWhoClicked()));

        setItem(32, ItemBuilder.copyOf(new ItemStack(Material.OAK_SAPLING))
                .name("§6Biome §8| §7(clic gauche)").build(), e ->
                e.getWhoClicked().sendMessage(Component.text("§cEn développement...")));


        setItem(40, ItemBuilder.copyOf(new ItemStack(Material.ENDER_EYE))
                .name("§6Téléportation §8| §7(clic gauche)").build(), e ->
                CompletableFuture.runAsync(() -> IslandsManager.INSTANCE.teleportToIsland(island, (Player) e.getWhoClicked())));


        /*setItem(21, ItemBuilder.copyOf(new ItemStack(Material.AMETHYST_BLOCK)).name("§6Warp §8| §7(clic gauche)").build(), e -> {
            Player p = (Player) e.getWhoClicked();
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly(p);
                return;
            }
            p.chat("/is warp");
        });
        setItem(23, ItemBuilder.copyOf(new ItemStack(Material.IRON_DOOR)).
                name("§6Accessibilité §8| §7(clic gauche)").lore("§7Actuellement: " + (island.isPublic() ? "§aPublique"
                        : "§cPrivée")).build(), e -> {
            Player p = (Player) e.getWhoClicked();
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly(p);
                return;
            }
            p.chat("/is " + (island.isPublic() ? "private" : "public"));
            new IslandInv(island).open(p);
        });*/
    }
}
