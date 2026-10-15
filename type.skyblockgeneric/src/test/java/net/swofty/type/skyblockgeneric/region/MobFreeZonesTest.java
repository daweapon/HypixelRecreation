package net.swofty.type.skyblockgeneric.region;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobFreeZonesTest {
    @Test
    void nucleusAndDivanAreMobFree() {
        assertTrue(MobFreeZones.isMobFree(RegionType.CRYSTAL_NUCLEUS));
        assertTrue(MobFreeZones.isMobFree(RegionType.MINES_OF_DIVAN));
    }

    @Test
    void otherHollowsZonesAllowMobs() {
        assertFalse(MobFreeZones.isMobFree(RegionType.MITHRIL_DEPOSITS));
        assertFalse(MobFreeZones.isMobFree(RegionType.JUNGLE));
        assertFalse(MobFreeZones.isMobFree(RegionType.CRYSTAL_HOLLOWS));
        assertFalse(MobFreeZones.isMobFree(null));
    }
}
