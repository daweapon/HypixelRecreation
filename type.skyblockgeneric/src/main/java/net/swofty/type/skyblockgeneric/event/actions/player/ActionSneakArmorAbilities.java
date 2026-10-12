package net.swofty.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerInputEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.item.armor.SneakAbilities;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionSneakArmorAbilities implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerInputEvent event) {
        if (!event.hasPressedShiftKey()) return;
        SneakAbilities.onSneak((SkyBlockPlayer) event.getPlayer());
    }
}
