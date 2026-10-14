package net.swofty.service.darkauction;

import net.swofty.commons.skyblock.auctions.DarkAuctionPhase;
import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DarkAuctionStateTest {
    private DarkAuctionState state;
    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();
    private final UUID carol = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        state = new DarkAuctionState();
    }

    @Test
    void startsInQueueAtStartingBidWithNoBidder() {
        assertEquals(DarkAuctionPhase.QUEUE, state.getPhase());
        assertEquals(DarkAuctionState.STARTING_BID, state.getCurrentBid());
        assertNull(state.getHighestBidderId());
        assertEquals(0, state.getCurrentRound());
    }

    @Test
    void hasFiveRoundsWithFixedBookAndFlowerMinionSlots() {
        List<ItemType> items = state.getRoundItems();
        assertEquals(5, items.size());
        assertEquals(ItemType.ENCHANTED_BOOK, items.get(1));
        assertEquals(ItemType.FLOWER_MINION, items.get(4));
    }

    @Test
    void roundItemListIsImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> state.getRoundItems().add(ItemType.FLOWER_MINION));
    }

    @Test
    void roundItemNamesMatchRoundItems() {
        assertEquals(state.getRoundItems().stream().map(ItemType::name).toList(), state.getRoundItemNames());
    }

    @Test
    void currentItemFollowsCurrentRoundAndIsNullOutOfRange() {
        assertEquals(state.getRoundItems().getFirst(), state.getCurrentItem());
        state.setCurrentRound(4);
        assertEquals(ItemType.FLOWER_MINION, state.getCurrentItem());
        state.setCurrentRound(5);
        assertNull(state.getCurrentItem());
        state.setCurrentRound(-1);
        assertNull(state.getCurrentItem());
    }

    @Test
    void hasMoreRoundsUntilLastRound() {
        for (int round = 0; round < 4; round++) {
            state.setCurrentRound(round);
            assertTrue(state.hasMoreRounds(), "round " + round);
        }
        state.setCurrentRound(4);
        assertFalse(state.hasMoreRounds());
    }

    @Test
    void advanceToNextRoundIncrementsAndResetsBiddingState() {
        state.setCurrentBid(900_000);
        state.setHighestBidderId(alice);
        state.setHighestBidderName("Alice");
        state.setEscrowedAmount(alice, 900_000);
        state.recordBid(alice, "Alice", 900_000);

        state.advanceToNextRound();

        assertEquals(1, state.getCurrentRound());
        assertEquals(DarkAuctionState.STARTING_BID, state.getCurrentBid());
        assertNull(state.getHighestBidderId());
        assertNull(state.getHighestBidderName());
        assertTrue(state.getEscrowedCoins().isEmpty());
        assertTrue(state.getBidHistory().isEmpty());
    }

    @Test
    void queueRefundMovesEscrowIntoPendingRefunds() {
        state.setEscrowedAmount(alice, 100_000);
        state.setEscrowedAmount(bob, 200_000);

        state.queueRefund(alice, 100_000);

        assertFalse(state.getEscrowedCoins().containsKey(alice));
        assertEquals(200_000L, state.getEscrowedCoins().get(bob));
        List<DarkAuctionState.RefundEntry> refunds = state.drainPendingRefunds();
        assertEquals(List.of(new DarkAuctionState.RefundEntry(alice, 100_000)), refunds);
    }

    @Test
    void drainPendingRefundsPreservesOrderAndEmptiesQueue() {
        state.queueRefund(alice, 1);
        state.queueRefund(bob, 2);
        state.queueRefund(carol, 3);

        List<DarkAuctionState.RefundEntry> refunds = state.drainPendingRefunds();

        assertEquals(List.of(alice, bob, carol), refunds.stream().map(DarkAuctionState.RefundEntry::playerId).toList());
        assertTrue(state.drainPendingRefunds().isEmpty());
    }

    @Test
    void clearEscrowForRoundDropsEscrowAndPendingRefunds() {
        state.setEscrowedAmount(alice, 5);
        state.queueRefund(bob, 7);

        state.clearEscrowForRound();

        assertTrue(state.getEscrowedCoins().isEmpty());
        assertTrue(state.drainPendingRefunds().isEmpty());
    }

    @Test
    void findNextHighestBidderSkipsExcludedPlayerAndPrefersMostRecent() {
        state.recordBid(alice, "Alice", 60_000);
        state.recordBid(bob, "Bob", 70_000);
        state.recordBid(carol, "Carol", 80_000);
        state.recordBid(bob, "Bob", 90_000);

        DarkAuctionState.BidEntry next = state.findNextHighestBidder(bob);

        assertEquals(new DarkAuctionState.BidEntry(carol, "Carol", 80_000), next);
    }

    @Test
    void findNextHighestBidderReturnsNullWhenOnlyExcludedPlayerBid() {
        state.recordBid(alice, "Alice", 60_000);
        state.recordBid(alice, "Alice", 70_000);

        assertNull(state.findNextHighestBidder(alice));
        assertNull(new DarkAuctionState().findNextHighestBidder(alice));
    }

    @Test
    void removeBidsFromPlayerOnlyRemovesThatPlayer() {
        state.recordBid(alice, "Alice", 60_000);
        state.recordBid(bob, "Bob", 70_000);
        state.recordBid(alice, "Alice", 80_000);

        state.removeBidsFromPlayer(alice);

        assertEquals(1, state.getBidHistory().size());
        assertEquals(bob, state.getBidHistory().getFirst().playerId());
    }
}
