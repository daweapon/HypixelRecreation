package dev.daweapon.type.skyblockgeneric.mythological;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.Optional;

public final class GriffinPet {
    private GriffinPet() {
    }

    public static Optional<Rarity> equippedRarity(SkyBlockPlayer player) {
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (pet == null || pet.getAttributeHandler().getPotentialType() != ItemType.GRIFFIN_PET) {
            return Optional.empty();
        }
        return Optional.of(pet.getAttributeHandler().getRarity());
    }

    public static boolean isMythic(SkyBlockPlayer player) {
        return equippedRarity(player).map(rarity -> rarity.isAtLeast(Rarity.MYTHIC)).orElse(false);
    }

    public static double ancientEarthTracking(SkyBlockPlayer player) {
        return PetAbilities.value(player, ItemType.GRIFFIN_PET, "Ancient Earth", 0);
    }

    public static double kingOfKingsMagicFind(SkyBlockPlayer player) {
        return PetAbilities.value(player, ItemType.GRIFFIN_PET, "King of Kings", 0);
    }

    public static String coloredName(Rarity rarity) {
        return "<color:" + rarity.getColor().asHexString() + ">" + StringUtility.toNormalCase(rarity.name());
    }
}
