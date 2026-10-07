package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.PlayerDamageSkyBlockBlockEvent;
import net.swofty.type.skyblockgeneric.mythological.burrow.GriffinBurrowService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionGriffinBurrowDig implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDamageSkyBlockBlockEvent event) {
        if (event.getStatus() != ClientPlayerActionPacket.Status.STARTED_DIGGING) return;
        GriffinBurrowService.INSTANCE.dig((SkyBlockPlayer) event.getPlayer(), event.getBlockPosition());
    }
}
