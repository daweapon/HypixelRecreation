package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreScreenTest {
    private static final DungeonScoreBreakdown SCORE = new DungeonScoreBreakdown(100, 42, 100, 0);

    private static String pad(int spaces) {
        return " ".repeat(spaces);
    }

    @Test
    void wonScreenMatchesTheCapturedLines() {
        Map<String, Long> classes = new LinkedHashMap<>();
        classes.put("Archer", 149L);
        List<String> lines = ScoreScreen.won(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, SCORE, 299,
                new ScoreScreen.Earned(297, classes));
        assertEquals(ScoreScreen.BAR, lines.get(0));
        assertEquals("<f>" + pad(25) + "<c>The Catacombs <8>- <e>Floor I", lines.get(1));
        assertEquals("", lines.get(2));
        assertEquals("<f>" + pad(28) + "<f>Team Score: <a>242 <f>(<a>A<f>)", lines.get(3));
        assertEquals("<f>" + pad(22) + "<c>☠ <e>Defeated <c>Bonzo <e>in <a>04m 59s", lines.get(4));
        assertTrue(lines.get(5).startsWith("<f>" + pad(29)));
        assertTrue(lines.get(5).contains("<click:run:'/showextrastats'>") && lines.get(5).contains("EXTRA STATS"));
        assertEquals("<f>" + pad(22) + "<8>+<3>297.0 Catacombs Experience", lines.get(6));
        assertEquals("<f>" + pad(24) + "<8>+<3>149.0 Archer Experience", lines.get(7));
        assertEquals(ScoreScreen.BAR, lines.get(8));
        assertEquals(64, ScoreScreen.BAR.chars().filter(c -> c == '▬').count());
    }

    @Test
    void extraStatsMatchTheCapturedLines() {
        List<String> lines = ScoreScreen.extraStats(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, SCORE, 299, true,
                new ScoreScreen.Stats("Archer", 4_759_909, 0, 157, 0, 0));
        assertEquals("<f>" + pad(21) + "<c>The Catacombs <8>- <e>Floor I Stats", lines.get(1));
        assertEquals("", lines.get(5));
        assertEquals("<f>" + pad(19) + "<f>Total Damage as Archer: <a>4,759,909", lines.get(6));
        assertTrue(lines.get(7).endsWith(" <f>Enemies Killed: <a>157"), "Hypixel pads this one 30, the font table gives 29");
        assertTrue(lines.get(7).startsWith("<f>" + pad(29)));
        assertEquals("<f>" + pad(35) + "<f>Deaths: <c>0", lines.get(8));
        assertEquals("<f>" + pad(30) + "<f>Secrets Found: <b>0", lines.get(9));
        assertEquals(12, lines.size(), "no Ally Healing line without healing");
        List<String> healed = ScoreScreen.extraStats(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, SCORE, 497, true,
                new ScoreScreen.Stats("Archer", 2_594_025, 1_040, 65, 0, 0));
        assertTrue(healed.get(7).endsWith(" <f>Ally Healing: <a>1,040"), "captured with 29 spaces; the font table is one off here");
    }

    @Test
    void requeuePromptRunsTheCommand() {
        List<String> lines = ScoreScreen.requeue(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL);
        assertEquals(5, lines.size());
        assertEquals("", lines.getFirst());
        assertEquals(ScoreScreen.BAR, lines.get(1));
        assertTrue(lines.get(2).startsWith("      <hover:"));
        assertTrue(lines.get(2).contains("<click:run:'/instancerequeue'><7>Click <e><l>HERE</l> <7>to re-queue into <a>The Catacombs<7>!"));
        assertTrue(lines.get(2).contains("Floor I"));
    }
}
