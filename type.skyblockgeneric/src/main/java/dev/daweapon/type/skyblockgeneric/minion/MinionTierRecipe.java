package dev.daweapon.type.skyblockgeneric.minion;

import java.util.List;
import java.util.Map;

/**
 * A minion tier whose recipe isn't the standard ring of one material around the previous tier.
 * {@code B} is always the previous tier; {@code minionTiers} adds further minions of the same type
 * at a fixed tier (the Inferno Minion XI needs an Inferno Minion VIII as well as the X).
 */
public record MinionTierRecipe(int tier, List<String> pattern, Map<Character, MinionIngredient> ingredients,
                               Map<Character, Integer> minionTiers) {
}
