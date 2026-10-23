package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkills;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
                * ElectionPerks.multiplier(SkyBlockMayor.Perk.MOAR_SKILLZ)
                * ElectionPerks.multiplier(SkyBlockMayor.Perk.BLOOD_GODS_BLESSING);
        if (extra <= 0) return;
        SkyBlockPlayer player = kill.player();
        DatapointSkills.PlayerSkills skills = player.getSkills();
        skills.setRaw(player, SkillCategories.COMBAT, skills.getRaw(SkillCategories.COMBAT) + extra);
    }
}
