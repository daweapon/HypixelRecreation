package net.swofty.type.skyblockgeneric.crystalhollows;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.Rarity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PrehistoricEggsTest {
    @Test
    void thresholdsFollowTheEggLore() {
        var tiers = PrehistoricEggs.tiers();
        assertEquals(5, tiers.size());
        assertEquals(4_000, tiers.get(0).blocks());
        assertEquals(Rarity.LEGENDARY, tiers.get(4).rarity());
        assertEquals(100, tiers.get(4).chance());
    }

    @Test
    void crossingAThresholdRollsItsChanceOnce() {
        assertNull(PrehistoricEggs.crossed(0, 3_999, _ -> true));
        assertEquals(Rarity.COMMON, PrehistoricEggs.crossed(3_990, 4_010, _ -> true).rarity());
        assertNull(PrehistoricEggs.crossed(3_990, 4_010, _ -> false));
        assertNull(PrehistoricEggs.crossed(4_010, 4_020, _ -> true));
        assertEquals(Rarity.LEGENDARY, PrehistoricEggs.crossed(99_999, 100_000, chance -> chance >= 100).rarity());
    }

    @Test
    void teleportsAndVerticalMovementDoNotCount() {
        assertEquals(5, PrehistoricEggs.stepLength(new Pos(0, 64, 0), new Pos(3, 90, 4)), 1e-9);
        assertEquals(0, PrehistoricEggs.stepLength(new Pos(0, 64, 0), new Pos(300, 64, 0)));
    }
}
