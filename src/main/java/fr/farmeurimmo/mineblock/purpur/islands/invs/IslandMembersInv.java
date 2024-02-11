package fr.farmeurimmo.mineblock.purpur.islands.invs;

import fr.farmeurimmo.mineblock.common.islands.Island;
import fr.farmeurimmo.mineblock.common.islands.IslandRanks;
import fr.farmeurimmo.mineblock.common.islands.IslandRanksManager;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandMembersInv extends FastInv {

    private final int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32,
            33, 34, 37, 38, 39, 40, 41, 42, 43};

    public IslandMembersInv(Island island, Player whoClicked) {
        super(54, "§8Membres de l'île");

        if (island == null) {
            whoClicked.sendMessage("§cUne erreur est survenue lors de la récupération de votre île.");
            return;
        }

        setItem(53, ItemBuilder.copyOf(new ItemStack(Material.ARROW))
                .name("§6Retour §8| §7(clic gauche)").build(), e -> new IslandInv(island).open((Player) e.getWhoClicked()));

        update(island);
    }

    public void update(Island island) {
        if (island == null) return;

        int currentSlot = 0;
        ArrayList<UUID> members = new ArrayList<>();
        int currentLevelRank = 0;
        Map<UUID, IslandRanks> levelRank = island.getMembers();
        HashMap<IslandRanks, Integer> rankPos = IslandRanksManager.INSTANCE.getIslandRankPos();
        while (members.size() != levelRank.size()) {
            if (currentLevelRank > 3) {
                break;
            }
            for (Map.Entry<UUID, IslandRanks> playerEntry : levelRank.entrySet()) {
                if (members.contains(playerEntry.getKey())) {
                    continue;
                }
                if (rankPos.get(playerEntry.getValue()) != currentLevelRank) {
                    continue;
                }
                ItemStack custom = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) custom.getItemMeta();
                if (playerEntry.getKey() != null) {
                    meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerEntry.getKey()));
                    meta.displayName(Component.text(Bukkit.getOfflinePlayer(playerEntry.getKey()).getName()));
                } else {
                    meta.setOwningPlayer(null);
                    meta.displayName(Component.text("§cErreur lors de la récupération du joueur"));
                }
                custom.setItemMeta(meta);
                ArrayList<String> lore = new ArrayList<>();
                lore.add("§7Clic droit pour promouvoir");
                lore.add("§7Clic gauche pour rétrograder");
                lore.add("");
                lore.add("§7Grade : " + playerEntry.getValue().name());
                setItem(slots[currentSlot], ItemBuilder.copyOf(custom).lore(lore).build(), e -> {
                    if (e.isLeftClick()) {
                        island.removeMember(playerEntry.getKey());
                        island.addMember(playerEntry.getKey(), IslandRanksManager.INSTANCE.getNextRank(playerEntry.getValue()));
                    } else {
                        island.removeMember(playerEntry.getKey());
                        island.addMember(playerEntry.getKey(), IslandRanksManager.INSTANCE.getPreviousRank(playerEntry.getValue()));
                    }
                    update(island);
                });
                members.add(playerEntry.getKey());
                currentSlot++;
            }
            currentLevelRank += 1;
        }
    }
}
