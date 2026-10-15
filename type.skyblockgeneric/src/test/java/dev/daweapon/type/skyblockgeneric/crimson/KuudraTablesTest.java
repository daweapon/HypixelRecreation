package dev.daweapon.type.skyblockgeneric.crimson;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraCollectionTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.CoinOffering;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    @SuppressWarnings("unchecked")
    void emissaryKeyEntriesMatchTheTierTable() throws Exception {
        Map<String, String> materials = Map.of("mage", "ENCHANTED_MYCELIUM", "barbarian", "ENCHANTED_RED_SAND");
        for (Map.Entry<String, String> faction : materials.entrySet()) {
            Map<String, Object> shop = YamlFileUtils.loadYaml(
                    new File("../configuration/skyblock/shops/crimson_" + faction.getKey() + "_emissary.yml"));
            List<CatalogueEntry> entries = ((List<Map<String, Object>>) shop.get("entries")).stream()
                    .map(CatalogueEntry::parse).filter(entry -> entry.itemId().startsWith("KUUDRA_")).toList();
            assertEquals(KuudraTier.values().length, entries.size());
            for (KuudraTier tier : KuudraTier.values()) {
                CatalogueEntry entry = entries.get(tier.ordinal());
                assertEquals(tier.getKeyItemId(), entry.itemId());
                assertEquals(tier.getKeyCoins(), (int) entry.cost().coins());
                assertEquals(tier.getKeyMaterials(), entry.cost().items().get(faction.getValue()));
                assertEquals(KuudraTier.KEY_NETHER_STARS, entry.cost().items().get("CORRUPTED_NETHER_STAR"));
                assertEquals(tier.getRequiredReputation(), entry.requirement() == null ? 0 : entry.requirement().reputation());
            }
        }
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
