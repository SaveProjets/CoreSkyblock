package fr.farmeurimmo.coreskyblock.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.codehaus.plexus.util.Base64;

import java.lang.reflect.Field;
import java.util.UUID;

public class CommonItemStacks {

    public static ItemStack getCommonGlassPane() {
        return ItemBuilder.copyOf(new ItemStack(Material.LIME_STAINED_GLASS_PANE))
                .enchant(Enchantment.UNBREAKING)
                .flags(ItemFlag.HIDE_ENCHANTS)
                .name("§7")
                .build();
    }

    public static ItemStack getCommonBack() {
        return ItemBuilder.copyOf(new ItemStack(Material.ARROW))
                .name("§8⬇ §7Retour §8⬇")
                .build();
    }

    public static ItemStack getCommonNextPage() {
        return ItemBuilder.copyOf(getHead("https://textures.minecraft.net/texture/956a3618459e43b287b22b7e235ec699594546c6fcd6dc84bfca4cf30ab9311"))
                .name("§8➡ §7Page Suivante")
                .build();
    }

    public static ItemStack getCommonPreviousPage() {
        return ItemBuilder.copyOf(getHead("https://textures.minecraft.net/texture/cdc9e4dcfa4221a1fadc1b5b2b11d8beeb57879af1c42362142bae1edd5"))
                .name("§8⬅ §7Page Précédente")
                .build();
    }

    public static ItemStack getHead(String skinURL) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (skinURL == null || skinURL.isEmpty()) {
            return head;
        }
        ItemMeta headMeta = head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "a");
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinURL).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        profileField.setAccessible(true);
        try {
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }

}
