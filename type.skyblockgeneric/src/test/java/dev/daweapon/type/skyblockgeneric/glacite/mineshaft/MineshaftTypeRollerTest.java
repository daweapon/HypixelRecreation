package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class MineshaftTypeRollerTest {
    private static final int ROLLS = 400_000;
    private static final double TOLERANCE = 0.005;

    @Test
    void everyTypeIsOneInSeventeenWithoutAMatch() {
        Map<ShaftType, Double> probabilities = MineshaftTypeRoller.probabilities(null, 0);
        assertEquals(17, probabilities.size());
        probabilities.values().forEach(value -> assertEquals(1.0 / 17, value, 1e-12));
    }

    @Test
    void aMatchingBlockMakesItAThirdAndTheOthersOneInTwentyFour() {
        Map<ShaftType, Double> probabilities = MineshaftTypeRoller.probabilities(ShaftType.RUBY, 0);
        assertEquals(1.0 / 3, probabilities.get(ShaftType.RUBY), 1e-12);
        probabilities.forEach((type, value) -> {
            if (type != ShaftType.RUBY) assertEquals(1.0 / 24, value, 1e-12, type.name());
        });
        assertEquals(1, probabilities.values().stream().mapToDouble(Double::doubleValue).sum(), 1e-12);
    }

    @Test
    void vanguardSeekerRaisesTheFairyWeight() {
        Map<ShaftType, Double> probabilities = MineshaftTypeRoller.probabilities(null, 51);
        assertEquals(0.0862, probabilities.get(ShaftType.FAIR), 0.0001);
        assertEquals(0.0571, probabilities.get(ShaftType.TITA), 0.0001);
        assertEquals(1, probabilities.values().stream().mapToDouble(Double::doubleValue).sum(), 1e-12);
    }

    @Test
    void simulatedTypeRollsMatchTheWeights() {
        SplittableRandom random = new SplittableRandom(11);
        Map<ShaftType, Integer> counts = new EnumMap<>(ShaftType.class);
        for (int i = 0; i < ROLLS; i++) counts.merge(MineshaftTypeRoller.rollType(null, ShaftType.UMBE, 0, random), 1, Integer::sum);
        assertEquals(1.0 / 3, counts.get(ShaftType.UMBE) / (double) ROLLS, TOLERANCE);
        assertEquals(1.0 / 24, counts.get(ShaftType.FAIR) / (double) ROLLS, TOLERANCE);
        assertEquals(1.0 / 24, counts.get(ShaftType.LITT) / (double) ROLLS, TOLERANCE);
    }

    @Test
    void simulatedFairyRollsFollowVanguardSeeker() {
        SplittableRandom random = new SplittableRandom(13);
        int fairy = 0;
        for (int i = 0; i < ROLLS; i++) if (MineshaftTypeRoller.rollType(null, null, 51, random) == ShaftType.FAIR) fairy++;
        assertEquals(0.0862, fairy / (double) ROLLS, TOLERANCE);
    }

    @Test
    void crystalVariantRollsAtTheGivenChance() {
        SplittableRandom random = new SplittableRandom(17);
        int crystal = 0;
        for (int i = 0; i < ROLLS; i++) {
            if (MineshaftTypeRoller.rollLayout(null, ShaftType.RUBY, 25, random).crystal()) crystal++;
        }
        assertEquals(0.25, crystal / (double) ROLLS, TOLERANCE);
    }

    @Test
    void plainVariantsAreEqual() {
        SplittableRandom random = new SplittableRandom(19);
        int first = 0;
        for (int i = 0; i < ROLLS; i++) {
            if (MineshaftTypeRoller.rollLayout(null, ShaftType.RUBY, 0, random).suffix().equals("1")) first++;
        }
        assertEquals(0.5, first / (double) ROLLS, TOLERANCE);
    }

    @Test
    void typesWithoutACrystalVariantNeverRollOne() {
        SplittableRandom random = new SplittableRandom(23);
        for (int i = 0; i < 1000; i++) {
            ShaftLayout layout = MineshaftTypeRoller.rollLayout(null, ShaftType.TITA, 100, random);
            assertFalse(layout.crystal());
            assertEquals("TITA_1", layout.code());
        }
        assertEquals("LITT_L", MineshaftTypeRoller.rollLayout(null, ShaftType.LITT, 100, random).code());
    }
}
