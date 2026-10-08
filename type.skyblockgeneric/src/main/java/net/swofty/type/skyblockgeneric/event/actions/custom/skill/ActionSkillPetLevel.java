package net.swofty.type.skyblockgeneric.event.actions.custom.skill;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.SkillUpdateEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetEvents;
import net.swofty.type.skyblockgeneric.user.pets.PetExperience;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
import net.swofty.type.skyblockgeneric.user.pets.PetScore;

// Grants Pet Exp to the active pet and its Exp Share pets as the player's skills increase, and
// trains Taming with what the summoned pet earned
public class ActionSkillPetLevel implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(SkillUpdateEvent event) {
        if (event.getNewValueRaw() <= event.getOldValueRaw()) return;

        SkyBlockPlayer player = event.getPlayer();
        double gained = event.getNewValueRaw() - event.getOldValueRaw();
        PetEvents.xpGained(player, PetEvent.XpType.SKILL, null, gained);

        SkyBlockItem enabledPet = player.getPetData().getEnabledPet();
        if (enabledPet == null) return;

        double xp = PetExperience.earned(player, enabledPet, event.getSkillCategory(), gained);
        if (xp <= 0) return;

        boolean hasLeveled = PetLevels.addExperience(enabledPet, xp);
        PetExperience.share(player, enabledPet, xp);
        PetExperience.trainTaming(player, xp);

        if (hasLeveled) {
            PetScore.syncSkyBlockXP(player);
            player.getPetData().refreshCachedAbilities();
            player.sendMessage(
                    "<a>Your {}levelled up to level <9>{}<a>!",
                    Text.of("<color:{}>{} Pet ",
                            enabledPet.getAttributeHandler().getRarity().getColor(),
                            enabledPet.getComponent(PetComponent.class).getPetName()),
                    PetLevels.level(enabledPet)
            );
        }
    }
}
