package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.event.player.PlayerTickEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.accessory.handler.NightVisionCharm;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionAccessoryNightVision implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerTickEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        if (player.getAliveTicks() % NightVisionCharm.REFRESH_TICKS != 0) return;
        if (SkyBlockDataHandler.getUser(player) == null) return;
        NightVisionCharm.refresh(player);
    }
}
