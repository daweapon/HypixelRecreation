package net.swofty.type.jerrysworkshop.present;

import net.minestom.server.coordinate.BlockVec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Where the 20 White Gifts hide each SkyBlock year.
 *
 * Hypixel moves the gifts between years while keeping them in the same areas - behind lampposts,
 * in snow and on rooftops - so this is a pool of every spot recorded by players across years, and
 * each year draws 20 of them. The draw is seeded by the year so every server and every restart
 * agrees on the same set, and a player's found-gift indices stay valid for the whole year.
 */
public final class WhiteGiftLocations {
    public static final int GIFTS_PER_YEAR = 20;

    static final List<BlockVec> POOL = List.of(
            new BlockVec(-1, 76, 81),
            new BlockVec(-9, 76, 104),
            new BlockVec(-1, 79, 113),
            new BlockVec(-34, 76, 81),
            new BlockVec(-25, 76, 69),
            new BlockVec(16, 95, 96),
            new BlockVec(26, 86, 43),
            new BlockVec(-17, 82, 30),
            new BlockVec(-12, 76, 9),
            new BlockVec(9, 77, 9),
            new BlockVec(6, 76, 81),
            new BlockVec(1, 76, 99),
            new BlockVec(-13, 78, 105),
            new BlockVec(-22, 85, 99),
            new BlockVec(-22, 77, 56),
            new BlockVec(-34, 84, 74),
            new BlockVec(10, 76, 70),
            new BlockVec(19, 86, 71),
            new BlockVec(23, 87, 27),
            new BlockVec(21, 77, 34),
            new BlockVec(-18, 93, 20),
            new BlockVec(-26, 78, 42),
            new BlockVec(19, 78, 49),
            new BlockVec(-28, 77, 92),
            new BlockVec(-17, 87, 49),
            new BlockVec(1, 78, 111),
            new BlockVec(-7, 76, 101),
            new BlockVec(-20, 76, 95),
            new BlockVec(3, 83, 101),
            new BlockVec(-11, 86, 97),
            new BlockVec(18, 76, 91),
            new BlockVec(17, 116, 96),
            new BlockVec(7, 77, 10),
            new BlockVec(13, 76, 26),
            new BlockVec(29, 76, 50),
            new BlockVec(25, 90, 35),
            new BlockVec(11, 76, 42),
            new BlockVec(26, 76, 54)
    );

    private WhiteGiftLocations() {
    }

    /** Indices into {@link #POOL} of this year's gifts, in a stable order. */
    public static List<Integer> forYear(int year) {
        List<Integer> indices = new ArrayList<>(IntStream.range(0, POOL.size()).boxed().toList());
        Collections.shuffle(indices, new Random(year * 31L + 0x5EA50F1EL));
        List<Integer> chosen = new ArrayList<>(indices.subList(0, GIFTS_PER_YEAR));
        Collections.sort(chosen);
        return chosen;
    }

    public static BlockVec position(int poolIndex) {
        return POOL.get(poolIndex);
    }
}
