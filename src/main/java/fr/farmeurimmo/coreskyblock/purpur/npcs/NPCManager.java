package fr.farmeurimmo.coreskyblock.purpur.npcs;

import com.github.juliarn.npclib.api.Npc;
import com.github.juliarn.npclib.api.NpcActionController;
import com.github.juliarn.npclib.api.Platform;
import com.github.juliarn.npclib.api.event.InteractNpcEvent;
import com.github.juliarn.npclib.api.profile.Profile;
import com.github.juliarn.npclib.bukkit.BukkitPlatform;
import com.github.juliarn.npclib.bukkit.BukkitProfileResolver;
import com.github.juliarn.npclib.bukkit.BukkitVersionAccessor;
import com.github.juliarn.npclib.bukkit.BukkitWorldAccessor;
import com.github.juliarn.npclib.bukkit.protocol.BukkitProtocolAdapter;
import com.github.juliarn.npclib.bukkit.util.BukkitPlatformUtil;
import fr.farmeurimmo.coreskyblock.purpur.CoreSkyblock;
import fr.farmeurimmo.coreskyblock.purpur.items.enchants.invs.EnchantsMainInv;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NPCManager {

    public static NPCManager INSTANCE;
    private final Platform platform;

    public NPCManager() {
        INSTANCE = this;

        this.platform = BukkitPlatform.bukkitNpcPlatformBuilder()
                // sets the extension of the platform, which is usually the plugin that
                // uses the plugin. on bukkit this is for example used to schedule sync
                // tasks using the bukkit scheduler.
                // This option has no default and must be set.
                .extension(CoreSkyblock.INSTANCE)
                // the resolver to use for npc profiles. the resolver is given an unresolved
                // version of a profile (for example only an uuid or name) and completes the
                // profile data (name, uuid, textures).
                // see BukkitProfileResolver for the available resolvers on bukkit (paper or spigot)
                // Defaults to a platform-specific implementation.
                .profileResolver(BukkitProfileResolver.profileResolver())
                // the resolver for worlds of npcs. each npc position contains a world identifier
                // which is resolved using this resolver. the resolver can also provide the
                // identifier of a world using the world instance.
                // see BukkitWorldAccessor for the available resolvers on bukkit (name or key based)
                // see MinestomWorldAccessor for the available resolver on minestom (uuid based)
                // Defaults to a platform-specific implementation.
                .worldAccessor(BukkitWorldAccessor.worldAccessor())
                // the provider for version information about the current platform. it's internally
                // used to determine which features can be used. an example is the profile resolver:
                // when on paper 1.12 or later the paper profile resolver is used, when on spigot
                // 1.18.2 or later the spigot profile resolver is used, else a fallback mojang api
                // based access is used.
                // see BukkitVersionAccessor for the bukkit implementations (PaperLib based)
                // see MinestomVersionAccessor for the minestom implementation
                // Defaults to a platform-specific implementation.
                .versionAccessor(BukkitVersionAccessor.versionAccessor())
                // the factory for packets that need to be sent in order to spawn and manage npcs.
                // see BukkitProtocolAdapter for the available options on Bukkit (ProtocolLib or PacketEvents)
                // see MinestomProtocolAdapter for the minestom implementation
                // Defaults to a platform-specific implementation.
                .packetFactory(BukkitProtocolAdapter.protocolLib())
                // configures the default action controller for the platform. if this method isn't called
                // during the build process, the default action controller is disabled. the method provides
                // a builder which can be used to modify the settings of the action controller. if the default
                // values should be used, don't call any methods and just provide an empty lambda.
                .actionController(builder -> builder
                        .flag(NpcActionController.SPAWN_DISTANCE, 60))
                // builds the final platform object which can then be used to spawn and manage npcs
                .build();

        spawnNpcEnchants();
    }

    public void spawnNpcEnchants() {
        Location loc = CoreSkyblock.SPAWN.clone().add(0, 0, -5);

        UUID uuid = UUID.randomUUID();

        Npc npc = platform.newNpcBuilder()
                .position(BukkitPlatformUtil.positionFromBukkitModern(loc))
                .profile(Profile.resolved("§6Enchanteur", uuid))
                .buildAndTrack();

        npc.flagValue(Npc.SNEAK_WHEN_PLAYER_SNEAKS, true);
        npc.flagValue(Npc.LOOK_AT_PLAYER, true);
        npc.platform().eventManager().registerEventHandler(InteractNpcEvent.class, e -> {
            if (e.hand() == InteractNpcEvent.Hand.OFF_HAND) return;

            Player player = e.player();

            Bukkit.getScheduler().callSyncMethod(CoreSkyblock.INSTANCE, () -> {
                new EnchantsMainInv().open(player);
                return null;
            });
        });
    }
}
