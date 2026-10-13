package net.swofty.type.thegarden;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.generic.HypixelTypeLoader;
import net.swofty.type.generic.SkyBlockTypeLoader;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.garden.farm.GardenCropGrowth;
import net.swofty.type.skyblockgeneric.garden.npc.NPCSam;
import net.swofty.type.skyblockgeneric.garden.npc.PesthunterNpc;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorNpc;
import net.swofty.type.thegarden.world.VisitorTicker;
import net.swofty.type.skyblockgeneric.tabmodules.AccountInformationModule;
import net.swofty.type.skyblockgeneric.user.AreaSidebar;
import net.swofty.type.skyblockgeneric.user.garden.SkyBlockGarden;
import net.swofty.type.thegarden.tab.GardenPestsModule;
import net.swofty.type.thegarden.tab.GardenComposterModule;
import net.swofty.type.thegarden.tab.GardenInfoModule;
import net.swofty.type.thegarden.world.GardenSidebar;
import net.swofty.type.thegarden.world.GardenWorldDecor;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeGardenLoader implements SkyBlockTypeLoader {
    public static final Pos SPAWN_POS = new Pos(-5.5, 71, 17.5, 180, 0);

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_THE_GARDEN;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeGardenLoader initialized!");

        SkyBlockGarden.setPresence(new GardenWorldDecor());
        AreaSidebar.register(GardenSidebar::area);
        AreaSidebar.registerDetail(GardenSidebar::detail);
        MinecraftServer.getSchedulerManager()
                .buildTask(VisitorTicker::tick)
                .repeat(TaskSchedule.seconds(1))
                .schedule();
        MinecraftServer.getSchedulerManager()
                .buildTask(VisitorTicker::tickVacuums)
                .repeat(TaskSchedule.tick(2))
                .schedule();
        MinecraftServer.getSchedulerManager()
                .buildTask(GardenCropGrowth::tick)
                .repeat(TaskSchedule.seconds(1))
                .schedule();
        MinecraftServer.getSchedulerManager()
                .buildTask(PlayerHolograms::updateExternalHolograms)
                .repeat(TaskSchedule.seconds(1))
                .schedule();
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
    }

    @Override
    public HypixelTypeLoader.LoaderValues getLoaderValues() {
        return new HypixelTypeLoader.LoaderValues(
                (_) -> SPAWN_POS,
                true
        );
    }

    @Override
    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return new ArrayList<>(List.of(
                        new GardenComposterModule(),
                        new GardenPestsModule(),
                        new GardenInfoModule(),
                        new AccountInformationModule()
                ));
            }
        };
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.thegarden.events.traditional",
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
        List<HypixelNPC> npcs = new ArrayList<>();
        npcs.add(NPCSam.get());
        for (PesthunterNpc.Kind kind : PesthunterNpc.Kind.values()) npcs.add(new PesthunterNpc(kind));
        VisitorCatalogue.get().visitors().stream().filter(VisitorCatalogue.Visitor::available)
                .forEach(visitor -> npcs.add(new VisitorNpc(visitor)));
        return npcs;
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
