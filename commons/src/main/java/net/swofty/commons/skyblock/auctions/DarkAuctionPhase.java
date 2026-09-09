package net.swofty.commons.skyblock.auctions;

// Enumerates the lifecycle phases of a single Dark Auction round.
public enum DarkAuctionPhase {
    QUEUE,      // 35 seconds to enter
    BIDDING,    // Active bidding
    BETWEEN,    // Between rounds
    COMPLETE    // Done
}
