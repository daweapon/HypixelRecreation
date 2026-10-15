package dev.daweapon.type.jerrysworkshop.events;

import net.minestom.server.event.player.PlayerInputEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.jerrysworkshop.chickenrace.ChickenRace;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionChickenRaceJump implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerInputEvent event) {
        if (event.hasPressedJumpKey() && event.getPlayer() instanceof SkyBlockPlayer player) {
            ChickenRace.jump(player);
        }
    }
}
