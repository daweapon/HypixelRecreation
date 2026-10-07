package net.swofty.type.skyblockgeneric.event.actions.custom.skill;

import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.SkillUpdateEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetExperience;
import net.swofty.type.skyblockgeneric.user.pets.PetScore;

// Grants Pet Exp to the active pet and its Exp Share pets as the player's skills increase
public class ActionSkillPetLevel implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(SkillUpdateEvent event) {
        if (event.getNewValueRaw() <= event.getOldValueRaw()) return;

        SkyBlockPlayer player = event.getPlayer();
        SkyBlockItem enabledPet = player.getPetData().getEnabledPet();
        if (enabledPet == null) return;

        double xp = PetExperience.earned(player, enabledPet, event.getSkillCategory(),
                event.getNewValueRaw() - event.getOldValueRaw());
        if (xp <= 0) return;

        PetComponent pet = enabledPet.getComponent(PetComponent.class);
        ItemAttributePetData.PetData petData = enabledPet.getAttributeHandler().getPetData();
        boolean hasLeveled = petData.addExperience(xp, enabledPet.getAttributeHandler().getRarity());
        PetExperience.share(player, enabledPet, xp);

        if (hasLeveled) {
            PetScore.syncSkyBlockXP(player);
            player.sendMessage(
                    "<a>Your {}levelled up to level <9>{}<a>!",
                    Text.of("<color:{}>{} Pet ",
                            enabledPet.getAttributeHandler().getRarity().getColor(),
                            pet.getPetName()),
                    petData.getAsLevel(enabledPet.getAttributeHandler().getRarity())
            );
        }
    }
}
