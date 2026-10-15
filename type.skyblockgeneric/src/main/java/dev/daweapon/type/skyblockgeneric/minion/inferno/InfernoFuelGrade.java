package dev.daweapon.type.skyblockgeneric.minion.inferno;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

/**
 * The gabagool tier an Inferno Minion Fuel was crafted with. Hypixel applies the multiplier as
 * {@code baseTime / ((1 + speedBuffs) * (1 + multiplier))}, so 10x/15x/20x act as 11x/16x/21x.
 */
@Getter
public enum InfernoFuelGrade {
    FUEL(10, null),
    HEAVY(15, null),
    HYPERGOLIC(20, ItemType.HYPERGOLIC_IONIZED_CERAMICS);

    private final int speedMultiplier;
    private final @Nullable ItemType residue;

    InfernoFuelGrade(int speedMultiplier, @Nullable ItemType residue) {
        this.speedMultiplier = speedMultiplier;
        this.residue = residue;
    }

    public double actionTimeDivisor() {
        return 1 + speedMultiplier;
    }

    public boolean createsUniqueItems() {
        return this == HYPERGOLIC;
    }
}
