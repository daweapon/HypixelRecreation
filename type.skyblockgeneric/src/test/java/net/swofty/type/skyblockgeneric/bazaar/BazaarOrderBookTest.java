package net.swofty.type.skyblockgeneric.bazaar;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarOrderBookTest {
    private static BazaarConnector.MarketOrder order(double price, double amount) {
        return new BazaarConnector.MarketOrder(UUID.randomUUID(), UUID.randomUUID(), price, amount);
    }

    @Test
    void sellOffersListLowestPriceFirstAndMergeSamePrice() {
        var levels = BazaarOrderBook.top(List.of(order(3.5, 10), order(3.4, 5), order(3.4, 7), order(3.6, 1)), false);

        assertEquals(3, levels.size());
        assertEquals(3.4, levels.get(0).price(), 1e-9);
        assertEquals(12, levels.get(0).amount());
        assertEquals(2, levels.get(0).orders());
        assertEquals(3.6, levels.get(2).price(), 1e-9);
    }

    @Test
    void buyOrdersListHighestPriceFirst() {
        var levels = BazaarOrderBook.top(List.of(order(3.4, 5), order(3.6, 2), order(3.5, 1)), true);

        assertEquals(3.6, levels.get(0).price(), 1e-9);
        assertEquals(3.4, levels.get(2).price(), 1e-9);
    }

    @Test
    void capsAtSevenLevelsAndSkipsEmptyOrders() {
        var orders = new java.util.ArrayList<BazaarConnector.MarketOrder>();
        for (int i = 0; i < 10; i++) {
            orders.add(order(1 + i * 0.1, 1));
        }
        orders.add(order(0.5, 0));

        var levels = BazaarOrderBook.top(orders, false);

        assertEquals(BazaarOrderBook.TOP_LEVELS, levels.size());
        assertTrue(levels.get(0).price() >= 1.0 - 1e-9);
    }
}
