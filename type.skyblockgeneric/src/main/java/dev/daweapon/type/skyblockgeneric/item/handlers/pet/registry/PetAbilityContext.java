package dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry;

import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * What a bespoke pet ability handler sees: the pet, its rarity and level, the ability's YAML
 * values already evaluated at that level, and the owner's per-ability runtime state.
 */
public record PetAbilityContext(SkyBlockItem pet, @Nullable SkyBlockPlayer player, Rarity rarity, int level,
                                double[] values, @Nullable AbilityRuntime runtime) {

    public double value(int index) {
        return index < values.length ? values[index] : 0;
    }
}
