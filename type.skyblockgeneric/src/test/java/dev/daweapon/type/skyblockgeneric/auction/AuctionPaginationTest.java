package dev.daweapon.type.skyblockgeneric.auction;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class AuctionPaginationTest {
    @Test void browserCanReachEveryListingBeyondTheFirstTwentyFour() {
        var pages = new AuctionPages<Integer>(24);
        var listings = listings(49);
        assertTrue(pages.accept(pages.request(), listings));
        var visited = new ArrayList<Integer>();
        for (int page = 1; page <= 3; page++) {
            var current = pages.current();
            assertEquals(page, current.number());
            assertEquals(3, current.count());
            assertEquals(page == 3 ? 1 : 24, current.items().size());
            assertEquals(page > 1, current.hasPrevious());
            assertEquals(page < 3, current.hasNext());
            visited.addAll(current.items());
            pages.move(1);
        }
        assertEquals(listings, visited);
        assertEquals(3, pages.current().number());
        pages.move(-1);
        assertEquals(listings.subList(24, 48), pages.current().items());
    }

    @Test void ownedAuctionsAndBidsCanReachPartialLastPagesAndNavigateBack() {
        var pages = new AuctionPages<Integer>(7);
        var listings = listings(15);
        pages.accept(pages.request(), listings);
        assertEquals(listings.subList(0, 7), pages.current().items());
        pages.move(1);
        assertEquals(listings.subList(7, 14), pages.current().items());
        pages.move(1);
        assertEquals(List.of(14), pages.current().items());
        pages.move(-1);
        assertEquals(listings.subList(7, 14), pages.current().items());
        pages.move(-100);
        assertEquals(1, pages.current().number());
        pages.move(Integer.MAX_VALUE);
        assertEquals(3, pages.current().number());
    }

    @Test void collectingOrRemovingListingsClampsThePageWithoutLeavingStaleItems() {
        var pages = new AuctionPages<Integer>(7);
        pages.accept(pages.request(), listings(15));
        pages.move(2);
        pages.accept(pages.request(), listings(8));
        assertEquals(2, pages.current().number());
        assertEquals(List.of(7), pages.current().items());
        pages.accept(pages.request(), List.of());
        assertEquals(1, pages.current().number());
        assertEquals(1, pages.current().count());
        assertEquals(0, pages.current().total());
        assertTrue(pages.current().items().isEmpty());
        assertFalse(pages.current().hasPrevious());
        assertFalse(pages.current().hasNext());
    }

    @Test void changingSelectionResetsThePageAndRejectsResponsesFromTheOldQuery() {
        var pages = new AuctionPages<Integer>(24);
        pages.accept(pages.request(), listings(50));
        pages.move(2);
        var oldRequest = pages.request();
        pages.reset();
        assertEquals(1, pages.current().number());
        assertTrue(pages.current().items().isEmpty());
        var newRequest = pages.request();
        assertTrue(pages.accept(newRequest, List.of(100)));
        assertFalse(pages.accept(oldRequest, listings(50)));
        assertEquals(List.of(100), pages.current().items());
    }

    @Test void newerResponsesCannotBeOverwrittenByLateOrRepeatedResponses() {
        var pages = new AuctionPages<Integer>(7);
        var first = pages.request(); var second = pages.request();
        assertTrue(pages.accept(second, List.of(2)));
        assertFalse(pages.accept(first, List.of(1)));
        assertFalse(pages.accept(second, List.of(99)));
        assertEquals(List.of(2), pages.current().items());
    }

    @Test void inspectingAndReturningKeepsThePageWhileRejectingClosedMenuRequests() {
        var pages = new AuctionPages<Integer>(24);
        pages.accept(pages.request(), listings(50));
        pages.move(1);
        var requestBeforeClose = pages.request();
        pages.invalidate();
        assertEquals(2, pages.current().number());
        assertFalse(pages.accept(requestBeforeClose, List.of()));
        pages.accept(pages.request(), listings(60));
        assertEquals(2, pages.current().number());
        assertEquals(listings(60).subList(24, 48), pages.current().items());
    }

    @Test void responseAndPageSnapshotsCannotBeMutatedAfterAcceptance() {
        var pages = new AuctionPages<Integer>(7);
        var results = new ArrayList<>(listings(8));
        pages.accept(pages.request(), results);
        results.clear();
        var before = pages.current();
        assertEquals(8, before.total());
        assertThrows(UnsupportedOperationException.class, () -> before.items().clear());
        pages.move(1);
        pages.accept(pages.request(), List.of(99));
        assertEquals(listings(7), before.items());
        assertEquals(List.of(99), pages.current().items());
    }

    private static List<Integer> listings(int count) { return IntStream.range(0, count).boxed().toList(); }
}
