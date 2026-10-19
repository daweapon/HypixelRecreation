package dev.daweapon.type.theend;

import dev.daweapon.type.generic.event.join.SpawnOnJoin;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.SkyBlockTypeLoader;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.PlayerDeathOverrides;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.SkyBlockTablistManager;

// SkyBlockTypeLoader for The End: spawn point, tablist, dimension, events, NPCs and the Remnant of the Eye death save.
public class TypeTheEndLoader implements SkyBlockTypeLoader {
    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_THE_END;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeTheEndLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        PlayerDeathOverrides.register(RemnantOfTheEye::saves);
        ZealotWaves.start();
        EndMobSpawner.start();
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
                (type) -> switch (type) {
                    default -> new Pos(-503, 101, -275, 90, 0);
                }, // Spawn position
                true // Announce death messages
        );
    }

    public TablistManager getTablistManager() {
        return new SkyBlockTablistManager(TabLocation.THE_END);
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SpawnOnJoin.intoMainInstance(SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.theend.events",
                HypixelEventClass.class
        ));
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return new ArrayList<>();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return new ArrayList<>(SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.theend.npcs",
                HypixelNPC.class
        ).toList());
    }

    @Override
    public @Nullable RegistryKey<DimensionType> getDimensionType() {
        return MinecraftServer.getDimensionTypeRegistry().register(
                Key.key("skyblock:the_end"),
                DimensionType.builder()
                        .skybox(DimensionType.Skybox.END)
                        .ambientLight(1f)
                        .build());
    }


    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return new ArrayList<>();
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return CustomWorlds.SKYBLOCK_THE_END;
    }
}
