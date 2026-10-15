package dev.daweapon.type.skyblockgeneric.fishing.resolver;

import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * Sea Creature Weight (wiki: Sea Creatures, Spawn Mechanics): once Sea Creature Chance succeeds, a
 * weighted pool picks the creature in proportion to its weight. The wiki's weight modifiers stack
 * multiplicatively: Common Hook on Common creatures, Whale Bait (and Light Bait by day, Dark Bait
 * by night) on creatures with a base weight under 400, Tracking on Legendary and Mythic creatures,
 * and per-creature multipliers such as the Drake Piper perk.
 */
public final class SeaCreatureWeights {
    public static final double LOW_WEIGHT_THRESHOLD = 400;
    public static final double HOOK_MULTIPLIER = 1.25;
    public static final double BAIT_MULTIPLIER = 1.25;

    private SeaCreatureWeights() {
    }

    public record Candidate(String id, double weight, @Nullable Rarity rarity) {
    }

    public record Modifiers(@Nullable String hookId, @Nullable String baitId, boolean night, double tracking,
                            Map<String, Double> creatureMultipliers) {
        public static Modifiers none() {
            return new Modifiers(null, null, false, 0, Map.of());
        }
    }

    public static double weight(Candidate candidate, Modifiers modifiers) {
        double weight = candidate.weight();
        if ("COMMON_HOOK".equals(modifiers.hookId()) && candidate.rarity() == Rarity.COMMON) {
            weight *= HOOK_MULTIPLIER;
        }
        if (candidate.weight() < LOW_WEIGHT_THRESHOLD && boostsLowWeight(modifiers)) {
            weight *= BAIT_MULTIPLIER;
        }
        if (candidate.rarity() == Rarity.LEGENDARY || candidate.rarity() == Rarity.MYTHIC) {
            weight *= 1 + modifiers.tracking() / 100D;
        }
        return weight * modifiers.creatureMultipliers().getOrDefault(candidate.id(), 1D);
    }

    public static Candidate pick(List<Candidate> eligible, Modifiers modifiers, RandomGenerator random) {
        return LootRoll.pick(null, eligible, candidate -> null, candidate -> weight(candidate, modifiers), random);
    }

    private static boolean boostsLowWeight(Modifiers modifiers) {
        String bait = modifiers.baitId();
        if (bait == null) return false;
        return switch (bait) {
            case "WHALE_BAIT" -> true;
            case "LIGHT_BAIT" -> !modifiers.night();
            case "DARK_BAIT" -> modifiers.night();
            default -> false;
        };
    }
}
