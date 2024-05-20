package fr.farmeurimmo.coreskyblock.purpur.minions;

import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;

import java.util.List;

public class MinionsManager {

    public static MinionsManager INSTANCE;

    public MinionsManager() {
        INSTANCE = this;
    }

    public void giveMinion(Player p, MinionType type, int level) {
        ItemStack minion = new ItemStack(Material.DRAGON_BREATH, 1);
        minion.setUnbreakable(true);
        ItemMeta meta = minion.getItemMeta();
        meta.displayName(Component.text("§6Minion " + type.getName()));
        meta.lore(List.of(Component.text("§6Niveau §e" + level)));
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        minion.setItemMeta(meta);

        p.getInventory().addItem(minion);
    }

    public boolean isAMinion(ItemStack item) {
        return item.getType() == Material.DRAGON_BREATH && item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().contains("§6Minion");
    }

    public void spawnMinion(Minion minion) {
        if (!minion.getMinionLocation().getDirection().equals(minion.getBlockFace().getDirection())) {
            minion.getMinionLocation().setDirection(minion.getBlockFace().getDirection());
        }
        final ArmorStand armorStand = (ArmorStand) minion.getMinionLocation().getWorld().spawnEntity(
                minion.getMinionLocation(), EntityType.ARMOR_STAND);
        final EntityEquipment equipment = armorStand.getEquipment();

        armorStand.setMetadata("minion", new FixedMetadataValue(CoreSkyblock.INSTANCE, minion));
        armorStand.setVisible(true);
        armorStand.setGravity(false);
        armorStand.customName(Component.text("§6Minion " + minion.getType().getName()));
        armorStand.setCustomNameVisible(true);
        armorStand.setSmall(true);
        armorStand.setInvulnerable(true);
        armorStand.setCollidable(false);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);

        armorStand.setRightLegPose(new EulerAngle(0, 0, -50));
        armorStand.setLeftLegPose(new EulerAngle(0, 0, 50));
        armorStand.setRightArmPose(new EulerAngle(206, 0, 0));

        equipment.setHelmet(MinionType.getMinionHelmet(minion.getType()));
        equipment.setChestplate(MinionType.getMinionChestplate(minion.getType()));
        equipment.setLeggings(MinionType.getMinionLeggings(minion.getType()));
        equipment.setBoots(MinionType.getMinionBoots(minion.getType()));
        equipment.setItemInMainHand(MinionType.getTool(minion.getType()));
    }

    public void despawnMinion(Minion minion) {
        for (ArmorStand armorStand : minion.getMinionLocation().getWorld().getEntitiesByClass(ArmorStand.class)) {
            if (armorStand.getLocation().getBlock().getLocation().equals(minion.getMinionLocation().getBlock().getLocation())
                    && !armorStand.hasGravity()) {
                armorStand.remove();
            }
        }
    }
}
