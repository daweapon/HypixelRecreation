package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerInputEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.armor.SneakAbilities;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionSneakArmorAbilities implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerInputEvent event) {
        if (!event.hasPressedShiftKey()) return;
        SneakAbilities.onSneak((SkyBlockPlayer) event.getPlayer());
    }
}
