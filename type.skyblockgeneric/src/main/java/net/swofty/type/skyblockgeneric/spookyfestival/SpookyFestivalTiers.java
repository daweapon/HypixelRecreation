package net.swofty.type.skyblockgeneric.spookyfestival;

import java.util.List;

// Fear Mongerer candy-leaderboard reward tiers (Roadmap #19). Real Hypixel (SkyBlock Wiki,
// "Spooky Festival" - confirmed against the live page, not the earlier rough-guess pass): points
// are 1 per Green Candy / 5 per Purple Candy. Exact thresholds and reward bundles below are
// wiki-sourced, not guessed. Top 50 is placement-based (no fixed point floor), not a point
// threshold like the other 6.
public final class SpookyFestivalTiers {

    public record Tier(
            String name,
            int threshold,
            boolean rankBased,
            long coins,
            int carnivalTickets,
            boolean spookyPie,
            boolean spookyDisc,
            boolean vampirismBook,
            boolean witchMask,
            boolean skeletonHorsePet,
            boolean goldenTreat
    ) {
    }

    public static final Tier[] TIERS = {
            new Tier("Wood", 100, false, 25_000, 5,
                    true, false, false, false, false, false),
            new Tier("Stone", 1000, false, 50_000, 10,
                    true, true, false, false, false, false),
            new Tier("Iron", 1500, false, 100_000, 15,
                    true, true, true, false, false, false),
            new Tier("Gold", 1750, false, 200_000, 20,
                    true, true, true, false, false, false),
            new Tier("Diamond", 2000, false, 500_000, 25,
                    true, true, true, true, false, false),
            new Tier("Emerald", 5000, false, 1_000_000, 35,
                    true, true, true, true, true, false),
            new Tier("Top 50", 0, true, 1_000_000, 50,
                    true, true, true, true, true, true),
    };

    private SpookyFestivalTiers() {
    }
}
