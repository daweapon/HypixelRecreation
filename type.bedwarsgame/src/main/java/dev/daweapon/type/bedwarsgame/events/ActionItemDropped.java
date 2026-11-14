package dev.daweapon.type.bedwarsgame.events;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.entity.EntitySpawnEvent;
import dev.daweapon.type.bedwarsgame.TypeBedWarsGameLoader;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;

// Records dropped-item entity spawns to the replay manager when a game is being recorded.
public class ActionItemDropped implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, isAsync = false)
    public void run(EntitySpawnEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            BedWarsGame game = TypeBedWarsGameLoader.getGameByInstance(event.getInstance());
            if (game == null) return;

            if (!game.getReplayManager().isRecording()) return;

            game.getReplayManager().recordDroppedItem(itemEntity);
        }
    }

}
