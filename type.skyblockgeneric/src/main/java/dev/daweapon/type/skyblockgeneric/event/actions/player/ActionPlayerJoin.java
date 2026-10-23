package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;

// Syncs a joining player's world time to the current SkyBlock calendar hour
public class ActionPlayerJoin implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.SPAWN)
    public void onPlayerJoin(PlayerSpawnEvent event) {
        int hour = SkyBlockCalendar.getHour();
        long minecraftTime = dev.daweapon.type.skyblockgeneric.dante.DanteSunlight.worldTime(hour);
        event.getPlayer().getInstance().setTime(minecraftTime);
    }

}
