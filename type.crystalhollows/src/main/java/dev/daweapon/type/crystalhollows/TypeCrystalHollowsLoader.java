package dev.daweapon.type.crystalhollows;

import dev.daweapon.type.generic.event.join.SpawnOnJoin;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.crystalhollows.boss.CrystalHollowsBossSpawner;
import dev.daweapon.type.crystalhollows.command.HollowsCommand;
import dev.daweapon.type.crystalhollows.divan.DivanDigSite;
import dev.daweapon.type.crystalhollows.layout.HollowsWorld;
import dev.daweapon.type.crystalhollows.layout.HollowsLayoutRefresh;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.crystalhollows.claim.CrystalDisplays;
import dev.daweapon.type.crystalhollows.quest.KingsScentWater;
import dev.daweapon.type.crystalhollows.lifecycle.CrystalHollowsLobby;
import dev.daweapon.type.crystalhollows.redis.RedisIsLobbyJoinable;
import dev.daweapon.type.generic.SkyBlockTypeLoader;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.crystalhollows.HeatService;
import dev.daweapon.type.skyblockgeneric.crystalhollows.MetalDetector;
import dev.daweapon.type.skyblockgeneric.crystalhollows.HollowsSpawnProtection;
import dev.daweapon.type.skyblockgeneric.user.AreaSidebar;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.SkyBlockTablistManager;

public class TypeCrystalHollowsLoader implements SkyBlockTypeLoader {

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_CRYSTAL_HOLLOWS;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        HypixelCommand.register(new HollowsCommand());
        Logger.info("TypeCrystalHollowsLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        HollowsLayoutRefresh.run(HypixelConst.getInstanceContainer());
        MetalDetector.install(new DivanDigSite());
        MetalDetector.start();
        HeatService.start();
        HollowsSpawnProtection.start();
        CrystalHollowsLobby.start();
        AreaSidebar.registerDetail(CrystalHollowsSidebar::detail);
        CrystalHollowsBossSpawner.start();
        CrystalDisplays.start();
        KingsScentWater.start();
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues((_) -> HollowsWorld.get().spawnOrPlaceholder(), true);
    }

    @Override
    public TablistManager getTablistManager() {
        return new SkyBlockTablistManager(TabLocation.CRYSTAL_HOLLOWS);
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SpawnOnJoin.intoMainInstance(SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.crystalhollows.events",
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
                "dev.daweapon.type.crystalhollows.npcs",
                HypixelNPC.class
        ).toList());
    }

    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of(new RedisIsLobbyJoinable());
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return new ArrayList<>();
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return CustomWorlds.SKYBLOCK_CRYSTAL_HOLLOWS;
    }

    @Override
    public @Nullable RegistryKey<DimensionType> getDimensionType() {
        return MinecraftServer.getDimensionTypeRegistry().register(
                Key.key("skyblock:crystal_hollows"),
                DimensionType.builder()
                        .ambientLight(0.4f)
                        .build());
    }
}
