package net.swofty.type.skyblockgeneric.auction;

import net.swofty.type.generic.data.datapoints.DatapointDouble;
import org.jetbrains.annotations.Nullable;

public final class AuctionCoinReservation {
    private final DatapointDouble purse;
    private final long amount;
    private boolean refunded;
    private boolean submitted;

    private AuctionCoinReservation(DatapointDouble purse, long amount) {
        this.purse = purse;
        this.amount = amount;
    }

    public static @Nullable AuctionCoinReservation reserve(DatapointDouble purse, long amount) {
        synchronized (purse) {
            double balance = purse.getValue();
            if (amount <= 0 || !Double.isFinite(balance) || balance < amount) return null;
            purse.setValue(balance - amount);
            return new AuctionCoinReservation(purse, amount);
        }
    }

    public void markSubmitted() {
        synchronized (purse) { submitted = true; }
    }

    public boolean refundBeforeSubmission() {
        synchronized (purse) {
            if (submitted) return false;
            refund();
            return true;
        }
    }

    public void refund() {
        synchronized (purse) {
            if (refunded) return;
            purse.setValue(purse.getValue() + amount);
            refunded = true;
        }
    }
}
