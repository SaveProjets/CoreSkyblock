package fr.farmeurimmo.coreskyblock.purpur.blocks.elevators;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.mrmicky.fastinv.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class ElevatorsManager {

    public static ElevatorsManager INSTANCE;
    public final ArrayList<UUID> usingElevator = new ArrayList<>();

    public ElevatorsManager() {
        INSTANCE = this;
    }

    public void giveElevator(Player p) {
        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItem(p.getLocation(), getElevator());
            p.sendMessage(Component.text("§aVotre inventaire est plein, un ascenseur a été déposé au sol."));
        } else {
            p.getInventory().addItem(getElevator());
            p.sendMessage(Component.text("§aVous avez reçu un ascenseur."));
        }
    }

    public ItemStack getElevator() {
        return ItemBuilder.copyOf(new ItemStack(Material.OBSIDIAN)).name("§6Ascenseur")
                .lore("§7Placer des ascenseurs pour vous déplacer", "§7rapidement entre les étages de votre île.").build();
    }

    public boolean isAnElevator(ItemStack item) { // FIXME: itemadder
        return item.getType() == getElevator().getType() && Objects.equals(item.getItemMeta(), getElevator().getItemMeta());
    }

    public void addUsingElevator(Player p) {
        usingElevator.add(p.getUniqueId());

        Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> usingElevator.remove(p.getUniqueId()), 10);
    }
}
