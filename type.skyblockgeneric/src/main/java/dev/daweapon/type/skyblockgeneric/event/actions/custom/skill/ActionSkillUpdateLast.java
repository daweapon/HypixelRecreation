package dev.daweapon.type.skyblockgeneric.event.actions.custom.skill;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkillCategory;
import dev.daweapon.type.skyblockgeneric.event.custom.SkillUpdateEvent;

// Tracks the most recently increased skill so it can be shown elsewhere in the UI
public class ActionSkillUpdateLast implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(SkillUpdateEvent event) {
        double oldValue = event.getOldValueRaw();
        double newValue = event.getNewValueRaw();
        double difference = newValue - oldValue;

        if (difference <= 0) return;
        event.getPlayer().getSkyblockDataHandler().get(dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler.Data.LAST_EDITED_SKILL, DatapointSkillCategory.class)
                .setValue(event.getSkillCategory());
    }
}

