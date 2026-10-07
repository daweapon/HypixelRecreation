package net.swofty.type.skyblockgeneric.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoinAmountParserTest {

    @Test
    void readsPlainAndShortenedAmounts() {
        assertEquals(1_500, CoinAmountParser.parse("1,500").getAsLong());
        assertEquals(250_000, CoinAmountParser.parse("250k").getAsLong());
        assertEquals(1_500_000, CoinAmountParser.parse("1.5m").getAsLong());
        assertEquals(2_000_000_000L, CoinAmountParser.parse("2B").getAsLong());
    }

    @Test
    void rejectsNonsenseAndNonPositiveAmounts() {
        assertTrue(CoinAmountParser.parse("abc").isEmpty());
        assertTrue(CoinAmountParser.parse("").isEmpty());
        assertTrue(CoinAmountParser.parse("0").isEmpty());
        assertTrue(CoinAmountParser.parse("-5k").isEmpty());
    }
}
