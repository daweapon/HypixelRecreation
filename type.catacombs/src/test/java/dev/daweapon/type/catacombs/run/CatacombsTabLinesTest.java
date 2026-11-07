package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.dungeons.catacombs.run.DungeonRoomState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CatacombsTabLinesTest {
    private static CatacombsTabLines.Snapshot snapshot(Map<BlessingType, Integer> blessings) {
        return new CatacombsTabLines.Snapshot(
                List.of(new CatacombsTabLines.Member(152, "#55ffff", "techmaster54", "Berserk", 0, false),
                        new CatacombsTabLines.Member(98, "#55ff55", "josomater", "Mage", 25, true)),
                List.of("josomater"), 37, 1, 3, 24, 2, 9, 6, 65,
                List.of(new CatacombsTabLines.Puzzle("Creeper Beams", DungeonRoomState.COMPLETED),
                        new CatacombsTabLines.Puzzle("Tic Tac Toe", DungeonRoomState.UNDISCOVERED)),
                blessings);
    }

    @Test
    void partyColumnListsEveryMemberWithClassOrDeath() {
        List<String> slots = CatacombsTabLines.slots(snapshot(Map.of()));
        assertEquals(80, slots.size());
        assertEquals("<b><l>Party</l> <f>(2)", slots.get(0));
        assertEquals("<8>[<7>152<8>] <color:#55ffff>techmaster54 <f>(<e>Berserk 0<f>)", slots.get(1));
        assertNull(slots.get(2));
        assertEquals("<8>[<7>98<8>] <color:#55ff55>josomater <f>(<c>DEAD<f>)", slots.get(5));
    }

    @Test
    void statsColumnsShowDiscoveriesRoomsTimeAndPuzzles() {
        List<String> slots = CatacombsTabLines.slots(snapshot(Map.of()));
        assertEquals("<f>Downed: <e>josomater", slots.get(21));
        assertEquals("<f>Time: <e>37s", slots.get(22));
        assertEquals("<f>Team Deaths: <c>1", slots.get(25));
        assertEquals("<b><l>Discoveries: <f>5", slots.get(30));
        assertEquals(" <f>Secrets Found: <b>3", slots.get(31));
        assertEquals(" <f>Crypts: <6>2", slots.get(32));
        assertEquals(" <f>Opened Rooms: <5>9", slots.get(41));
        assertEquals(" <f>Completed Rooms: <5>6", slots.get(42));
        assertEquals(" <f>Secrets Found: <e>12.5%", slots.get(43));
        assertEquals(" <f>Time: <6>1m 5s", slots.get(44));
        assertEquals("<b><l>Puzzles: <f>(2)", slots.get(46));
        assertEquals(" <d>Creeper Beams: <f>[<a>✔<f>]", slots.get(47));
        assertEquals(" <7>???: <f>[<6>✦<f>]", slots.get(48));
    }

    @Test
    void buffsColumnListsBlessingsOrSaysNoneActive() {
        assertEquals("<7>No Buffs active. Find them", CatacombsTabLines.slots(snapshot(Map.of())).get(61));
        List<String> slots = CatacombsTabLines.slots(snapshot(Map.of(BlessingType.POWER, 7, BlessingType.LIFE, 2)));
        assertEquals("<b><l>Dungeon Buffs", slots.get(60));
        assertEquals("<f>Blessing of Life <a>II", slots.get(61));
        assertEquals("<f>Blessing of Power <a>VII", slots.get(62));
    }

    @Test
    void timeAndPercentFormatsMatchHypixel() {
        assertEquals("45s", CatacombsTabLines.time(45));
        assertEquals("1m 5s", CatacombsTabLines.time(65));
        assertEquals("1h 0m 3s", CatacombsTabLines.time(3603));
        assertEquals("0", CatacombsTabLines.secretPercent(0, 0));
        assertEquals("100", CatacombsTabLines.secretPercent(4, 4));
    }

    @Test
    void partyMemberRowsAreFoundByTheirSlot() {
        assertEquals(0, CatacombsTabLines.memberAt(1));
        assertEquals(1, CatacombsTabLines.memberAt(5));
        assertEquals(4, CatacombsTabLines.memberAt(17));
        assertEquals(-1, CatacombsTabLines.memberAt(0));
        assertEquals(-1, CatacombsTabLines.memberAt(2));
        assertEquals(-1, CatacombsTabLines.memberAt(CatacombsTabLines.ROWS + 1));
    }
}
