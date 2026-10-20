package dev.daweapon.type.skyblockgeneric.slayer;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.thepark.HowlingCaveMobSpecs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SvenSlayerTargetsTest {
    private static SlayerDefinition sven;

    @BeforeAll
    static void load() {
        SlayerRegistry.loadAll(new File("../configuration/skyblock/slayers.yml"));
        sven = SlayerRegistry.get(SlayerType.SVEN_PACKMASTER);
    }

    @Test
    void theWolvesPlayersActuallyFightFillTheCombatXpBar() {
        assertNotNull(sven);
        assertTrue(sven.targetMobIds().contains("RUIN_WOLF"));
        assertTrue(sven.targetMobIds().contains("OLD_WOLF"));
    }

    @Test
    void theHowlingCaveWolvesCountForWolfSlayer() {
        for (var spec : java.util.List.of(HowlingCaveMobSpecs.PACK_SPIRIT, HowlingCaveMobSpecs.HOWLING_SPIRIT,
                HowlingCaveMobSpecs.SOUL_OF_THE_ALPHA)) {
            assertTrue(sven.targetMobIds().contains(spec.id()), spec.id());
        }
    }

    @Test
    void theBossStillSpawnsAsAWoodlandMob() {
        assertEquals(java.util.List.of(MobType.WOODLAND), sven.bossMobTypes());
    }
}
