package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerDamageSkyBlockBlockEvent;
import dev.daweapon.type.skyblockgeneric.mythological.burrow.GriffinBurrowService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionGriffinBurrowDig implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDamageSkyBlockBlockEvent event) {
        if (event.getStatus() != ClientPlayerActionPacket.Status.STARTED_DIGGING) return;
        GriffinBurrowService.INSTANCE.dig((SkyBlockPlayer) event.getPlayer(), event.getBlockPosition());
    }
}
