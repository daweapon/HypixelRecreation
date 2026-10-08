package net.swofty.type.crimsonisle.events;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTestCompletedEvent;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.event.custom.CrimsonMinibossKilledEvent;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;

// Feeds Crimson Isle kills, miniboss kills and Dojo results into the main questlines and the faction side quests.
public class ActionFactionQuestProgress implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        CrimsonQuestlines.onMobKilled(event.getPlayer(), event.getKilledMob());
        CrimsonSideQuests.onMobKilled(event.getPlayer(), event.getKilledMob());
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
