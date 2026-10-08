package net.swofty.type.skyblockgeneric.crimson;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraCollectionTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.CoinOffering;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraTablesTest {

    @Test
    void keysUseTheNeuIdsLavaTreasureAndMinibossesDrop() {
        assertEquals(List.of("KUUDRA_TIER_KEY", "KUUDRA_HOT_TIER_KEY", "KUUDRA_BURNING_TIER_KEY",
                        "KUUDRA_FIERY_TIER_KEY", "KUUDRA_INFERNAL_TIER_KEY"),
                Arrays.stream(KuudraTier.values()).map(KuudraTier::getKeyItemId).toList());
        for (KuudraTier tier : KuudraTier.values()) assertNotNull(ItemType.get(tier.getKeyItemId()), tier.name());
    }

    @Test
    void keyPricesFollowTheLatestEmissaryTable() {
        assertEquals(List.of(200_000, 400_000, 750_000, 1_500_000, 3_000_000),
                Arrays.stream(KuudraTier.values()).map(KuudraTier::getKeyCoins).toList());
        assertEquals(List.of(2, 4, 16, 40, 80), Arrays.stream(KuudraTier.values()).map(KuudraTier::getKeyMaterials).toList());
        assertEquals(List.of(0, 1_000, 3_000, 7_000, 12_000),
                Arrays.stream(KuudraTier.values()).map(KuudraTier::getRequiredReputation).toList());
        assertEquals(300, Arrays.stream(KuudraTier.values()).mapToInt(KuudraTier::getFirstClearSkyBlockXP).sum());
    }

    @Test
    void collectionTiersMatchTheWiki() {
        assertEquals(List.of(10, 100, 500, 2_000, 5_000),
                Arrays.stream(KuudraCollectionTier.values()).map(KuudraCollectionTier::getRequiredProgress).toList());
        assertEquals(5, KuudraCollectionTier.pointsFor(KuudraTier.INFERNAL));
    }

    @Test
    void helmetDonationStaysInsideTheWikiRange() {
        long earliest = KuudraFollowerRules.helmetDonation("abcdefghijklmnop", Instant.parse("2026-01-01T12:00:00Z"));
        long latest = KuudraFollowerRules.helmetDonation("abc", Instant.parse("2024-12-31T12:00:00Z"));
        assertEquals(13_331_996L + 2 - 58 * 16, earliest);
        assertEquals(13_331_996L + 2L * 366 * 366 - 58 * 3, latest);
        assertTrue(earliest >= 13_331_070L && latest <= 13_599_850L);
    }

    @Test
    void helmetDonationUsesUtcMinusFiveWithoutDaylightSaving() {
        long beforeMidnightEst = KuudraFollowerRules.helmetDonation("Steve", Instant.parse("2026-03-02T04:59:00Z"));
        long afterMidnightEst = KuudraFollowerRules.helmetDonation("Steve", Instant.parse("2026-03-02T05:00:00Z"));
        assertEquals(13_331_996L + 2L * 60 * 60 - 58 * 5, beforeMidnightEst);
        assertEquals(13_331_996L + 2L * 61 * 61 - 58 * 5, afterMidnightEst);
    }

    @Test
    void offeringsParseLikeTheBelieverSign() {
        assertEquals(new CoinOffering(CoinOffering.Result.VALID, 4_202_022), CoinOffering.parse("4,202,022"));
        assertEquals(new CoinOffering(CoinOffering.Result.VALID, 1_000_000), CoinOffering.parse("1m"));
        assertEquals(new CoinOffering(CoinOffering.Result.VALID, 1_500), CoinOffering.parse("1.5k"));
        assertEquals(new CoinOffering(CoinOffering.Result.VALID, 0), CoinOffering.parse("0"));
        assertEquals(CoinOffering.Result.NEGATIVE, CoinOffering.parse("-5").result());
        assertEquals(CoinOffering.Result.UNREADABLE, CoinOffering.parse("lots").result());
        assertEquals(CoinOffering.Result.UNREADABLE, CoinOffering.parse("").result());
        assertEquals(CoinOffering.Result.UNREADABLE, CoinOffering.parse("0.5").result());
    }
}
