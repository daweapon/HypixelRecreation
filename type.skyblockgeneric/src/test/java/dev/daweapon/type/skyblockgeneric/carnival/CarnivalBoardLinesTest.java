package dev.daweapon.type.skyblockgeneric.carnival;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarnivalBoardLinesTest {
    @Test
    void boardMatchesTheCapturedLayout() {
        CarnivalLeaderboards.Board board = new CarnivalLeaderboards.Board(20,
                List.of(new CarnivalLeaderboards.BoardRow(1, "meeowwwwwwwwwww", "gold", 9_595)));
        List<String> lines = CarnivalLeaderboards.boardLines(CarnivalGame.CATCH_A_FISH, board, null, "daweapon", "gold");
        assertEquals(14, lines.size());
        assertEquals("<e><l>20th Carnival", lines.get(0));
        assertEquals("<3>Catch a Fish", lines.get(1));
        assertEquals("<e>1. <color:gold>meeowwwwwwwwwww <7>- <e>9,595", lines.get(2));
        assertEquals("<7>...", lines.get(12));
        assertEquals("<8>?. <color:gold>daweapon <7>- <8>N/A", lines.get(13));
    }

    @Test
    void rankedViewerSeesTheirPlace() {
        CarnivalLeaderboards.Board board = new CarnivalLeaderboards.Board(1, List.of());
        List<String> lines = CarnivalLeaderboards.boardLines(CarnivalGame.ZOMBIE_SHOOTOUT, board,
                new CarnivalLeaderboards.BoardRow(42, "daweapon", "aqua", 6_158), "daweapon", "aqua");
        assertEquals("<e><l>1st Carnival", lines.get(0));
        assertEquals("<e>42. <color:aqua>daweapon <7>- <e>6,158", lines.get(13));
    }
}
