package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.SkillUpdateEvent;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetService;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetTrigger;

// Fires the Autopet rules that react to skill XP and collection gains
public class ActionAutopetTriggers implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSkill(SkillUpdateEvent event) {
        if (event.getNewValueRaw() <= event.getOldValueRaw()) return;
        AutopetService.fire(event.getPlayer(), AutopetTrigger.GAIN_SKILL_XP, event.getSkillCategory().name());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onCollection(CollectionUpdateEvent event) {
        AutopetService.fire(event.getPlayer(), AutopetTrigger.GAIN_COLLECTION, event.getItemType().name());
    }
}
