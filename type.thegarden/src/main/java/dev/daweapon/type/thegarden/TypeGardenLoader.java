package dev.daweapon.type.thegarden;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelTypeLoader;
import dev.daweapon.type.generic.SkyBlockTypeLoader;
import dev.daweapon.type.generic.entity.hologram.PlayerHolograms;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.garden.farm.GardenCropGrowth;
import dev.daweapon.type.skyblockgeneric.garden.npc.CarpenterNpc;
import dev.daweapon.type.skyblockgeneric.garden.npc.JakeNpc;
import dev.daweapon.type.skyblockgeneric.garden.npc.JeffNpc;
import dev.daweapon.type.skyblockgeneric.garden.npc.NPCSam;
import dev.daweapon.type.skyblockgeneric.garden.npc.PesthunterNpc;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorNpc;
import dev.daweapon.type.thegarden.world.VisitorTicker;
import dev.daweapon.type.skyblockgeneric.user.AreaSidebar;
import dev.daweapon.type.skyblockgeneric.user.garden.SkyBlockGarden;
import dev.daweapon.type.thegarden.world.GardenSidebar;
import dev.daweapon.type.thegarden.world.GardenWorldDecor;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.SkyBlockTablistManager;

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
        return new SkyBlockTablistManager(TabLocation.GARDEN);
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.thegarden.events.traditional",
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
        for (int i = 0; i < 3; i++) {
            npcs.add(new CarpenterNpc(i));
            npcs.add(new JakeNpc(i));
        }
        npcs.add(new JeffNpc());
        for (PesthunterNpc.Kind kind : PesthunterNpc.Kind.values()) npcs.add(PesthunterNpc.of(kind));
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
