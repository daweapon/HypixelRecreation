package net.swofty.type.skyblockgeneric.auction;

import net.swofty.commons.protocol.objects.auctions.AuctionClaimProtocol.Receipt;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AuctionClaimsTest {
    private final Receipt receipt=new Receipt(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),false,100,"item");
    private record Snapshot(double coins,int items,List<UUID> delivered,List<UUID> active,List<UUID> inactive) {}
    private class Session {
        DatapointDouble coins=new DatapointDouble("coins",1000D);
        int items=0;
        List<UUID> delivered=new ArrayList<>(),active=new ArrayList<>(List.of(receipt.auction(),receipt.auction())),inactive=new ArrayList<>();
        Snapshot snapshot() { return new Snapshot(coins.getValue(),items,List.copyOf(delivered),List.copyOf(active),List.copyOf(inactive)); }
        void reload(Snapshot saved) {
            coins.setValue(saved.coins);items=saved.items;delivered=new ArrayList<>(saved.delivered);
            active=new ArrayList<>(saved.active);inactive=new ArrayList<>(saved.inactive);
        }
        boolean claim(Runnable save) { return AuctionClaims.settle(receipt,coins,delivered,active,inactive,()->items++,save); }
    }
    @Test void duplicateCallbacksGrantOnceAndClearAllTrackingDuplicates() {
        var session=new Session();
        assertTrue(session.claim(()->{}));assertFalse(session.claim(()->fail("duplicate save")));
        assertEquals(1100D,session.coins.getValue());assertEquals(1,session.items);
        assertTrue(session.active.isEmpty());assertEquals(List.of(receipt.auction()),session.inactive);
    }
    @Test void crashBeforeProfileCommitCanRetryTheSameReceipt() {
        var session=new Session();var before=session.snapshot();
        assertThrows(IllegalStateException.class,()->session.claim(()->{throw new IllegalStateException("storage unavailable");}));
        session.reload(before);
        assertTrue(session.claim(()->{}));assertEquals(1100D,session.coins.getValue());assertEquals(1,session.items);
    }
    @Test void lostSaveAcknowledgementAfterCommitDoesNotPayAgainAfterReload() {
        var session=new Session();var saved=new Snapshot[1];
        assertThrows(IllegalStateException.class,()->session.claim(()->{saved[0]=session.snapshot();throw new IllegalStateException("lost ack");}));
        session.reload(saved[0]);assertFalse(session.claim(()->fail("duplicate save")));
        assertEquals(1100D,session.coins.getValue());assertEquals(1,session.items);
    }
    @Test void failedSaveCannotRepeatRewardInSameSession() {
        var session=new Session();
        assertThrows(IllegalStateException.class,()->session.claim(()->{throw new IllegalStateException();}));
        assertFalse(session.claim(()->{}));assertEquals(1,session.items);
    }
    @Test void legacyCompletedClaimAndMissingEligibilityCannotPay() {
        var session=new Session();session.inactive.add(receipt.auction());assertFalse(session.claim(()->{}));
        session.inactive.clear();session.active.clear();assertFalse(session.claim(()->{}));assertEquals(1000D,session.coins.getValue());
    }
}
