package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BazaarTooltipsTest {
    @Test
    void pricesBelowTenThousandShowOneDecimal() {
        assertEquals("5,148.8", BazaarTooltips.coins(5148.8));
        assertEquals("4,995.1", BazaarTooltips.coins(4995.1));
        assertEquals("0.2", BazaarTooltips.coins(0.2));
        assertEquals("7,497.7", BazaarTooltips.coins(7497.7));
        assertEquals("1,000.3", BazaarTooltips.coins(1000.3));
    }

    @Test
    void pricesFromTenThousandShowWholeCoins() {
        assertEquals("18,997", BazaarTooltips.coins(18997));
        assertEquals("888,888", BazaarTooltips.coins(888888));
        assertEquals("4,733,324", BazaarTooltips.coins(4733324.4));
        assertEquals("74,841,496", BazaarTooltips.coins(74841496));
    }

    @Test
    void historyAmountsUseKAndM() {
        assertEquals("3.9M", BazaarMenuButtons.shortCoins(3_900_000));
        assertEquals("152.8k", BazaarMenuButtons.shortCoins(152_800));
        assertEquals("12.5M", BazaarMenuButtons.shortCoins(12_500_000));
    }
}
