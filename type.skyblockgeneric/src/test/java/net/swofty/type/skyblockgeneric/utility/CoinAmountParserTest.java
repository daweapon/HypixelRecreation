package net.swofty.type.skyblockgeneric.utility;

import net.swofty.commons.StringUtility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoinAmountParserTest {
    @Test
    void shorthandExpandsToTheFullAmount() {
        assertEquals(5_000, CoinAmountParser.parseLong("5k"));
        assertEquals(60_000_000_000L, CoinAmountParser.parseLong("60b"));
        assertEquals(1_500_000, CoinAmountParser.parseLong("1.5m"));
        assertEquals(2_000_000_000_000L, CoinAmountParser.parseLong("2T"));
        assertEquals(10_000, CoinAmountParser.parseLong("10,000"));
        assertEquals(0.5, CoinAmountParser.parseDouble("0.5"));
        assertEquals(250, CoinAmountParser.parseDouble(" 250 "));
    }

    @Test
    void garbageIsRejected() {
        assertThrows(NumberFormatException.class, () -> CoinAmountParser.parseLong("abc"));
        assertThrows(NumberFormatException.class, () -> CoinAmountParser.parseDouble(""));
        assertThrows(NumberFormatException.class, () -> CoinAmountParser.parseDouble("k"));
        assertTrue(CoinAmountParser.parse("0").isEmpty());
        assertEquals(60_000_000_000L, CoinAmountParser.parse("60b").getAsLong());
    }

    @Test
    void coinAmountsGetCommasAndKeepASingleDecimal() {
        assertEquals("10,000", StringUtility.coinAmount(10000));
        assertEquals("1,234,567.5", StringUtility.coinAmount(1234567.5));
        assertEquals("990,000,000,610", StringUtility.coinAmount(990_000_000_610D));
        assertEquals("12", StringUtility.coinAmount(12));
    }
}
