package dev.daweapon.service.bazaar;

import dev.daweapon.commons.skyblock.bazaar.BazaarTransaction;
import dev.daweapon.commons.skyblock.bazaar.BuyOrderRefundTransaction;
import dev.daweapon.commons.skyblock.bazaar.OrderExpiredBazaarTransaction;
import dev.daweapon.commons.skyblock.bazaar.SuccessfulBazaarTransaction;
import dev.daweapon.service.bazaar.BazaarMarket.LimitOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BazaarMarketTest {
    private static final String ITEM = "ENCHANTED_DIAMOND";
    private static final double EPS = 1e-9;

    private static final class RecordingStore implements BazaarMarket.OrderStore {
        final Set<UUID> saved = new HashSet<>();
        final Set<UUID> deleted = new HashSet<>();
        final Map<UUID, Double> remaining = new HashMap<>();

        public void saveNew(LimitOrder order, String itemName) { saved.add(order.orderId); }
        public void updateRemaining(UUID orderId, double rem) { remaining.put(orderId, rem); }
        public void delete(UUID orderId) { deleted.add(orderId); }
    }

    private final RecordingStore store = new RecordingStore();
    private final List<BazaarTransaction> published = new ArrayList<>();
    private BazaarMarket market;
    private final UUID buyer = UUID.randomUUID();
    private final UUID buyerProfile = UUID.randomUUID();
    private final UUID seller = UUID.randomUUID();
    private final UUID sellerProfile = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        market = new BazaarMarket(store, published::add, false);
    }

    private List<SuccessfulBazaarTransaction> trades() {
        return published.stream()
                .filter(SuccessfulBazaarTransaction.class::isInstance)
                .map(SuccessfulBazaarTransaction.class::cast)
                .toList();
    }

    private List<BuyOrderRefundTransaction> refunds() {
        return published.stream()
                .filter(BuyOrderRefundTransaction.class::isInstance)
                .map(BuyOrderRefundTransaction.class::cast)
                .toList();
    }

    @Test
    void sellerTaxRateComesFromThePerSellerPolicy() {
        BazaarMarket flipped = new BazaarMarket(store, published::add, false,
                owner -> owner.equals(seller) ? 0.01 : 0.0125);
        flipped.submitSell(ITEM, seller, sellerProfile, 100, 10);
        flipped.submitBuy(ITEM, buyer, buyerProfile, 100, 10);

        assertEquals(1, trades().size());
        assertEquals(10.0, trades().getFirst().taxTaken(), EPS);
    }

    @Test
    void nonCrossingOrdersRestInTheBook() {
        market.submitSell(ITEM, seller, sellerProfile, 100, 5);
        market.submitBuy(ITEM, buyer, buyerProfile, 90, 5);

        assertTrue(published.isEmpty());
        assertEquals(1, market.getBuyOrders(ITEM).size());
        assertEquals(1, market.getSellOrders(ITEM).size());
        assertEquals(2, store.saved.size());
    }

    @Test
    void crossingOrdersTradeAtRestingSellPriceWithTaxAndImprovementRefund() {
        market.submitSell(ITEM, seller, sellerProfile, 100, 4);
        market.submitBuy(ITEM, buyer, buyerProfile, 110, 4);

        assertEquals(1, trades().size());
        SuccessfulBazaarTransaction trade = trades().getFirst();
        assertEquals(buyer, trade.buyer());
        assertEquals(seller, trade.seller());
        assertEquals(100, trade.actualPricePerUnit(), EPS);
        assertEquals(110, trade.originalBuyerBid(), EPS);
        assertEquals(4, trade.quantity(), EPS);
        assertEquals(400 * 0.0125, trade.taxTaken(), EPS);
        assertEquals(40, trade.priceImprovement(), EPS);

        assertEquals(1, refunds().size());
        assertEquals("COMPLETED", refunds().getFirst().reason());
        assertEquals(40, refunds().getFirst().refundAmount(), EPS);

        assertTrue(market.getBuyOrders(ITEM).isEmpty());
        assertTrue(market.getSellOrders(ITEM).isEmpty());
        assertEquals(2, store.deleted.size());
    }

    @Test
    void exactPriceMatchProducesNoRefund() {
        market.submitSell(ITEM, seller, sellerProfile, 100, 2);
        market.submitBuy(ITEM, buyer, buyerProfile, 100, 2);

        assertEquals(1, trades().size());
        assertTrue(refunds().isEmpty());
    }

    @Test
    void partialFillLeavesRemainderOnBuySide() {
        market.submitSell(ITEM, seller, sellerProfile, 100, 5);
        market.submitBuy(ITEM, buyer, buyerProfile, 100, 8);

        assertEquals(5, trades().getFirst().quantity(), EPS);
        assertTrue(market.getSellOrders(ITEM).isEmpty());
        List<LimitOrder> buys = market.getBuyOrders(ITEM);
        assertEquals(1, buys.size());
        assertEquals(3, buys.getFirst().remaining, EPS);
        assertEquals(3, store.remaining.get(buys.getFirst().orderId), EPS);
        assertFalse(store.deleted.contains(buys.getFirst().orderId));
    }
    @Test
    void partialFillLeavesRemainderOnSellSide() {
        market.submitBuy(ITEM, buyer, buyerProfile, 100, 2);
        market.submitSell(ITEM, seller, sellerProfile, 100, 10);

        assertEquals(2, trades().getFirst().quantity(), EPS);
        assertTrue(market.getBuyOrders(ITEM).isEmpty());
        assertEquals(8, market.getSellOrders(ITEM).getFirst().remaining, EPS);
    }

    @Test
    void buyerGetsLowestAskFirst() {
        UUID cheapSeller = UUID.randomUUID();
        market.submitSell(ITEM, seller, sellerProfile, 100, 1);
        market.submitSell(ITEM, cheapSeller, UUID.randomUUID(), 90, 1);
        market.submitBuy(ITEM, buyer, buyerProfile, 100, 1);

        assertEquals(1, trades().size());
        assertEquals(cheapSeller, trades().getFirst().seller());
        assertEquals(90, trades().getFirst().actualPricePerUnit(), EPS);
    }

    @Test
    void sellerGetsHighestBidFirst() {
        UUID highBidder = UUID.randomUUID();
        market.submitBuy(ITEM, buyer, buyerProfile, 95, 1);
        market.submitBuy(ITEM, highBidder, UUID.randomUUID(), 99, 1);
        market.submitSell(ITEM, seller, sellerProfile, 90, 1);

        assertEquals(1, trades().size());
        assertEquals(highBidder, trades().getFirst().buyer());
    }
    @Test
    void oneLargeBuyWalksSeveralSellOrders() {
        market.submitSell(ITEM, seller, sellerProfile, 100, 3);
        market.submitSell(ITEM, UUID.randomUUID(), UUID.randomUUID(), 101, 3);
        market.submitBuy(ITEM, buyer, buyerProfile, 101, 5);

        List<SuccessfulBazaarTransaction> trades = trades();
        assertEquals(2, trades.size());
        assertEquals(3, trades.get(0).quantity(), EPS);
        assertEquals(100, trades.get(0).actualPricePerUnit(), EPS);
        assertEquals(2, trades.get(1).quantity(), EPS);
        assertEquals(101, trades.get(1).actualPricePerUnit(), EPS);
        assertEquals(1, market.getSellOrders(ITEM).size());
        assertEquals(1, market.getSellOrders(ITEM).getFirst().remaining, EPS);
    }

    @Test
    void ownOrdersNeverTradeWithEachOther() {
        market.submitSell(ITEM, buyer, buyerProfile, 100, 5);
        market.submitBuy(ITEM, buyer, buyerProfile, 100, 5);

        assertTrue(trades().isEmpty());
        assertEquals(1, market.getBuyOrders(ITEM).size());
        assertEquals(1, market.getSellOrders(ITEM).size());
    }

    @Test
    void ownSellIsSkippedToReachAnotherSellersOrder() {
        market.submitSell(ITEM, buyer, buyerProfile, 90, 5);
        market.submitSell(ITEM, seller, sellerProfile, 100, 5);
        market.submitBuy(ITEM, buyer, buyerProfile, 100, 5);

        assertEquals(1, trades().size());
        assertEquals(seller, trades().getFirst().seller());
        assertEquals(1, market.getSellOrders(ITEM).size());
        assertEquals(buyer, market.getSellOrders(ITEM).getFirst().owner);
    }

    @Test
    void differentItemsHaveSeparateBooks() {
        market.submitSell("OTHER_ITEM", seller, sellerProfile, 100, 5);
        market.submitBuy(ITEM, buyer, buyerProfile, 200, 5);

        assertTrue(trades().isEmpty());
        assertTrue(market.getSellOrders(ITEM).isEmpty());
        assertTrue(market.getBuyOrders("OTHER_ITEM").isEmpty());
    }

    @Test
    void cancelAfterPriceImprovementRefundsAccumulatedAmount() {
        market.submitSell(ITEM, seller, sellerProfile, 90, 5);
        market.submitBuy(ITEM, buyer, buyerProfile, 100, 10);
        LimitOrder remaining = market.getBuyOrders(ITEM).getFirst();

        market.submitDelete(remaining.orderId, buyer, buyerProfile);

        assertEquals(1, refunds().size());
        assertEquals("CANCELLED", refunds().getFirst().reason());
        assertEquals(50, refunds().getFirst().refundAmount(), EPS);
        assertTrue(market.getBuyOrders(ITEM).isEmpty());
        assertTrue(store.deleted.contains(remaining.orderId));
    }

    @Test
    void cancelWithoutImprovementPublishesNoRefund() {
        market.submitBuy(ITEM, buyer, buyerProfile, 100, 10);
        LimitOrder order = market.getBuyOrders(ITEM).getFirst();

        market.submitDelete(order.orderId, buyer, buyerProfile);

        assertTrue(published.isEmpty());
        assertTrue(market.getBuyOrders(ITEM).isEmpty());
    }

    @Test
    void cancellingSellOrderRemovesItFromBook() {
        market.submitSell(ITEM, seller, sellerProfile, 100, 10);
        LimitOrder order = market.getSellOrders(ITEM).getFirst();

        market.submitDelete(order.orderId, seller, sellerProfile);

        assertTrue(market.getSellOrders(ITEM).isEmpty());
        assertTrue(store.deleted.contains(order.orderId));
    }

    @Test
    void expiryRefundsUnfilledBuyAndReturnsUnfilledSell() {
        Instant old = Instant.now().minus(8, ChronoUnit.DAYS);
        LimitOrder oldBuy = new LimitOrder(UUID.randomUUID(), buyer, buyerProfile, LimitOrder.Side.BUY, 100, 10, old);
        LimitOrder oldSell = new LimitOrder(UUID.randomUUID(), seller, sellerProfile, LimitOrder.Side.SELL, 500, 7, old);
        market.injectLoadedOrder(ITEM, oldBuy);
        market.injectLoadedOrder(ITEM, oldSell);

        market.expireOldOrders();

        assertEquals(1, refunds().size());
        assertEquals("EXPIRED", refunds().getFirst().reason());
        assertEquals(1000, refunds().getFirst().refundAmount(), EPS);
        OrderExpiredBazaarTransaction expired = published.stream()
                .filter(OrderExpiredBazaarTransaction.class::isInstance)
                .map(OrderExpiredBazaarTransaction.class::cast)
                .findFirst().orElseThrow();
        assertEquals(oldSell.orderId, expired.orderId());
        assertEquals("SELL", expired.side());
        assertEquals(7, expired.remainingQty(), EPS);
        assertTrue(market.getBuyOrders(ITEM).isEmpty());
        assertTrue(market.getSellOrders(ITEM).isEmpty());
        assertTrue(store.deleted.containsAll(Set.of(oldBuy.orderId, oldSell.orderId)));
    }

    @Test
    void expiryLeavesRecentOrdersAlone() {
        Instant recent = Instant.now().minus(6, ChronoUnit.DAYS);
        market.injectLoadedOrder(ITEM, new LimitOrder(UUID.randomUUID(), buyer, buyerProfile, LimitOrder.Side.BUY, 100, 10, recent));
        market.submitSell(ITEM, seller, sellerProfile, 500, 5);

        market.expireOldOrders();

        assertTrue(published.isEmpty());
        assertEquals(1, market.getBuyOrders(ITEM).size());
        assertEquals(1, market.getSellOrders(ITEM).size());
    }

    @Test
    void loadedBuyOrderCanBeMatchedAndCompletesLikeFreshOne() {
        LimitOrder loaded = new LimitOrder(UUID.randomUUID(), buyer, buyerProfile, LimitOrder.Side.BUY, 120, 2, Instant.now());
        market.injectLoadedOrder(ITEM, loaded);

        market.submitSell(ITEM, seller, sellerProfile, 100, 2);

        assertEquals(1, trades().size());
        assertEquals(loaded.orderId, trades().getFirst().buyerOrderId());
        assertEquals(40, refunds().getFirst().refundAmount(), EPS);
    }

    @Test
    void unknownItemHasEmptyBooks() {
        assertTrue(market.getBuyOrders("NOPE").isEmpty());
        assertTrue(market.getSellOrders("NOPE").isEmpty());
    }
}
