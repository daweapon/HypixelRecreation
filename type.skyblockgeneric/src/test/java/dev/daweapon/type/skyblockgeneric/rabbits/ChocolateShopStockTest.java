package dev.daweapon.type.skyblockgeneric.rabbits;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChocolateShopStockTest {
    @Test
    void stockTableMatchesTheWiki() {
        assertEquals(500, ChocolateShopStock.annualStock("SUPREME_CHOCOLATE_BAR"));
        assertEquals(1, ChocolateShopStock.annualStock("EGGLOCATOR"));
        assertEquals(12, ChocolateShopStock.annualStock("SHARD_RABBIT_MAFIOSO"));
        assertEquals(9, ChocolateShopStock.annualStock("SHARD_RABBIT_CAT"));
        assertEquals(3, ChocolateShopStock.annualStock("DARK_CACAO_TRUFFLE"));
        assertEquals(6, ChocolateShopStock.annualStock("SHARD_RABBIT_NEIGHBOR"));
        assertEquals(4, ChocolateShopStock.annualStock("DYE_CHOCOLATE"));
        assertEquals(2, ChocolateShopStock.annualStock("CHOCO_RABBIT_PERSONALITY"));
        assertEquals(3, ChocolateShopStock.annualStock("SHARD_RABBIT_GODMOTHER"));
        assertEquals(1, ChocolateShopStock.annualStock("STEAMED_CHOCOLATE_FISH"));
        assertEquals(4, ChocolateShopStock.annualStock("HOT_CHOCOLATE_MIXIN"));
        assertEquals(25, ChocolateShopStock.annualStock("CHOCOLATE_FORTUNE"));
    }

    @Test
    void itemsWithoutAStockAreUnlimited() {
        assertFalse(ChocolateShopStock.isLimited("ZORROS_CAPE"));
        assertEquals(Integer.MAX_VALUE, ChocolateShopStock.remaining("ZORROS_CAPE", 1000));
    }

    @Test
    void remainingNeverGoesBelowZero() {
        assertEquals(0, ChocolateShopStock.remaining("EGGLOCATOR", 5));
    }

    @Test
    void purchasesReduceRemainingStock() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.recordShopPurchase("SHARD_RABBIT_CAT", 471);
        data.recordShopPurchase("SHARD_RABBIT_CAT", 471);
        assertEquals(7, data.shopRemaining("SHARD_RABBIT_CAT", 471));
        assertEquals(6, data.shopRemaining("SHARD_RABBIT_NEIGHBOR", 471));
    }

    @Test
    void stockResetsWhenTheYearChanges() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.recordShopPurchase("EGGLOCATOR", 471);
        assertEquals(0, data.shopRemaining("EGGLOCATOR", 471));
        assertEquals(1, data.shopRemaining("EGGLOCATOR", 472));
        data.resetShopStockIfNewYear(472);
        assertTrue(data.getShopPurchases().isEmpty());
        assertEquals(472, data.getShopStockYear());
    }
}
