package dev.daweapon.type.ravengardgeneric.event.actions.player;

import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.ravengardgeneric.user.RavengardPlayer;
import org.tinylog.Logger;

public class ActionPlayerJoin implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, phase = EventPhase.GAMEPLAY)
    public void run(AsyncPlayerConfigurationEvent event) {
        RavengardPlayer player = (RavengardPlayer) event.getPlayer();

        event.setSpawningInstance(HypixelConst.getEmptyInstance());
        player.setRespawnPoint(HypixelConst.getTypeLoader()
                .getLoaderValues()
                .spawnPosition()
                .apply(player.getOriginServer())
        );

        Logger.info("Player {} joined Ravengard from {}", player.getUsername(), player.getOriginServer());
    }
}
