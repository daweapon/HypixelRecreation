package dev.daweapon.type.skyblockgeneric.event.actions.pet;

import net.minestom.server.event.player.PlayerTickEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crystalhollows.PrehistoricEggs;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionPrehistoricEggTick implements HypixelEventClass {
    private static final int SCAN_TICKS = 20;

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerTickEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        if (player.getAliveTicks() % SCAN_TICKS != 0) return;
        if (SkyBlockDataHandler.getUser(player) == null) return;
        PrehistoricEggs.tick(player);
    }
}
