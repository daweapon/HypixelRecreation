package dev.daweapon.type.goldmine;

import dev.daweapon.type.generic.event.join.SpawnOnJoin;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.SkyBlockTypeLoader;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;

import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.goldmine.entity.EntityLostPickaxe;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.SkyBlockTablistManager;

// Loader that wires up the Gold Mine server type: spawn point, tablist, NPCs, and events.
public class TypeGoldMineLoader implements SkyBlockTypeLoader {
    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_GOLD_MINE;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeGoldMineLoader initialized!");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        EntityLostPickaxe lostPickaxe = new EntityLostPickaxe();
        lostPickaxe.setInstance(HypixelConst.getInstanceContainer(), new Pos(-18.9, 24.5, -305.1));

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (lostPickaxe.isRemoved()) return;
            lostPickaxe.getInstance().sendGroupedPacket(
                    new ParticlePacket(
                            Particle.CRIT,
                            lostPickaxe.getPosition().add(0, 0, -0.3),
                            new Pos(0.3, 0.2, 0.3),
                            0.06f, 3
                    )
            );
        }).repeat(TaskSchedule.tick(20)).schedule();
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
                (type) -> switch (type) {
                    case SKYBLOCK_DEEP_CAVERNS -> new Pos(-8, 68, -393, 41, 0);
                    default -> new Pos(-4.5, 74, -278.5, -180, 0);
                }, // Spawn position
                true // Announce death messages
        );
    }

    public TablistManager getTablistManager() {
        return new SkyBlockTablistManager(TabLocation.GOLD_MINE);
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SpawnOnJoin.intoMainInstance(SkyBlockGenericLoader.loopThroughPackage(
                "dev.daweapon.type.goldmine.events",
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
                "dev.daweapon.type.goldmine.npcs",
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
        return CustomWorlds.SKYBLOCK_GOLD_MINE;
    }
}
