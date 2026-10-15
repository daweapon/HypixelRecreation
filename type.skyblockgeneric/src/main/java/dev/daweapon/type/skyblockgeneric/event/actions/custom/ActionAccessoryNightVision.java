package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.event.player.PlayerTickEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.accessory.handler.NightVisionCharm;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionAccessoryNightVision implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerTickEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        if (player.getAliveTicks() % NightVisionCharm.REFRESH_TICKS != 0) return;
        if (SkyBlockDataHandler.getUser(player) == null) return;
        NightVisionCharm.refresh(player);
    }
}
