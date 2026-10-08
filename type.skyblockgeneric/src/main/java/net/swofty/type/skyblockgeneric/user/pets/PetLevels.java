package net.swofty.type.skyblockgeneric.user.pets;

import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;

/**
 * A pet's level from its stored experience, honouring its own level cap (200 for the dragons),
 * its XP table offset (rarity, or a fixed offset for Bingo) and fixed-level pets.
 */
public final class PetLevels {
    private PetLevels() {
    }

    public static int level(SkyBlockItem pet) {
        PetComponent component = pet.getComponent(PetComponent.class);
        if (component.isFixedLevel()) return component.getMaxLevel();
        return data(pet).getAsLevel(offset(pet), component.getMaxLevel());
    }

    public static int maxLevel(SkyBlockItem pet) {
        return pet.getComponent(PetComponent.class).getMaxLevel();
    }

    public static boolean isMaxLevel(SkyBlockItem pet) {
        return level(pet) >= maxLevel(pet);
    }

    public static int offset(SkyBlockItem pet) {
        return pet.getComponent(PetComponent.class).xpOffset(pet.getAttributeHandler().getRarity());
    }

    public static void setLevel(SkyBlockItem pet, int level) {
        ItemAttributePetData.PetData data = data(pet);
        data.setLevel(level, offset(pet), maxLevel(pet));
        pet.getAttributeHandler().setPetData(data);
    }

    public static boolean addExperience(SkyBlockItem pet, double experience) {
        if (experience <= 0) return false;
        int before = level(pet);
        ItemAttributePetData.PetData data = data(pet);
        data.setExperience(data.getExperience() + experience);
        pet.getAttributeHandler().setPetData(data);
        return level(pet) > before;
    }

    public static double experienceIntoLevel(SkyBlockItem pet) {
        return data(pet).getExperience() - ItemAttributePetData.totalExperienceForLevel(level(pet), offset(pet));
    }

    public static long experienceToNextLevel(SkyBlockItem pet) {
        return ItemAttributePetData.experienceToLevelUp(level(pet), offset(pet));
    }

    public static long totalExperienceForMax(SkyBlockItem pet) {
        return ItemAttributePetData.totalExperienceForLevel(maxLevel(pet), offset(pet));
    }

    private static ItemAttributePetData.PetData data(SkyBlockItem pet) {
        return pet.getAttributeHandler().getPetData();
    }
}
