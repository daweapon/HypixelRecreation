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
        assertEquals(24, ChocolateShopStock.annualStock("SHARD_RABBIT_MAFIOSO"));
        assertEquals(18, ChocolateShopStock.annualStock("SHARD_RABBIT_CAT"));
        assertEquals(3, ChocolateShopStock.annualStock("DARK_CACAO_TRUFFLE"));
        assertEquals(12, ChocolateShopStock.annualStock("SHARD_RABBIT_NEIGHBOR"));
        assertEquals(4, ChocolateShopStock.annualStock("DYE_CHOCOLATE"));
        assertEquals(2, ChocolateShopStock.annualStock("CHOCO_RABBIT_PERSONALITY"));
        assertEquals(6, ChocolateShopStock.annualStock("SHARD_RABBIT_GODMOTHER"));
        assertEquals(1, ChocolateShopStock.annualStock("ZORROS_CAPE"));
        assertEquals(1, ChocolateShopStock.annualStock("PET_ITEM_CHOCOLATE_SYRINGE"));
        assertEquals(1, ChocolateShopStock.annualStock("STEAMED_CHOCOLATE_FISH"));
        assertEquals(4, ChocolateShopStock.annualStock("HOT_CHOCOLATE_MIXIN"));
        assertEquals(25, ChocolateShopStock.annualStock("CHOCOLATE_FORTUNE"));
    }

    @Test
    void itemsWithoutAStockAreUnlimited() {
        assertFalse(ChocolateShopStock.isLimited("SOMETHING_UNLISTED"));
        assertEquals(Integer.MAX_VALUE, ChocolateShopStock.remaining("SOMETHING_UNLISTED", 1000));
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
        assertEquals(16, data.shopRemaining("SHARD_RABBIT_CAT", 471));
        assertEquals(12, data.shopRemaining("SHARD_RABBIT_NEIGHBOR", 471));
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
