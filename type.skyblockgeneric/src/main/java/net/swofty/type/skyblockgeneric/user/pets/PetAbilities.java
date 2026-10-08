package net.swofty.type.skyblockgeneric.user.pets;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointPetData;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Lets a game system honour a {@code kind: TEXT} pet ability: whether one of the player's active
 * pets has it unlocked, and its values at that pet's level.
 */
public final class PetAbilities {
    private PetAbilities() {
    }

    public static boolean has(SkyBlockPlayer player, ItemType pet, String ability) {
        return find(player, pet, ability) != null;
    }

    public static double value(SkyBlockPlayer player, ItemType pet, String ability, int index) {
        DatapointPetData.ActiveAbility active = find(player, pet, ability);
        if (active == null) return 0;
        double[] values = ((ConfiguredPetAbility) active.ability()).valuesFor(active.pet());
        return index < values.length ? values[index] : 0;
    }

    private static DatapointPetData.ActiveAbility find(SkyBlockPlayer player, ItemType pet, String ability) {
        if (player == null || player.getSkyblockDataHandler() == null) return null;
        for (DatapointPetData.ActiveAbility active : player.getPetData().getActiveAbilities()) {
            if (active.pet().getAttributeHandler().getPotentialType() != pet) continue;
            if (active.ability() instanceof ConfiguredPetAbility configured && configured.getName().equals(ability)) {
                return active;
            }
        }
        return null;
    }
}
