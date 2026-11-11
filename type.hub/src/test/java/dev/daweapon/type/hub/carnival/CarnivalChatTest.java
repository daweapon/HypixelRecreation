package dev.daweapon.type.hub.carnival;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarnivalChatTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "<6><l>Fruit Digging|31",
            "<3><l>Catch a Fish|31",
            "<c><l>Zombie Shootout|29",
            "<f>Your Score|33",
            "<a>2,600|37",
            "<f>Fruits Collected|30",
            "<f>Fish Caught|33",
            "<f>Zombies Slain|32",
            "<a>40x <r><e>Leather Zombies <7>- 1,440 points|18",
            "<a>13x <r><f>Iron Zombies <7>- 770 points|21",
            "<a>17x <r><6>Gold Zombies <7>- 1,600 points|21",
            "<a>9x <r><b>Diamond Zombies <7>- 1,248 points|19",
            "<c><l>BONUS! <c>11x Redstone Lamps <7>- 1,100 points|13",
            "<f>Total Kills: <a>79|32",
            "You earned <e>26 Carnival Tokens<7>!|20",
    })
    void centresLikeTheCapture(String markup, int spaces) {
        assertTrue(Math.abs(spaces - CarnivalChat.spacesFor(markup)) <= 1, markup + " gave " + CarnivalChat.spacesFor(markup));
    }

    @Test
    void wrapsTheFruitListWhereHypixelDoes() {
        List<String> lines = CarnivalChat.centeredWrapped(List.of("<f>4x <a>Apple", "<f>3x <a>Mango", "<f>2x <9>Pomegranate",
                "<f>1x <9>Watermelon", "<f>1x <5>Cherry", "<f>2x <c>Bomb", "<f>3x <e>Rum"), "<f>, ");
        assertEquals(List.of(
                "<f>      <f>4x <a>Apple<f>, <f>3x <a>Mango<f>, <f>2x <9>Pomegranate<f>, <f>1x <9>Watermelon<f>, <f>1x",
                "<f>                         <5>Cherry<f>, <f>2x <c>Bomb<f>, <f>3x <e>Rum"), lines);
    }

    @Test
    void formatsTimeLeftWithHypixelsColours() {
        assertEquals("<f>Time Left: <7><a>4m 59s", CarnivalChat.timeLeft(299));
        assertEquals("<f>Time Left: <7><e>1m 0s", CarnivalChat.timeLeft(60));
        assertEquals("<f>Time Left: <7><e>21s", CarnivalChat.timeLeft(21));
        assertEquals("<f>Time Left: <7><c>20s", CarnivalChat.timeLeft(20));
    }
}
