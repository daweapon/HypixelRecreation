package dev.daweapon.type.crimsonisle;

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
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.crimsonisle.faction.TownVillagers;
import dev.daweapon.type.crimsonisle.tab.CrimsonIsleInfoModule;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.tabmodules.AccountInformationModule;
import dev.daweapon.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// SkyBlockTypeLoader for the Crimson Isle server type: defines its spawn point, tablist, and events/NPCs.
public class TypeCrimsonIsleLoader implements SkyBlockTypeLoader {
    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_CRIMSON_ISLE;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeCrimsonIsleLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        CrimsonIsleServices.start();
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
            (type) -> new Pos(-360.5, 80, -430.5, -180, 0), // Spawn position
                true // Announce death messages
        );
    }

    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return new ArrayList<>(List.of(
                        new SkyBlockPlayersOnlineModule(1, TypeCrimsonIsleLoader::factionIcon),
                        new SkyBlockPlayersOnlineModule(2, TypeCrimsonIsleLoader::factionIcon),
                        new CrimsonIsleInfoModule(),
                        new AccountInformationModule()
                ));
            }
        };
    }

    private static String factionIcon(SkyBlockPlayer player) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        return faction == null ? "" : " " + faction.getColour() + faction.getIcon();
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SpawnOnJoin.intoMainInstance(SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.crimsonisle.events",
                HypixelEventClass.class
        ));
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return new ArrayList<>();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        List<HypixelNPC> npcs = new ArrayList<>(SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.crimsonisle.npcs",
                HypixelNPC.class
        ).toList());
        npcs.addAll(TownVillagers.all());
        return npcs;
    }


    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return new ArrayList<>(List.of(ServiceType.AUCTION_HOUSE, ServiceType.BAZAAR, ServiceType.ITEM_TRACKER));
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return CustomWorlds.SKYBLOCK_CRIMSON_ISLE;
    }
}
