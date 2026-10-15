package dev.daweapon.type.skyblockgeneric.user.pets;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetData;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Lets a game system honour a {@code kind: TEXT} pet ability: whether one of the player's active
 * pets has it unlocked, and its values at that pet's level. Also finds the best minion speed boost
 * among the pets of the players on an island.
 */
public final class PetAbilities {
    private PetAbilities() {
    }

    public static boolean has(SkyBlockPlayer player, ItemType pet, String ability) {
        return find(player, pet, ability) != null;
    }

    public static boolean hasAtLeast(SkyBlockPlayer player, ItemType pet, String ability, Rarity rarity) {
        DatapointPetData.ActiveAbility active = find(player, pet, ability);
        return active != null && active.pet().getAttributeHandler().getRarity().isAtLeast(rarity);
    }

    public static double value(SkyBlockPlayer player, ItemType pet, String ability, int index) {
        DatapointPetData.ActiveAbility active = find(player, pet, ability);
        if (active == null) return 0;
        double[] values = ((ConfiguredPetAbility) active.ability()).valuesFor(active.pet());
        return index < values.length ? values[index] : 0;
    }

    public static AbilityRuntime runtime(SkyBlockPlayer player, ItemType pet, String ability) {
        DatapointPetData.ActiveAbility active = find(player, pet, ability);
        return active == null ? null : player.getPetData().getAbilityRuntime(active.ability());
    }

    public static double minionSpeedPercent(Instance island, String minion) {
        if (island == null) return 0;
        double best = 0;
        for (Player viewer : island.getPlayers()) {
            if (!(viewer instanceof SkyBlockPlayer player) || player.getSkyblockDataHandler() == null) continue;
            for (DatapointPetData.ActiveAbility active : player.getPetData().getActiveAbilities()) {
                if (active.ability() instanceof ConfiguredPetAbility configured) {
                    best = Math.max(best, configured.minionSpeedPercent(minion, active.pet(), player));
                }
            }
        }
        return best;
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
