package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class IslandInv extends FastInv {

    public IslandInv(Island island) {
        super(45, "§8Menu de l'île");

        setItem(10, ItemBuilder.copyOf(new ItemStack(Material.ENDER_EYE))
                .name("§6Téléportation §8| §7(clic gauche)").build(), e ->
                IslandsManager.INSTANCE.teleportToIsland(island, (Player) e.getWhoClicked()));

        ItemStack member = ItemBuilder.copyOf(new ItemStack(Material.PLAYER_HEAD))
                .name("§6Membres §8| §7(clic gauche)").build();
        SkullMeta meta = (SkullMeta) member.getItemMeta();
        meta.setOwner("Farmeurimmo");
        member.setItemMeta(meta);

        setItem(11, member, e -> new IslandMembersInv(island, (Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(12, ItemBuilder.copyOf(new ItemStack(Material.BLAST_FURNACE))
                .name("§6Améliorations §8| §7(clic gauche)").build(), e ->
                new IslandUpgradesInv(island, (Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(13, ItemBuilder.copyOf(new ItemStack(Material.BOOKSHELF))
                .name("§6Permissions §8| §7(clic gauche)").build(), e ->
                new IslandRankEditInv(island, (Player) e.getWhoClicked()).open((Player) e.getWhoClicked()));

        setItem(14, ItemBuilder.copyOf(new ItemStack(Material.EMERALD_BLOCK))
                .name("§6Valeur des blocs §8| §7(clic gauche)").build(), e ->
                new IslandBlocksValues(island).open((Player) e.getWhoClicked()));


        setItem(16, ItemBuilder.copyOf(new ItemStack(Material.BEACON))
                .name("§6Classement §8| §7(clic gauche)").build(), e ->
                e.getWhoClicked().sendMessage(Component.text("§cEn développement...")));


        setItem(19, ItemBuilder.copyOf(new ItemStack(Material.WOODEN_HOE))
                .name("§6Coops §8| §7(clic gauche)").build(), e ->
                e.getWhoClicked().sendMessage(Component.text("§cEn développement...")));

        setItem(20, ItemBuilder.copyOf(new ItemStack(Material.PAPER))
                .name("§6Challenges §8| §7(clic gauche)").build(), e ->
                e.getWhoClicked().sendMessage(Component.text("§cEn développement...")));

        setItem(21, ItemBuilder.copyOf(new ItemStack(Material.GOLD_BLOCK))
                .name("§6Banque §8| §7(clic gauche)").build(), e ->
                new IslandBankInv(island).open((Player) e.getWhoClicked()));

        setItem(22, ItemBuilder.copyOf(new ItemStack(Material.BEDROCK))
                .name("§c???").build(), e ->
                e.getWhoClicked().sendMessage(Component.text("§cEn développement...")));

        setItem(23, ItemBuilder.copyOf(new ItemStack(Material.COMPARATOR))
                .name("§6Paramètres d'îles §8| §7(clic gauche)").build(), e ->
                new IslandSettingsInv(island).open((Player) e.getWhoClicked()));


        setItem(28, ItemBuilder.copyOf(new ItemStack(Material.SPRUCE_SAPLING))
                .name("§6Biome §8| §7(clic gauche)").build(), e ->
                e.getWhoClicked().sendMessage(Component.text("§cEn développement...")));

        setItem(29, ItemBuilder.copyOf(new ItemStack(Material.IRON_DOOR)).
                name("§6Accessibilité §8| §7(clic gauche)").lore("§7Actuellement: " + (island.isPublic() ? "§aPublique"
                        : "§cPrivée")).build(), e -> {
            Player p = (Player) e.getWhoClicked();
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly(p);
                return;
            }
            p.chat("/is " + (island.isPublic() ? "private" : "public"));
            new IslandInv(island).open(p);
        });

        setItem(34, ItemBuilder.copyOf(new ItemStack(Material.AMETHYST_BLOCK)).name("§6Warp §8| §7(clic gauche)").build(), e -> {
            Player p = (Player) e.getWhoClicked();
            if (island.isReadOnly()) {
                IslandsManager.INSTANCE.sendPlayerIslandReadOnly(p);
                return;
            }
            p.chat("/is warp");
        });
    }
}
