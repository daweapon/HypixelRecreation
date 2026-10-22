package dev.daweapon.type.skyblockgeneric.trapper;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrapperTest {
    private static final File SHOPS = new File("../configuration/skyblock/shops");

    @SuppressWarnings("unchecked")
    private static List<CatalogueEntry> entries(String shop) throws IOException {
        Map<String, Object> root = YamlFileUtils.loadYaml(new File(SHOPS, shop + ".yml"));
        return ((List<Map<String, Object>>) root.get("entries")).stream().map(CatalogueEntry::parse).toList();
    }

    @Test
    void rarityWeightsGiveTheWikiOdds() {
        int total = Stream.of(TrapperRarity.values()).mapToInt(TrapperRarity::getWeight).sum();
        assertEquals(606, total);
        assertEquals(39.6039, 100D * TrapperRarity.TRACKABLE.getWeight() / total, 0.0001);
        assertEquals(0.9901, 100D * TrapperRarity.ELUSIVE.getWeight() / total, 0.0001);
    }

    @Test
    void crestMultipliersFollowItsRarity() {
        assertEquals(110, TrapperRewards.multiplierHundredths(Rarity.COMMON));
        assertEquals(125, TrapperRewards.multiplierHundredths(Rarity.UNCOMMON));
        assertEquals(150, TrapperRewards.multiplierHundredths(Rarity.RARE));
        assertEquals(Rarity.UNCOMMON, TrapperRewards.crestTierFor(100));
        assertEquals(Rarity.RARE, TrapperRewards.crestTierFor(500));
        assertEquals(Rarity.COMMON, TrapperRewards.crestTierFor(99));
    }

    @Test
    void fullyBuffedPeltsMatchTheWikiMaximums() {
        int[] expected = {6, 9, 12, 15, 27};
        for (TrapperRarity rarity : TrapperRarity.values()) {
            int flat = rarity.getPelts() + 2;
            assertEquals(expected[rarity.ordinal()], (flat * 150 + 99) / 100, rarity.name());
        }
    }

    @Test
    void zonesLoad() {
        TrapperConfig config = TrapperConfig.load(new File("../configuration/skyblock/trapper.yml"));
        assertEquals(20_000, config.cooldownMillis());
        assertEquals(600_000, config.timeLimitMillis());
        assertTrue(config.zones().size() >= 5);
    }

    @Test
    void tonySellsFifteenTierTwelveMinionsForSeventyFivePelts() throws IOException {
        List<CatalogueEntry> entries = entries("tony");
        assertEquals(15, entries.size());
        entries.forEach(entry -> {
            assertEquals(75, entry.cost().pelts(), entry.itemId());
            assertEquals(12, entry.minionTier(), entry.itemId());
            assertEquals(Map.of(entry.itemId(), 11), entry.cost().minions(), entry.itemId());
            assertTrue(entry.available(), entry.itemId());
        });
    }

    @Test
    void talbotPricesMatchTheWikiShop() throws IOException {
        List<CatalogueEntry> entries = entries("talbot");
        assertEquals(List.of(100, 80, 250, 340, 200, 250), entries.stream().map(entry -> entry.cost().pelts()).toList());
        entries.forEach(entry -> assertTrue(entry.available(), entry.itemId()));
    }
}
