package dev.daweapon.type.dungeonhub;

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

// SkyBlockTypeLoader for the Dungeon Hub server type: defines its spawn point, tablist, and events/NPCs, and requires the item tracker service.
public class TypeDungeonHubLoader implements SkyBlockTypeLoader {

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_DUNGEON_HUB;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeDungeonHubLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {

    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
                (type) -> switch (type) {
                    default -> new Pos(-28.5, 121, 0.5, 90, 0);
                }, // Spawn position
                false // Announce death messages
        );
    }

    public TablistManager getTablistManager() {
        return new SkyBlockTablistManager(TabLocation.DUNGEON_HUB);
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SpawnOnJoin.intoMainInstance(SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.dungeonhub.events",
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
                "dev.daweapon.type.dungeonhub.npcs",
                HypixelNPC.class
        ).toList());
    }


    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of(ServiceType.ITEM_TRACKER);
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return CustomWorlds.SKYBLOCK_DUNGEON_HUB;
    }
}
