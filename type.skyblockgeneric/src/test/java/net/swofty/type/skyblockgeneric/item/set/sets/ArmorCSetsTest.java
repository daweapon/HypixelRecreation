package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.commons.ServerType;
import net.swofty.type.skyblockgeneric.item.armor.PieceBonuses;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorCSetsTest {
    @Test
    void absorbDoublesDefenseOnlyInWater() {
        assertEquals(400, AbsorbSet.bonusDefense(400, true), 1e-9);
        assertEquals(0, AbsorbSet.bonusDefense(400, false), 1e-9);
        assertEquals(0, AbsorbSet.bonusDefense(-5, true), 1e-9);
    }

    @Test
    void fairyOutfitGivesOneHealthPerSoul() {
        assertEquals(0, FairyOutfitSet.health(0), 1e-9);
        assertEquals(247, FairyOutfitSet.health(247), 1e-9);
        assertEquals(0, FairyOutfitSet.health(-3), 1e-9);
    }

    @Test
    void depthCoatingDoublesPieceDefenseOnMiningIslands() {
        assertEquals(15, PieceBonuses.depthCoating(15, true), 1e-9);
        assertEquals(0, PieceBonuses.depthCoating(15, false), 1e-9);
        assertTrue(PieceBonuses.isOnMiningIsland(ServerType.SKYBLOCK_DWARVEN_MINES));
        assertFalse(PieceBonuses.isOnMiningIsland(ServerType.SKYBLOCK_JERRYS_WORKSHOP));
        assertFalse(PieceBonuses.isOnMiningIsland(null));
    }

    @Test
    void zombieHatGivesTenDefensePerZombie() {
        assertEquals(0, PieceBonuses.zombieHatDefense(0), 1e-9);
        assertEquals(30, PieceBonuses.zombieHatDefense(3), 1e-9);
        assertEquals(0, PieceBonuses.zombieHatDefense(-1), 1e-9);
    }

    @Test
    void holyBloodGrantsSeventyFiveHealthRegen() {
        assertEquals(75, HolyBloodSet.HEALTH_REGEN, 1e-9);
    }
}
