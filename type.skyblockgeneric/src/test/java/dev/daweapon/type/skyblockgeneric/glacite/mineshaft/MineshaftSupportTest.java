package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class MineshaftSupportTest {
    @Test
    void scrapChanceStartsAtOneInTwoHundredAndHalvesPerScrap() {
        assertEquals(0.5, SuspiciousScrap.chancePercent(0), 1e-12);
        assertEquals(0.25, SuspiciousScrap.chancePercent(1), 1e-12);
        assertEquals(0.5 / 8, SuspiciousScrap.chancePercent(3), 1e-12);
    }

    @Test
    void scrapBuffsAreMultiplicative() {
        assertEquals(0.5 * 1.255 * 1.05, SuspiciousScrap.chancePercent(0, 25.5, 5), 1e-12);
    }

    @Test
    void coldGrowsEveryHundredAndFiftySeconds() {
        assertEquals(0, MineshaftCold.vulnerability(149_999), 1e-12);
        assertEquals(0.25, MineshaftCold.vulnerability(150_000), 1e-12);
        assertEquals(0.5, MineshaftCold.vulnerability(300_001), 1e-12);
        assertEquals(2, MineshaftCold.steps(300_001));
        assertEquals(0, MineshaftCold.steps(-5));
    }

    @Test
    void leaveRewardFormula() {
        assertEquals(1_000, MineshaftRewards.leavePowder(0, 0, 0));
        assertEquals(1_000 + 1_000 + 750 + 420, MineshaftRewards.leavePowder(2, 3, 42.9));
    }

    @Test
    void mayhemBuffsAreEquallyLikely() {
        SplittableRandom random = new SplittableRandom(5);
        Map<MayhemBuff, Integer> counts = new EnumMap<>(MayhemBuff.class);
        int rolls = 100_000;
        for (int i = 0; i < rolls; i++) counts.merge(MayhemBuff.roll(null, random), 1, Integer::sum);
        for (MayhemBuff buff : MayhemBuff.values()) assertEquals(0.2, counts.get(buff) / (double) rolls, 0.01, buff.name());
    }

    @Test
    void placeholderIsAHollowCavernInsideTheShaftBox() {
        assertEquals(MineshaftPlaceholderLayout.Tile.WALL, MineshaftPlaceholderLayout.tileAt(MineshaftPlaceholderLayout.MIN_X, 10, -180));
        assertEquals(MineshaftPlaceholderLayout.Tile.WALL, MineshaftPlaceholderLayout.tileAt(-150, MineshaftPlaceholderLayout.MAX_Y, -180));
        assertEquals(MineshaftPlaceholderLayout.Tile.FILL, MineshaftPlaceholderLayout.tileAt(-150, MineshaftPlaceholderLayout.FLOOR_Y, -180));
        assertEquals(MineshaftPlaceholderLayout.Tile.AIR, MineshaftPlaceholderLayout.tileAt(-150, 10, -160));
        assertEquals(MineshaftPlaceholderLayout.Tile.AIR, MineshaftPlaceholderLayout.tileAt(0, 10, 0));
    }

    @Test
    void entranceStandsOnThePlatformAboveALadderShaft() {
        int x = MineshaftPlaceholderLayout.ENTRANCE.blockX();
        int z = MineshaftPlaceholderLayout.ENTRANCE.blockZ();
        int y = MineshaftPlaceholderLayout.ENTRANCE.blockY();
        assertEquals(MineshaftPlaceholderLayout.Tile.PLATFORM, MineshaftPlaceholderLayout.tileAt(x, y - 1, z));
        assertEquals(MineshaftPlaceholderLayout.Tile.AIR, MineshaftPlaceholderLayout.tileAt(x, y, z));
        assertEquals(MineshaftPlaceholderLayout.Tile.AIR, MineshaftPlaceholderLayout.tileAt(x, y + 1, z));
        for (int ladderY = MineshaftPlaceholderLayout.VEIN_Y; ladderY <= MineshaftPlaceholderLayout.PLATFORM_Y; ladderY++) {
            assertEquals(MineshaftPlaceholderLayout.Tile.LADDER,
                    MineshaftPlaceholderLayout.tileAt(MineshaftPlaceholderLayout.LADDER_X, ladderY, MineshaftPlaceholderLayout.LADDER_Z));
            assertEquals(MineshaftPlaceholderLayout.Tile.PILLAR,
                    MineshaftPlaceholderLayout.tileAt(MineshaftPlaceholderLayout.LADDER_WALL_X, ladderY, MineshaftPlaceholderLayout.LADDER_Z));
        }
        assertTrue(MineshaftPlaceholderLayout.EXIT_MARKER.blockX() >= MineshaftPlaceholderLayout.PLATFORM_MIN_X);
    }

    @Test
    void placeholderHasOneVeinOfEachTestBlock() {
        Map<MineshaftPlaceholderLayout.Tile, Integer> counts = new EnumMap<>(MineshaftPlaceholderLayout.Tile.class);
        for (int x = MineshaftPlaceholderLayout.MIN_X; x <= MineshaftPlaceholderLayout.MAX_X; x++) {
            for (int y = MineshaftPlaceholderLayout.VEIN_Y; y < MineshaftPlaceholderLayout.VEIN_Y + 2; y++) {
                for (int z = MineshaftPlaceholderLayout.MIN_Z + 1; z < MineshaftPlaceholderLayout.MAX_Z; z++) {
                    counts.merge(MineshaftPlaceholderLayout.tileAt(x, y, z), 1, Integer::sum);
                }
            }
        }
        int vein = MineshaftPlaceholderLayout.VEIN_SIZE * MineshaftPlaceholderLayout.VEIN_SIZE * 2;
        assertEquals(vein, counts.get(MineshaftPlaceholderLayout.Tile.VEIN_PRIMARY));
        assertEquals(vein, counts.get(MineshaftPlaceholderLayout.Tile.VEIN_TITANIUM));
    }

    @Test
    void worldFilesLiveUnderTheMineshaftDirectory() {
        assertEquals("RUBY_2.polar", MineshaftWorld.worldFile(ShaftType.parseLayout("RUBY_2")).getFileName().toString());
    }
}
