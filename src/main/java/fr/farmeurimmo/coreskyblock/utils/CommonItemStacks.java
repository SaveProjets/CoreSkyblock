package fr.farmeurimmo.coreskyblock.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.codehaus.plexus.util.Base64;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CommonItemStacks {

    private static final UUID uuid = UUID.randomUUID();
    private static final JsonParser parser = new JsonParser();
    private static final String API_PROFILE_LINK = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Map<UUID, String> textures = new java.util.HashMap<>();

    public static ItemStack getCommonGlassPane(Material pane) {
        return ItemBuilder.copyOf(new ItemStack(pane))
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

    public static ItemStack getCommonMovingRight() {
        return ItemBuilder.copyOf(getHead("https://textures.minecraft.net/texture/956a3618459e43b287b22b7e235ec699594546c6fcd6dc84bfca4cf30ab9311"))
                .name("§8➡ §7Déplacer à droite")
                .build();
    }

    public static ItemStack getCommonMovingLeft() {
        return ItemBuilder.copyOf(getHead("https://textures.minecraft.net/texture/cdc9e4dcfa4221a1fadc1b5b2b11d8beeb57879af1c42362142bae1edd5"))
                .name("§8⬅ §7Déplacer à gauche")
                .build();
    }

    public static String getArrowWithColors(boolean isActive, boolean isNegate) {
        return (isActive ? "§f➟ " + (isNegate ? "§c" : "§a") : "§f▶ §8");
    }

    public static void applyCommonPanes(Material pane, Inventory inv) {
        ItemStack glassPane = getCommonGlassPane(pane);

        inv.setItem(0, glassPane);
        inv.setItem(8, glassPane);

        if (inv.getSize() <= 9) return;
        inv.setItem(9, glassPane);
        inv.setItem(17, glassPane);

        if (inv.getSize() <= 18) return;

        inv.setItem(inv.getSize() - 18, glassPane);
        inv.setItem(inv.getSize() - 9, glassPane);
        inv.setItem(inv.getSize() - 1, glassPane);
        inv.setItem(inv.getSize() - 10, glassPane);
    }

    public static ItemStack getHead(String skinURL) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (skinURL == null || skinURL.isEmpty()) {
            return head;
        }

        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinURL).getBytes());

        head.editMeta(SkullMeta.class, skullMeta -> {
            final UUID uuid = UUID.randomUUID();
            final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
            playerProfile.setProperty(new ProfileProperty("textures", new String(encodedData)));

            skullMeta.setPlayerProfile(playerProfile);
        });

        return head;
    }

    public static ItemStack getCached(UUID uuid, String name) {
        if (!textures.containsKey(uuid)) {
            return null;
        }
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        return getHeadApplied(head, textures.get(uuid), uuid);
    }

    @NotNull
    private static ItemStack getHeadApplied(ItemStack head, String texture, UUID uuid) {
        head.editMeta(SkullMeta.class, skullMeta -> {
            final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
            playerProfile.setProperty(new ProfileProperty("textures", texture));

            skullMeta.setPlayerProfile(playerProfile);
        });

        return head;
    }

    public static String getSkinUrl(String uuid) {
        if (textures.containsKey(UUID.fromString(uuid))) {
            return textures.get(UUID.fromString(uuid));
        }
        String json = getContent(API_PROFILE_LINK + uuid);
        if (json == null) {
            return null;
        }
        JsonObject o = JsonParser.parseReader(new JsonReader(new java.io.StringReader(json))).getAsJsonObject();
        String jsonBase64 = o.get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();

        o = JsonParser.parseString(new String(java.util.Base64.getDecoder().decode(jsonBase64))).getAsJsonObject();

        return o.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
    }

    private static String getContent(String link) {
        try {
            URL url = URI.create(link).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    content.append(inputLine);
                }
                return content.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static CompletableFuture<ItemStack> getHead(UUID uuid, String name) {
        return CompletableFuture.supplyAsync(() -> {
            textures.put(uuid, new String(Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", getSkinUrl(uuid.toString())).getBytes())));

            return getHeadApplied(new ItemStack(Material.PLAYER_HEAD), textures.get(uuid), uuid);
        });
    }
}
