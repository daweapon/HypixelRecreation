package net.swofty.type.skyblockgeneric.slayer;

import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A slayer miniboss that may appear while a quest of one of its tiers is in progress. A "stronger"
 * miniboss replaces the regular one of the same tier on a separate roll.
 */
public record SlayerMiniboss(
    String id,
    String name,
    Set<SlayerTier> tiers,
    boolean stronger,
    int level,
    double health,
    double damage,
    int combatXp,
    @Nullable ItemType token,
    int tokenAmount
) {
}
