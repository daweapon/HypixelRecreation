package net.swofty.type.crimsonisle;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.generic.SkyBlockTypeLoader;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.crimsonisle.faction.TownVillagers;
import net.swofty.type.crimsonisle.tab.CrimsonIsleInfoModule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.tabmodules.AccountInformationModule;
import net.swofty.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
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
        return SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.crimsonisle.events",
                HypixelEventClass.class
        ).collect(Collectors.toList());
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return new ArrayList<>();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        List<HypixelNPC> npcs = new ArrayList<>(SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.crimsonisle.npcs",
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
