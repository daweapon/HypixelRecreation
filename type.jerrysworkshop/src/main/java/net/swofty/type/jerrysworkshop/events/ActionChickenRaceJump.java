package net.swofty.type.jerrysworkshop.events;

import net.minestom.server.event.player.PlayerInputEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.jerrysworkshop.chickenrace.ChickenRace;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionChickenRaceJump implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerInputEvent event) {
        if (event.hasPressedJumpKey() && event.getPlayer() instanceof SkyBlockPlayer player) {
            ChickenRace.jump(player);
        }
    }
}
