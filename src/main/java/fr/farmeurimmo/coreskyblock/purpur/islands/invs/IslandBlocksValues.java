package fr.farmeurimmo.coreskyblock.purpur.islands.invs;

import fr.farmeurimmo.coreskyblock.common.islands.Island;
import fr.farmeurimmo.coreskyblock.purpur.islands.levels.IslandsBlocksValues;
import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.Map;

public class IslandBlocksValues extends FastInv {

    public IslandBlocksValues(Island island) {
        super(27, "§8Valeur des blocs");

        setItem(18, ItemBuilder.copyOf(new ItemStack(Material.KNOWLEDGE_BOOK))
                .name("§6Informations complémentaires").lore("§7Il faut recalculer la valeur de l'île",
                        "§7pour que les blocs posés soient pris en compte.").build());

        setItem(26, ItemBuilder.copyOf(new ItemStack(Material.ARROW))
                .name("§6Retour §8| §7(clic gauche)").build(), e ->
                new IslandInv(island).open((Player) e.getWhoClicked()));

        update();
    }

    private void update() {
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int i = 0;
        for (Map.Entry<Material, Float> entry : IslandsBlocksValues.INSTANCE.getBlocksValues().entrySet()) {
            setItem(slots[i], ItemBuilder.copyOf(new ItemStack(entry.getKey())).name("§6" + entry.getKey().name())
                    .lore("§7Valeur: §6" + NumberFormat.getInstance().format(entry.getValue())).build());

            i++;
            if (i >= slots.length) {
                break;
            }
        }
    }
}
