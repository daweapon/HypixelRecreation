package dev.daweapon.type.generic;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.Tuple;
import dev.daweapon.commons.text.Text;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.data.GameDataHandler;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// Contract a specific game-type module (SkyBlock, BedWars, etc.) implements to plug into the generic server bootstrap.
public interface HypixelTypeLoader {
    ServerType getType();

    void onInitialize(MinecraftServer server);

    void afterInitialize(MinecraftServer server);

    List<ServiceType> getRequiredServices();

    TablistManager getTablistManager();

    LoaderValues getLoaderValues();

    List<HypixelEventClass> getTraditionalEvents();

    List<HypixelEventClass> getCustomEvents();

    List<HypixelNPC> getNPCs();

    default boolean usesPerPlayerNpcInstances() {
        return false;
    }

    List<RedisMessageHandler<?, ?>> getProxyHandlers();

    record LoaderValues(Function<ServerType, Pos> spawnPosition, boolean announceDeathMessages) {
        public LoaderValues(Pos spawnPosition, boolean announceDeathMessages) {
            this(_ -> spawnPosition, announceDeathMessages);
        }
    }

    @Nullable CustomWorlds getMainInstance();

    /**
     * Returns additional game-specific data handlers this server needs.
     * These handlers will be automatically loaded/saved on player join/quit.
     * @return List of GameDataHandler classes to load
     */
    default List<RedisMessageHandler<?, ?>> getServiceHandlers() {
        return List.of();
    }

    default List<Class<? extends GameDataHandler>> getAdditionalDataHandlers() {
        return List.of();
    }

    /**
     * Gets the dimension type for this server type, or null if the default should be used. You should also register
     * the DimensionType to the registry in the same method.
     * @return The dimension type for this server type, or null if the default should be used.
     */
    @Nullable default RegistryKey<DimensionType> getDimensionType() {
        return null;
    }

    @NotNull
    default boolean headerFooterPerPlayer() {
        return false;
    }

    default Optional<Tuple<Text, Text>> headerFooter() {
        return Optional.empty();
    }
}
