package net.swofty.type.thegarden.events.traditional;

import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.thegarden.TypeGardenLoader;

public class ActionPlayerJoin implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.CONNECT)
    public void run(AsyncPlayerConfigurationEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        event.setSpawningInstance(HypixelConst.getEmptyInstance());

        player.setRespawnPoint(TypeGardenLoader.SPAWN_POS);
    }
}
