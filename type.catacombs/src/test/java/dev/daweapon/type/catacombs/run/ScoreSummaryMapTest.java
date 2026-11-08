package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreSummaryMapTest {
    private static final Path DIRECTORY = Path.of("..", "configuration", "skyblock", "dungeons", "score_map");

    @Test
    void redrawsBothCapturedScoreMapsPixelForPixel() throws Exception {
        Assumptions.assumeTrue(Files.isDirectory(DIRECTORY.resolve("captured")));
        assertSame(ScoreSummaryMap.render(new DungeonScoreBreakdown(96, 57, 100, 11), DIRECTORY), "captured/f1_run1.png");
        assertSame(ScoreSummaryMap.render(new DungeonScoreBreakdown(84, 48, 100, 10), DIRECTORY), "captured/f1_run2.png");
    }

    @Test
    void namesTheBadgeOfEveryRank() {
        assertEquals("S_PLUS", ScoreSummaryMap.badgeName(DungeonScoreRank.S_PLUS));
        assertEquals("A", ScoreSummaryMap.badgeName(DungeonScoreRank.A));
    }

    private static void assertSame(BufferedImage drawn, String captured) throws Exception {
        BufferedImage expected = ImageIO.read(DIRECTORY.resolve(captured).toFile());
        for (int y = 0; y < ScoreSummaryMap.SIZE; y++) {
            for (int x = 0; x < ScoreSummaryMap.SIZE; x++) {
                assertEquals(expected.getRGB(x, y) & 0xFFFFFF, drawn.getRGB(x, y) & 0xFFFFFF, captured + " at " + x + "," + y);
            }
        }
    }
}
