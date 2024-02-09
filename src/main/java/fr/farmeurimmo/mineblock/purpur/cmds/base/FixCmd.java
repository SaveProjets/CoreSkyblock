package fr.farmeurimmo.mineblock.purpur.cmds.base;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FixCmd implements CommandExecutor, TabCompleter {

    public static final int COOLDOWN = 60 * 60 * 3;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("§cVous devez être un joueur pour exécuter cette commande."));
            return false;
        }
        if (cooldowns.containsKey(p.getUniqueId())) {
            long secondsLeft = ((cooldowns.get(p.getUniqueId()) / 1000) + COOLDOWN) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                p.sendMessage(Component.text("§cVous devez attendre " + secondsLeft + " secondes avant de pouvoir réparer un item."));
                return false;
            }
        }
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            for (ItemStack item : p.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR && item.getDamage() > 0) {
                    itemStacks.add(item);
                }
            }
            for (ItemStack item : p.getInventory().getArmorContents()) {
                if (item != null && item.getType() != Material.AIR && item.getDamage() > 0) {
                    itemStacks.add(item);
                }
            }
            if (itemStacks.isEmpty()) {
                p.sendMessage(Component.text("§cVous n'avez aucun item à réparer."));
                return false;
            }
            for (ItemStack item : itemStacks) {
                item.setDamage(0);
            }
            p.sendMessage(Component.text("§aTous vos items ont été réparés."));
            cooldowns.put(p.getUniqueId(), System.currentTimeMillis());
            return false;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            p.sendMessage(Component.text("§cVous devez avoir un item en main."));
            return false;
        }
        if (item.getDamage() == 0) {
            p.sendMessage(Component.text("§cCet item n'a pas besoin d'être réparé."));
            return false;
        }
        item.setDamage(0);
        p.sendMessage(Component.text("§aVotre item a été réparé."));
        cooldowns.put(p.getUniqueId(), System.currentTimeMillis());
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
