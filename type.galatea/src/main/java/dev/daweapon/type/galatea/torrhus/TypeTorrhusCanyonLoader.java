package dev.daweapon.type.galatea.torrhus;

import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.Songs;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.SkyBlockTypeLoader;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.join.SpawnOnJoin;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.tab.SkyBlockTablistManager;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.user.QuestObjectives;
import dev.daweapon.type.skyblockgeneric.torrhus.Honeyhives;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public class TypeTorrhusCanyonLoader implements SkyBlockTypeLoader {
    public static final Pos SPAWN = new Pos(-720, 94, 141, -45, 0);

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_TORRHUS_CANYON;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        QuestObjectives.register(HeliaObjective::of);
        Logger.info("TypeTorrhusCanyonLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        Honeyhives.register(MinecraftServer.getSchedulerManager());
        dev.daweapon.type.skyblockgeneric.torrhus.DesertTemple.register(MinecraftServer.getSchedulerManager());
        dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpawner.start();
    }

    @Override
    public Songs getIslandSong() {
        return Songs.A_SILENT_MEMOIR;
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues((type) -> SPAWN, true);
    }

    public TablistManager getTablistManager() {
        return new SkyBlockTablistManager(TabLocation.TORRHUS_CANYON);
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SpawnOnJoin.intoMainInstance(SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.galatea.torrhus.events",
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
                "dev.daweapon.type.galatea.torrhus.npcs",
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
        return CustomWorlds.SKYBLOCK_TORRHUS_CANYON;
    }
}
