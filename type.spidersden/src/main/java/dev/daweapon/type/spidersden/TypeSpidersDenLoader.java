package dev.daweapon.type.spidersden;

import dev.daweapon.type.generic.event.join.SpawnOnJoin;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.SkyBlockTypeLoader;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.SkyBlockTablistManager;

// Loader that wires up the Spider's Den server type: spawn point, tablist, NPCs, and events.
public class TypeSpidersDenLoader implements SkyBlockTypeLoader {
    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_SPIDERS_DEN;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeSpidersDenLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {

    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
                (type) -> switch (type) {
                    case SKYBLOCK_THE_END -> new Pos(-378, 118.5, -261, -90, 0);
                    case SKYBLOCK_CRIMSON_ISLE -> new Pos(-355, 87, -347, -20, 0);
                    default -> new Pos(-202.5, 83, -233.5, 135, 0);
                }, // Spawn position
                true // Announce death messages
        );
    }

    public TablistManager getTablistManager() {
        return new SkyBlockTablistManager(TabLocation.SPIDERS_DEN);
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SpawnOnJoin.intoMainInstance(SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.spidersden.events",
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
                "dev.daweapon.type.spidersden.npcs",
                HypixelNPC.class
        ).toList());
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
        return CustomWorlds.SKYBLOCK_SPIDERS_DEN;
    }
}
