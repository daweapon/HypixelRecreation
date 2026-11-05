package dev.daweapon.type.catacombs.blessing;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.blessing.BlessingSet;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlessingMessagesTest {
    @Test
    void powerTwoMatchesTheLineSeenInPlay() {
        assertEquals(List.of(
                "     <7>Granted you <a>+8 <7>& <a>+1.04x <c>❁ Strength<7>.",
                "     <7>Also granted you <a>+8 <7>& <a>+1.04x <9>☠ Crit Damage<7>."),
                BlessingMessages.granted(BlessingType.POWER, 2, 1.0));
    }

    @Test
    void wisdomAndStoneFiveMatchTheLinesSeenInPlay() {
        assertEquals("     <7>Granted you <a>+20 <7>& <a>+1.1x <b>✎ Intelligence <7>and <a>+20 <f>✦ Speed<7>.",
                BlessingMessages.granted(BlessingType.WISDOM, 5, 1.0).getFirst());
        assertEquals("     <7>Granted you <a>+20 <7>& <a>+1.1x <a>❈ Defense <7>and <a>+30 <c>Damage<7>.",
                BlessingMessages.granted(BlessingType.STONE, 5, 1.0).getFirst());
        assertEquals("     <7>Granted you <a>+1.15x <c>HP <7>and <a>+1.15x <c>❣ Health Regen<7>.",
                BlessingMessages.granted(BlessingType.LIFE, 5, 1.0).getFirst());
    }

    @Test
    void headerNamesTheBlessingWithItsRomanLevel() {
        assertEquals("<6><l>DUNGEON BUFF!</l> <f>You found a <d>Blessing of Wisdom V<f>!", BlessingMessages.foundByYou(BlessingType.WISDOM, 5));
        assertEquals("<6><l>DUNGEON BUFF!</l> <f>A <d>Blessing of Time V <f>was found!", BlessingMessages.foundInRoom(BlessingType.TIME, 5));
    }

    @Test
    void stackedBlessingsBecomeFlatPlusMultiplier() {
        BlessingSet set = new BlessingSet();
        set.add(BlessingType.POWER, 2);
        set.add(BlessingType.POWER, 5);
        ItemStatistics stats = BlessingStats.of(set, 1.0);
        assertEquals(28, stats.getBase(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(1.14, stats.getMultiplicative(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(28, stats.getBase(ItemStatistic.CRITICAL_DAMAGE), 1e-9);
    }

    @Test
    void floorThreeAndUpMakesBlessingsStronger() {
        assertEquals(1.0, BlessingStats.effectiveness(CatacombsFloor.FLOOR_TWO));
        assertEquals(1.2, BlessingStats.effectiveness(CatacombsFloor.FLOOR_THREE));
        BlessingSet set = new BlessingSet();
        set.add(BlessingType.STONE, 5);
        assertEquals(36, BlessingStats.of(set, 1.2).getBase(ItemStatistic.DAMAGE), 1e-9);
    }
}
