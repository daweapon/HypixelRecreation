package dev.daweapon.type.skyblockgeneric.economy;

import dev.daweapon.commons.economy.CoinFlowStore.Kind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoinFlowCategoriesTest {
    private static final String PACKAGE = "dev.daweapon.type.skyblockgeneric.";

    @Test
    void playerToPlayerChannelsAreTransfers() {
        assertEquals(new CoinFlowCategories.Classified(Kind.TRANSFER, "Bazaar"),
                CoinFlowCategories.classify(PACKAGE + "bazaar.BazaarConnector", -500));
        assertEquals(new CoinFlowCategories.Classified(Kind.TRANSFER, "Auction House"),
                CoinFlowCategories.classify(PACKAGE + "auction.AuctionClaims", 900));
        assertEquals(Kind.TRANSFER, CoinFlowCategories.classify(PACKAGE + "gui.inventories.GUIBankerDeposit$1", -10).kind());
    }

    @Test
    void listingFeesStayASinkEvenWhenRefunded() {
        assertEquals(new CoinFlowCategories.Classified(Kind.SINK, "Auction listing fees"),
                CoinFlowCategories.classify(PACKAGE + "auction.AuctionCreation", 250));
    }

    @Test
    void staffToolsAreKeptApart() {
        assertEquals(Kind.STAFF, CoinFlowCategories.classify(PACKAGE + "staff.MaxStickService", 1_000_000).kind());
        assertEquals(Kind.STAFF, CoinFlowCategories.classify(PACKAGE + "gui.inventories.centauri.GUICentauriCoinGenerator", 5).kind());
    }

    @Test
    void everythingElseIsASourceOrSinkBySign() {
        assertEquals(new CoinFlowCategories.Classified(Kind.SOURCE, "Mob kill coins"),
                CoinFlowCategories.classify(PACKAGE + "item.handlers.CoinDropHandler", 12));
        assertEquals(new CoinFlowCategories.Classified(Kind.SINK, "Reforging"),
                CoinFlowCategories.classify(PACKAGE + "gui.inventories.GUIReforge$2", -2_000));
        assertEquals(new CoinFlowCategories.Classified(Kind.SINK, "Lonely Philosopher"),
                CoinFlowCategories.classify(PACKAGE + "gui.inventories.GUILonelyPhilosopher", -150_000));
        assertEquals(new CoinFlowCategories.Classified(Kind.SOURCE, "Treasure Hunter"),
                CoinFlowCategories.classify(PACKAGE + "npc.TreasureHunter", 100));
    }
}
