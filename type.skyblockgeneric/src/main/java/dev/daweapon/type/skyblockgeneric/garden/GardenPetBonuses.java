package dev.daweapon.type.skyblockgeneric.garden;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;

public final class GardenPetBonuses {
    private GardenPetBonuses() {
    }

    public static double vinylBoostFraction(SkyBlockPlayer player) {
        return PetAbilities.value(player, ItemType.MOSQUITO_PET, "Smooth Jazz", 0) / 100.0;
    }

    public static double pestDamageMultiplier(SkyBlockPlayer player) {
        return 1 + PetAbilities.value(player, ItemType.HEDGEHOG_PET, "Spiky Quills", 0) / 100.0;
    }

    public static double pestFortune(SkyBlockPlayer player) {
        return PetAbilities.value(player, ItemType.HEDGEHOG_PET, "Fearsome Farmer", 0);
    }

    public static double pestOverbloom(SkyBlockPlayer player) {
        return PetAbilities.value(player, ItemType.HEDGEHOG_PET, "Fearsome Farmer", 1);
    }

    public static double trapCatchSpeedFraction(SkyBlockPlayer player) {
        return Math.min(100, PetAbilities.value(player, ItemType.MOSQUITO_PET, "Bloodsucker's Betrayal", 0)) / 100.0;
    }

    public static double toolXpMultiplier(SkyBlockPlayer player) {
        return 1 + PetAbilities.value(player, ItemType.ORCHID_MANTIS_PET, "Intelligent Specimen", 0) / 100.0;
    }

    public static double copperMultiplier(SkyBlockPlayer player) {
        return 1 + PetAbilities.value(player, ItemType.ROSE_DRAGON_PET, "Spiritual Perfection", 0) / 100.0;
    }
}
