package net.swofty.type.skyblockgeneric.fishing.resolver;

import net.swofty.commons.skyblock.item.Rarity;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeaCreatureWeightsTest {
    private static final SeaCreatureWeights.Candidate STEVE = new SeaCreatureWeights.Candidate("FROZEN_STEVE", 5000, Rarity.COMMON);
    private static final SeaCreatureWeights.Candidate FROSTY = new SeaCreatureWeights.Candidate("FROSTY_THE_SNOWMAN", 2875, Rarity.UNCOMMON);
    private static final SeaCreatureWeights.Candidate GRINCH = new SeaCreatureWeights.Candidate("GRINCH", 1500, Rarity.RARE);
    private static final SeaCreatureWeights.Candidate NUTCRACKER = new SeaCreatureWeights.Candidate("NUTCRACKER", 500, Rarity.EPIC);
    private static final SeaCreatureWeights.Candidate YETI = new SeaCreatureWeights.Candidate("YETI", 100, Rarity.LEGENDARY);
    private static final SeaCreatureWeights.Candidate REINDRAKE = new SeaCreatureWeights.Candidate("REINDRAKE", 25, Rarity.MYTHIC);
    private static final List<SeaCreatureWeights.Candidate> WINTER = List.of(STEVE, FROSTY, GRINCH, NUTCRACKER, YETI, REINDRAKE);

    private static SeaCreatureWeights.Modifiers modifiers(String hook, String bait, boolean night, double tracking,
                                                          Map<String, Double> multipliers) {
        return new SeaCreatureWeights.Modifiers(hook, bait, night, tracking, multipliers);
    }

    @Test
    void winterWeightsSumToTenThousand() {
        double total = WINTER.stream().mapToDouble(c -> SeaCreatureWeights.weight(c, SeaCreatureWeights.Modifiers.none())).sum();
        assertEquals(10_000D, total);
    }

    @Test
    void commonHookOnlyBoostsCommonCreatures() {
        SeaCreatureWeights.Modifiers hook = modifiers("COMMON_HOOK", null, false, 0, Map.of());
        assertEquals(6_250D, SeaCreatureWeights.weight(STEVE, hook));
        assertEquals(2_875D, SeaCreatureWeights.weight(FROSTY, hook));
    }

    @Test
    void whaleBaitBoostsOnlyWeightsUnderFourHundred() {
        SeaCreatureWeights.Modifiers whale = modifiers(null, "WHALE_BAIT", false, 0, Map.of());
        assertEquals(125D, SeaCreatureWeights.weight(YETI, whale));
        assertEquals(31.25D, SeaCreatureWeights.weight(REINDRAKE, whale));
        assertEquals(500D, SeaCreatureWeights.weight(NUTCRACKER, whale));
    }

    @Test
    void lightBaitWorksByDayAndDarkBaitByNight() {
        assertEquals(125D, SeaCreatureWeights.weight(YETI, modifiers(null, "LIGHT_BAIT", false, 0, Map.of())));
        assertEquals(100D, SeaCreatureWeights.weight(YETI, modifiers(null, "LIGHT_BAIT", true, 0, Map.of())));
        assertEquals(125D, SeaCreatureWeights.weight(YETI, modifiers(null, "DARK_BAIT", true, 0, Map.of())));
        assertEquals(100D, SeaCreatureWeights.weight(YETI, modifiers(null, "DARK_BAIT", false, 0, Map.of())));
    }

    @Test
    void trackingBoostsOnlyLegendaryAndMythic() {
        SeaCreatureWeights.Modifiers tracking = modifiers(null, null, false, 5, Map.of());
        assertEquals(105D, SeaCreatureWeights.weight(YETI, tracking), 1e-9);
        assertEquals(26.25D, SeaCreatureWeights.weight(REINDRAKE, tracking), 1e-9);
        assertEquals(500D, SeaCreatureWeights.weight(NUTCRACKER, tracking));
    }

    @Test
    void modifiersStackMultiplicatively() {
        SeaCreatureWeights.Modifiers all = modifiers("COMMON_HOOK", "WHALE_BAIT", false, 5, Map.of("REINDRAKE", 1.1));
        assertEquals(25 * 1.25 * 1.05 * 1.1, SeaCreatureWeights.weight(REINDRAKE, all), 1e-9);
        assertEquals(27.5D, SeaCreatureWeights.weight(REINDRAKE, modifiers(null, null, false, 0, Map.of("REINDRAKE", 1.1))), 1e-9);
    }

    @Test
    void picksFollowTheWeights() {
        Random random = new Random(20);
        Map<String, Integer> counts = new HashMap<>();
        int rolls = 200_000;
        for (int i = 0; i < rolls; i++) {
            counts.merge(SeaCreatureWeights.pick(WINTER, SeaCreatureWeights.Modifiers.none(), random).id(), 1, Integer::sum);
        }
        assertEquals(0.5, counts.get("FROZEN_STEVE") / (double) rolls, 0.01);
        assertEquals(0.15, counts.get("GRINCH") / (double) rolls, 0.01);
        assertEquals(0.0025, counts.getOrDefault("REINDRAKE", 0) / (double) rolls, 0.001);
    }
}
