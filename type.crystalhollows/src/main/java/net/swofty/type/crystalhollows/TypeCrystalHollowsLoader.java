package net.swofty.type.crystalhollows;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.crystalhollows.layout.CrystalHollowsLayout;
import net.swofty.type.generic.SkyBlockTypeLoader;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.AreaServerModule;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.tabmodules.AccountInformationModule;
import net.swofty.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeCrystalHollowsLoader implements SkyBlockTypeLoader {
    public static final Pos SPAWN_POS = new Pos(512.5, 120, 512.5, 0, 0);

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_CRYSTAL_HOLLOWS;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeCrystalHollowsLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        CrystalHollowsLayout.generate();
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues((_) -> SPAWN_POS, true);
    }

    @Override
    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return new ArrayList<>(List.of(
                        new SkyBlockPlayersOnlineModule(1),
                        new SkyBlockPlayersOnlineModule(2),
                        new AreaServerModule("tablist.server_info.area.crystal_hollows"),
                        new AccountInformationModule()
                ));
            }
        };
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.crystalhollows.events",
                HypixelEventClass.class
        ).collect(Collectors.toList());
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return new ArrayList<>();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return new ArrayList<>(SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.crystalhollows.npcs",
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
