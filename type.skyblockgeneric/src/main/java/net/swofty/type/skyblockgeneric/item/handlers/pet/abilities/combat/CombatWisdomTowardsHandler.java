package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * "+{0} Combat Wisdom towards" one kind of mob: killing a mob the YAML {@code mobs:} list matches
 * grants the extra Combat XP that much Wisdom would have added to the kill.
 */
public final class CombatWisdomTowardsHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.Kill kill) || kill.mob().getSkillCategory() != SkillCategories.COMBAT) return;
        OtherLoot loot = kill.mob().getOtherLoot();
        if (loot == null || loot.getSkillXPAmount() <= 0) return;
        double extra = loot.getSkillXPAmount() * context.value(0) / 100D
                * ElectionPerks.multiplier(SkyBlockMayor.Perk.MOAR_SKILLZ);
        if (extra <= 0) return;
        SkyBlockPlayer player = kill.player();
        DatapointSkills.PlayerSkills skills = player.getSkills();
        skills.setRaw(player, SkillCategories.COMBAT, skills.getRaw(SkillCategories.COMBAT) + extra);
    }
}
