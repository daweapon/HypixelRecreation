package net.swofty.type.skyblockgeneric.fishing.festival;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.function.ToIntBiFunction;

/**
 * Doug's Fishing Festival perks, bought with Carnival Tokens at the Carnival (wiki: Doug, Events
 * Shop). The festival reads their effects from here; the Carnival's perk shop owns the tiers a
 * profile has bought and plugs them in with {@link #bindTierSource}. Until it does, every profile
 * is treated as tier 0.
 */
@Getter
public enum FishingFestivalPerk {
    FISHING_PRODIGY("Fishing Prodigy", 2, List.of(50, 100, 150, 200, 250)),
    SHARK_SONAR("Shark Sonar", 1, List.of(50, 100, 150, 200, 250)),
    DENTURE_COLLECTOR("Denture Collector", 10, List.of(100, 200, 400)),
    ELASMOBRANCHII_AFFINITY("Elasmobranchii Affinity", 50, List.of(100, 1000)),
    ;

    private static volatile ToIntBiFunction<SkyBlockPlayer, FishingFestivalPerk> tierSource = (player, perk) -> 0;

    private final String displayName;
    private final double bonusPerTier;
    private final List<Integer> carnivalTokenCosts;

    FishingFestivalPerk(String displayName, double bonusPerTier, List<Integer> carnivalTokenCosts) {
        this.displayName = displayName;
        this.bonusPerTier = bonusPerTier;
        this.carnivalTokenCosts = carnivalTokenCosts;
    }

    public static void bindTierSource(ToIntBiFunction<SkyBlockPlayer, FishingFestivalPerk> source) {
        tierSource = source;
    }

    public int maxTier() {
        return carnivalTokenCosts.size();
    }

    public int tier(SkyBlockPlayer player) {
        return Math.clamp(tierSource.applyAsInt(player, this), 0, maxTier());
    }

    public double bonus(SkyBlockPlayer player) {
        return tier(player) * bonusPerTier;
    }
}
