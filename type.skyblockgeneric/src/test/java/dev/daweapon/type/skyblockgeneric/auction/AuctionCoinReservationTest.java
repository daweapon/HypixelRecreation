package dev.daweapon.type.skyblockgeneric.auction;

import dev.daweapon.type.generic.data.datapoints.DatapointDouble;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class AuctionCoinReservationTest {
    @Test
    void uncertainSubmittedWriteCannotBeAutomaticallyRefunded() {
        var purse=new DatapointDouble("coins",1000D);
        var reservation=AuctionCoinReservation.reserve(purse,100);
        reservation.markSubmitted();assertFalse(reservation.refundBeforeSubmission());assertEquals(900D,purse.getValue());
        reservation.refund();assertEquals(1000D,purse.getValue());
    }
    @Test
    void failureBeforeSubmissionCanRefundOnce() {
        var purse=new DatapointDouble("coins",1000D);
        var reservation=AuctionCoinReservation.reserve(purse,100);
        assertTrue(reservation.refundBeforeSubmission());reservation.refundBeforeSubmission();assertEquals(1000D,purse.getValue());
    }
    @Test
    void rejectedPurchaseReturnsOnlyItsReservedCoinsOnce() {
        var purse = new DatapointDouble("coins", 1000D);
        var reservation = AuctionCoinReservation.reserve(purse, 100);
        assertNotNull(reservation);
        assertEquals(900D, purse.getValue());
        reservation.refund();
        reservation.refund();
        assertEquals(1000D, purse.getValue());
    }

    @Test
    void refundPreservesInterveningBalanceChanges() {
        var purse = new DatapointDouble("coins", 1000D);
        var reservation = AuctionCoinReservation.reserve(purse, 100);
        assertNotNull(reservation);
        purse.setValue(purse.getValue() + 75);
        reservation.refund();
        assertEquals(1075D, purse.getValue());
    }

    @Test
    void invalidAndUnaffordableReservationsDoNotChangePurse() {
        var purse = new DatapointDouble("coins", 100D);
        for (long amount : new long[]{-100, 0, 101}) assertNull(AuctionCoinReservation.reserve(purse, amount));
        assertEquals(100D, purse.getValue());
    }

    @Test
    void concurrentReservationsCannotSpendTheSameBalance() {
        var purse = new DatapointDouble("coins", 100D);
        var futures = new ArrayList<CompletableFuture<AuctionCoinReservation>>();
        for (int i = 0; i < 20; i++) futures.add(CompletableFuture.supplyAsync(() -> AuctionCoinReservation.reserve(purse, 100)));
        var reservations = futures.stream().map(CompletableFuture::join).filter(java.util.Objects::nonNull).toList();
        assertEquals(1, reservations.size());
        assertEquals(0D, purse.getValue());
        reservations.getFirst().refund();
        assertEquals(100D, purse.getValue());
    }
}
