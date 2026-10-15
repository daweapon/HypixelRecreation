package dev.daweapon.type.skyblockgeneric.elections.perks;

import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor.Perk;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Perks whose game system this server does not have yet. They still appear in every Mayor and
 * Minister menu, with the reason in their lore. {@code hook} names the single call to add once the
 * system exists; after wiring it, delete the perk's row here.
 */
public final class PerkAvailability {
    private static final String DUNGEONS = "Dungeons aren't in this server yet.";

    private static final Map<Perk, Gap> GAPS = new EnumMap<>(Map.ofEntries(
            missing(Perk.SHOPPING_SPREE, "NPC shops have no daily buy limits in this server yet.",
                    "daily NPC buy limit * ElectionPerks.factor(SHOPPING_SPREE)"),
            partial(Perk.GRAND_FEAST, "The Feast runs all year, but the Grand Bakery and its Kernels aren't in this server yet.",
                    "open the Grand Bakery shop when ElectionPerks.isActive(GRAND_FEAST)"),
            partial(Perk.A_TIME_FOR_GIVING, "Party Chests aren't in this server yet, so only Party Gifts from Winter Sea Creatures are given.",
                    "spawn Party Chests instead of Trick or Treat Chests when ElectionPerks.isActive(A_TIME_FOR_GIVING)"),
            missing(Perk.MARAUDER, DUNGEONS,
                    "dungeon reward chest price * (1 - ElectionPerks.bonus(MARAUDER) / 100)"),
            missing(Perk.EZPZ, DUNGEONS,
                    "dungeon score += ElectionPerks.bonus(EZPZ)"),
            missing(Perk.BENEDICTION, DUNGEONS,
                    "blessing strength * ElectionPerks.multiplier(BENEDICTION)"),
            missing(Perk.BRIBE, "Profile playtime isn't tracked yet, and the bribe is paid by playtime.",
                    "Scorpius pays the playtime-tier bribe when ElectionPerks.isActive(BRIBE) and the player voted for him"),
            missing(Perk.DARKER_AUCTIONS,
                    "The Darker Auction lots (Plasma Nucleus, Hegemony Artifact, book bundles) aren't in this server yet.",
                    "build the 8-round Darker lot list in DarkAuctionState when ElectionPerks.isActive(DARKER_AUCTIONS)"),
            partial(Perk.SWEET_BENEVOLENCE, "Duplicate Hoppity rabbits aren't in this server yet, so only Candy and Gifts are boosted.",
                    "duplicate rabbit chocolate * ElectionPerks.multiplier(SWEET_BENEVOLENCE)")
    ));

    private PerkAvailability() {}

    public static boolean isFullyAvailable(Perk perk) {
        return !GAPS.containsKey(perk);
    }

    public static boolean isMissing(Perk perk) {
        Gap gap = GAPS.get(perk);
        return gap != null && !gap.partial();
    }

    public static Optional<Gap> gap(Perk perk) {
        return Optional.ofNullable(GAPS.get(perk));
    }

    private static Map.Entry<Perk, Gap> missing(Perk perk, String reason, String hook) {
        return Map.entry(perk, new Gap(false, reason, hook));
    }

    private static Map.Entry<Perk, Gap> partial(Perk perk, String reason, String hook) {
        return Map.entry(perk, new Gap(true, reason, hook));
    }

    public record Gap(boolean partial, String reason, String hook) {}
}
