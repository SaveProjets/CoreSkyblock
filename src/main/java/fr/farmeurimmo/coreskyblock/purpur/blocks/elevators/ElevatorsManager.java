package fr.farmeurimmo.coreskyblock.purpur.blocks.elevators;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class ElevatorsManager {

    public static final int MAX_ELEVATOR_HEIGHT = 30;
    public static ElevatorsManager INSTANCE;
    public final ArrayList<UUID> usingElevator = new ArrayList<>();

    public ElevatorsManager() {
        INSTANCE = this;
    }

    public final ItemStack getElevator() {
        return getElevatorCustomStack().getItemStack();
    }

    public final CustomStack getElevatorCustomStack() {
        return CustomStack.getInstance("elevator");
    }

    public void giveElevator(Player p) {
        CustomStack customStack = getElevatorCustomStack();
        if (customStack != null) {
            if (p.getInventory().firstEmpty() == -1) {
                p.getWorld().dropItem(p.getLocation(), getElevator());
                p.sendMessage(Component.text("§aVotre inventaire est plein, un ascenseur a été déposé au sol."));
            } else {
                p.getInventory().addItem(getElevator());
                p.sendMessage(Component.text("§aVous avez reçu un ascenseur."));
            }
        } else {
            p.sendMessage(Component.text("§cErreur lors de la récupération de l'ascenseur."));
        }
    }

    public boolean isAnElevator(CustomBlock customBlock) {
        if (customBlock != null) {
            return Objects.equals(customBlock.getNamespace(), getElevatorCustomStack().getNamespace());
        }
        return false;
    }

    public boolean isAnElevator(String namespaceID) {
        return Objects.equals(namespaceID, getElevatorCustomStack().getNamespacedID());

    }

    public void addUsingElevator(Player p) {
        usingElevator.add(p.getUniqueId());

        Bukkit.getScheduler().runTaskLater(CoreSkyblock.INSTANCE, () -> usingElevator.remove(p.getUniqueId()), 7);
    }
}
