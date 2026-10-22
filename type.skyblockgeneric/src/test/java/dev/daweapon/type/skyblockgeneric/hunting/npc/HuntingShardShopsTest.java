package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HuntingShardShopsTest {
    private static final File FILE = new File("../configuration/skyblock/hunting/shard_shops.yml");
    private final HuntingShardShops.Shop sanger = HuntingShardShops.load(FILE, "sanger");
    private final HuntingShardShops.Shop kiara = HuntingShardShops.load(FILE, "kiara");

    private static long daysToMax(HuntingShardShops.Offer offer, int stock) {
        AttributeDefinition definition = AttributeRegistry.get(offer.shardId());
        assertNotNull(definition, offer.shardId());
        int needed = definition.rarity().cumulativeForLevel(10);
        return (needed + stock - 1) / stock;
    }

    private static long totalCoinsToMax(HuntingShardShops.Shop shop) {
        return shop.offers().stream()
                .mapToLong(offer -> offer.coins() * AttributeRegistry.get(offer.shardId()).rarity().cumulativeForLevel(10))
                .sum();
    }

    @Test
    void sangerSellsTheFiveElusiveShardsAtTheWikiPrices() {
        assertEquals(java.util.List.of("U70", "U47", "R87", "R40", "E25"), sanger.offers().stream().map(HuntingShardShops.Offer::shardId).toList());
        assertEquals(java.util.List.of(250_000L, 250_000L, 500_000L, 500_000L, 750_000L), sanger.offers().stream().map(HuntingShardShops.Offer::coins).toList());
        assertEquals(java.util.List.of(10, 10, 6, 6, 3), sanger.offers().stream().map(HuntingShardShops.Offer::stock).toList());
    }

    @Test
    void kiaraSellsFourShardsAtTheWikiPrices() {
        assertEquals(java.util.List.of("U21", "R45", "R53", "R61"), kiara.offers().stream().map(HuntingShardShops.Offer::shardId).toList());
        assertEquals(java.util.List.of(100_000L, 300_000L, 350_000L, 600_000L), kiara.offers().stream().map(HuntingShardShops.Offer::coins).toList());
        assertEquals(java.util.List.of(10, 6, 6, 4), kiara.offers().stream().map(HuntingShardShops.Offer::stock).toList());
    }

    @Test
    void sangerDaysAndCostToMaxMatchTheWikiTrivia() {
        assertEquals(java.util.List.of(7L, 7L, 8L, 8L, 11L), sanger.offers().stream().map(offer -> daysToMax(offer, offer.stock())).toList());
        assertEquals(104_000_000L, totalCoinsToMax(sanger));
    }

    @Test
    void kiaraDaysAndCostToMaxMatchTheWikiTriviaWithAndWithoutContact() {
        assertEquals(java.util.List.of(7L, 8L, 8L, 12L), kiara.offers().stream().map(offer -> daysToMax(offer, kiara.stockFor(offer, false))).toList());
        assertEquals(java.util.List.of(6L, 7L, 7L, 10L), kiara.offers().stream().map(offer -> daysToMax(offer, kiara.stockFor(offer, true))).toList());
        assertEquals(66_400_000L, totalCoinsToMax(kiara));
    }

    @Test
    void onlyKiarasContactAddsStock() {
        assertEquals(1, kiara.contactBonus());
        assertEquals(0, sanger.contactBonus());
        assertEquals(11, kiara.stockFor(kiara.offers().getFirst(), true));
        assertEquals(10, kiara.stockFor(kiara.offers().getFirst(), false));
        assertEquals(10, sanger.stockFor(sanger.offers().getFirst(), true));
    }

    @Test
    void everyOfferedShardHasAMatchingSlot() {
        assertEquals(sanger.offers().size(), sanger.slots().length);
        assertEquals(kiara.offers().size(), kiara.slots().length);
    }
}
