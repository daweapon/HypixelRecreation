package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.event.custom.CalenderHourlyUpdateEvent;

// Syncs every loaded player's world time to the SkyBlock calendar's hourly clock
public class ActionChangeTimeCalender implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CalenderHourlyUpdateEvent event) {
        int hour = event.hour();
        long minecraftTime = dev.daweapon.type.skyblockgeneric.dante.DanteSunlight.worldTime(hour);

        SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
            player.getInstance().setTime(minecraftTime);
        });
    }
}
