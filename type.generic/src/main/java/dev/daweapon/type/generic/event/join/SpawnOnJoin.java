package dev.daweapon.type.generic.event.join;

import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class SpawnOnJoin implements HypixelEventClass {
    private final Supplier<? extends Instance> spawningInstance;

    private SpawnOnJoin(Supplier<? extends Instance> spawningInstance) {
        this.spawningInstance = spawningInstance;
    }

    public static List<HypixelEventClass> intoMainInstance(Stream<HypixelEventClass> events) {
        return with(events, new SpawnOnJoin(HypixelConst::getInstanceContainer));
    }

    public static List<HypixelEventClass> intoEmptyInstance(Stream<HypixelEventClass> events) {
        return with(events, new SpawnOnJoin(HypixelConst::getEmptyInstance));
    }

    private static List<HypixelEventClass> with(Stream<HypixelEventClass> events, SpawnOnJoin join) {
        List<HypixelEventClass> all = new ArrayList<>(events.toList());
        all.add(join);
        return all;
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.CONNECT)
    public void run(AsyncPlayerConfigurationEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        event.setSpawningInstance(spawningInstance.get());
        player.setRespawnPoint(HypixelConst.getTypeLoader()
                .getLoaderValues()
                .spawnPosition()
                .apply(player.getOriginServer())
        );
    }
}
