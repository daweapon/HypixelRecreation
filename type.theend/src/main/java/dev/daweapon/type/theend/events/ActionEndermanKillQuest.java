package dev.daweapon.type.theend.events;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.theend.npcs.LoneAdventurerProgress;

public class ActionEndermanKillQuest implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        if (event.getKilledMob() instanceof BestiaryMob mob && mob.getMobID().startsWith("ENDERMAN")) {
            LoneAdventurerProgress.recordEndermanKill(event.getPlayer());
        }
    }
}
