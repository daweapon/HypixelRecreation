package net.swofty.commons;

// Enumerates the standalone backend services reachable over Redis RPC (auction house, bazaar, punishment, etc).
public enum ServiceType {
    AUCTION_HOUSE,
    BAZAAR,
    ITEM_TRACKER,
    API,
    PARTY,
    DARK_AUCTION,
    ORCHESTRATOR,
    FRIEND,
    REPLAY,
    PUNISHMENT,
    ELECTION,
    GUILD,
    STORE,
    ;
}
