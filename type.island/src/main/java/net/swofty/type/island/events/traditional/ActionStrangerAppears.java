package net.swofty.type.island.events.traditional;

import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.island.npcs.NPCStranger;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionStrangerAppears implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_DATA)
    public void run(AsyncPlayerConfigurationEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!NPCStranger.isEligible(player)) return;
        player.sendMessage("<b>A Stranger<f> appears on your island at <e>8, 100, 41<f>! You should go talk to them.");
    }
}
