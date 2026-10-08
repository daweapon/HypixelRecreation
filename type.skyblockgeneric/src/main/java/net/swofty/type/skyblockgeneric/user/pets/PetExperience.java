package net.swofty.type.skyblockgeneric.user.pets;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointPetData;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.gifts.ReindeerPet;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumSet;
import java.util.Set;

/**
 * Hypixel's Pet Exp rules: full Skill XP in the pet's own type, a third in any other skill and a
 * quarter for off-type Alchemy and Enchanting, scaled by Taming, Fishing and Diana. Bingo takes
 * every skill at the full rate, Gabagool pets only level by feeding and fixed-level pets never
 * gain XP. Exp Share slots then receive a percentage of what the summoned pet earned, and the
 * summoned pet's earnings also train Taming.
 */
public final class PetExperience {
    private static final Set<SkillCategories> NO_PET_EXP =
            EnumSet.of(SkillCategories.CARPENTRY, SkillCategories.TAMING, SkillCategories.RUNECRAFTING);
    private static final Set<SkillCategories> QUARTER_RATE =
            EnumSet.of(SkillCategories.ALCHEMY, SkillCategories.ENCHANTING);
    private static final double FISHING_MULTIPLIER = 1.5;
    private static final double TAMING_MULTIPLIER_PER_LEVEL = 0.01;
    private static final double TAMING_SHARE_PER_LEVEL = 0.002;
    private static final double WINTER_SPRINT_MULTIPLIER = 2;

    private PetExperience() {
    }

    public static double earned(SkyBlockPlayer player, SkyBlockItem pet, SkillCategories skill, double skillXP) {
        return baseRate(pet.getComponent(PetComponent.class), skill, skillXP)
                * (1 + tamingLevel(player) * TAMING_MULTIPLIER_PER_LEVEL)
                * PetItems.expMultiplier(pet, skill)
                * (ReindeerPet.isReindeer(pet) ? WINTER_SPRINT_MULTIPLIER : 1)
                * ElectionPerks.multiplier(SkyBlockMayor.Perk.PET_XP_BUFF);
    }

    public static double baseRate(PetComponent pet, SkillCategories skill, double skillXP) {
        if (NO_PET_EXP.contains(skill) || skillXP <= 0) return 0;
        if (pet.isFixedLevel() || pet.getPetType() == PetType.GABAGOOL) return 0;

        double xp = skillXP;
        if (!pet.getPetType().takesFullXp(skill)) xp /= QUARTER_RATE.contains(skill) ? 4 : 3;
        if (skill == SkillCategories.FISHING) xp *= FISHING_MULTIPLIER;
        return xp;
    }

    public static double shareRate(SkyBlockPlayer player) {
        double rate = tamingLevel(player) * TAMING_SHARE_PER_LEVEL;
        if (ElectionPerks.isActive(SkyBlockMayor.Perk.SHARING_IS_CARING)) {
            rate += ElectionPerks.bonus(SkyBlockMayor.Perk.SHARING_IS_CARING) / 100D;
        }
        return rate;
    }

    public static boolean isSlotActive(int slot) {
        return slot == 0 || ElectionPerks.isActive(SkyBlockMayor.Perk.SHARING_IS_CARING);
    }

    public static void share(SkyBlockPlayer player, SkyBlockItem summoned, double summonedXP) {
        if (summonedXP <= 0) return;
        double rate = shareRate(player);

        DatapointPetData.UserPetData data = player.getPetData();
        for (int slot = 0; slot < DatapointPetData.EXP_SHARE_SLOTS; slot++) {
            SkyBlockItem pet = data.getExpSharePet(slot);
            if (pet == null || pet == summoned || !isSlotActive(slot)) continue;
            if (pet.getComponent(PetComponent.class).isFixedLevel()) continue;
            double petRate = rate + PetItems.expShareRate(pet);
            if (petRate > 0) PetLevels.addExperience(pet, summonedXP * petRate);
        }
    }

    public static void trainTaming(SkyBlockPlayer player, double petXP) {
        if (petXP <= 0) return;
        player.getSkills().increase(player, SkillCategories.TAMING, petXP);
    }

    private static int tamingLevel(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.TAMING);
    }
}
