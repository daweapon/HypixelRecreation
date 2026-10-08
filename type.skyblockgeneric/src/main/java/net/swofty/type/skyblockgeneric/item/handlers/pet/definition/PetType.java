package net.swofty.type.skyblockgeneric.item.handlers.pet.definition;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;

/**
 * A pet's type: the "X Pet" lore line and which skill's XP it levels from at the full rate.
 */
@Getter
public enum PetType {
    COMBAT("Combat", SkillCategories.COMBAT),
    FARMING("Farming", SkillCategories.FARMING),
    FISHING("Fishing", SkillCategories.FISHING),
    MINING("Mining", SkillCategories.MINING),
    FORAGING("Foraging", SkillCategories.FORAGING),
    ENCHANTING("Enchanting", SkillCategories.ENCHANTING),
    ALCHEMY("Alchemy", SkillCategories.ALCHEMY),
    TAMING("Taming", null),
    HUNTING("Hunting", SkillCategories.HUNTING),
    ALL("All Skills", null),
    GABAGOOL("Gabagool", null),
    FRACTURED_SOUL("Fractured Soul", null);

    private final String label;
    private final @Nullable SkillCategories skill;

    PetType(String label, @Nullable SkillCategories skill) {
        this.label = label;
        this.skill = skill;
    }

    public boolean takesFullXp(SkillCategories earned) {
        return this == ALL || this == GABAGOOL || skill == earned;
    }

    public static PetType parse(String name) {
        return valueOf(name.trim().toUpperCase().replace(' ', '_'));
    }
}
