package dev.daweapon.type.crimsonisle.events;

import dev.daweapon.type.crimsonisle.faction.DuelistNPC;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTestCompletedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.event.custom.CrimsonMinibossKilledEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;

// Feeds Crimson Isle kills, miniboss kills and Dojo results into the main questlines and the faction side quests.
public class ActionCrimsonQuestlineProgress implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        CrimsonQuestlines.onMobKilled(event.getPlayer(), event.getKilledMob());
        CrimsonSideQuests.onMobKilled(event.getPlayer(), event.getKilledMob());
        DuelistNPC.onKill(event.getPlayer(), event.getKilledMob());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onMiniboss(CrimsonMinibossKilledEvent event) {
        CrimsonQuestlines.onMinibossKilled(event.player(), event.miniboss().name());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onDojo(DojoTestCompletedEvent event) {
        CrimsonQuestlines.onDojoTestCompleted(event.player(), event.test(), event.grade());
    }
}
