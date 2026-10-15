package dev.daweapon.type.island.events.traditional;

import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.island.npcs.NPCStranger;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionStrangerAppears implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_DATA)
    public void run(AsyncPlayerConfigurationEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!NPCStranger.isEligible(player)) return;
        player.sendMessage("<b>A Stranger<f> appears on your island at <e>8, 100, 41<f>! You should go talk to them.");
    }
}
