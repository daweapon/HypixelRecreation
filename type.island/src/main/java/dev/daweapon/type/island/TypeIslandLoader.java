package dev.daweapon.type.island;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelTypeLoader;
import dev.daweapon.type.generic.SkyBlockTypeLoader;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;

import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.island.lifecycle.IslandLifecycleSteps;
import dev.daweapon.type.island.npcs.NPCStranger;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.minion.MinionHandler;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import dev.daweapon.type.island.tab.IslandPlayersBlock;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.SkyBlockTablistManager;

// Server type loader that configures and initializes the Island server (a player's private SkyBlock island).
public class TypeIslandLoader implements SkyBlockTypeLoader {
    public static final Pos SPAWN_POS = new Pos(7.5, 100, 7.5);

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_ISLAND;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeIslandLoader initialized!");

        IslandLifecycleSteps.register();
        new MinionHandler(MinecraftServer.getSchedulerManager()).start();
    }

    @Override
    public void afterInitialize(MinecraftServer server) {

    }

    @Override
    public HypixelTypeLoader.LoaderValues getLoaderValues() {
        return new HypixelTypeLoader.LoaderValues(
            (_) -> SPAWN_POS, // Spawn position
                true // Announce death messages
        );
    }

    @Override
    public TablistManager getTablistManager() {
        return new SkyBlockTablistManager(TabLocation.PRIVATE_ISLAND, new IslandPlayersBlock());
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.island.events.traditional",
                HypixelEventClass.class
        ).collect(Collectors.toList());
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return List.of();
    }

    @Override
    public boolean usesPerPlayerNpcInstances() {
        return true;
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return new ArrayList<>(List.of(new NPCStranger()));
    }


    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of(ServiceType.AUCTION_HOUSE, ServiceType.ITEM_TRACKER);
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return null;
    }
}
