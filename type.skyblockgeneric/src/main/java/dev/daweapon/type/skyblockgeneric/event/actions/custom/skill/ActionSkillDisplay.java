package dev.daweapon.type.skyblockgeneric.event.actions.custom.skill;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.SkillUpdateEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;

// Shows skill XP gain progress in the action bar when a player's skill value increases
public class ActionSkillDisplay implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(SkillUpdateEvent event) {
        double oldValue = event.getOldValueRaw();
        double newValue = event.getNewValueRaw();
        double difference = newValue - oldValue;

        if (difference <= 0) return;

        int currentLevel = event.getPlayer().getSkills().getCurrentLevel(event.getSkillCategory());
        if (currentLevel >= event.getSkillCategory().asCategory().getHighestLevel()) return;
        int currentRequirement = event.getSkillCategory().asCategory().getReward(currentLevel + 1).requirement();

        SkyBlockActionBar.getFor(event.getPlayer()).addReplacement(
                SkyBlockActionBar.BarSection.DEFENSE,
                Text.of("<3>+{} {} ({}/{})", difference, event.getSkillCategory(),
                        event.getNewValueCumulative(), currentRequirement),
                20,
                2
        );
    }
}
