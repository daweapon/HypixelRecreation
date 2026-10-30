package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteBowman;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteCaver;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteMage;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobGlaciteMutt;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite.MobLittlefoot;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.mob.ShaftMob;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.mob.ShaftMobSpawns;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShaftMobsTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    void statsMatchTheWikiForTheFourShaftMobs() {
        assertStats(new MobGlaciteBowman(), 174, 750_000, 2_550);
        assertStats(new MobGlaciteCaver(), 175, 800_000, 2_500);
        assertStats(new MobGlaciteMage(), 177, 825_000, 2_550);
        assertStats(new MobGlaciteMutt(), 173, 750_000, 2_500);
    }

    @Test
    void littlefootIsATwentyFiveMillionHealthGlacialHumanoidElusiveMiniboss() {
        MobLittlefoot littlefoot = new MobLittlefoot();
        assertStats(littlefoot, 508, 25_000_000, 10_000);
        assertEquals("LITTLEFOOT", littlefoot.getMobID());
        assertTrue(littlefoot.getMobTypes().containsAll(java.util.List.of(MobType.GLACIAL, MobType.HUMANOID, MobType.ELUSIVE)));
        assertEquals(2, littlefoot.getBestiaryBracket());
        assertEquals(5, littlefoot.getMaxBestiaryTier());
    }

    @Test
    void shaftMobsAreGlacialAndUseTheBestiaryFamilyIds() {
        assertEquals("GLACITE_BOWMAN", new MobGlaciteBowman().getMobID());
        assertEquals("GLACITE_MUTT", new MobGlaciteMutt().getMobID());
        assertTrue(new MobGlaciteMage().getMobTypes().contains(MobType.GLACIAL));
        assertTrue(new MobGlaciteMutt().getMobTypes().contains(MobType.ANIMAL));
        assertEquals(4, new MobGlaciteCaver().getBestiaryBracket());
    }

    @Test
    void tougherToolsHitHarder() {
        assertEquals(1, ShaftMob.breakingPowerMultiplier(0));
        assertTrue(ShaftMob.breakingPowerMultiplier(10) > ShaftMob.breakingPowerMultiplier(5));
        assertEquals(1, ShaftMob.breakingPowerMultiplier(-3));
    }

    @Test
    void littlefootReplacesOnePercentOfMobsAndEveryDenSpawn() {
        assertTrue(ShaftMobSpawns.replacesWithLittlefoot("LITT_L", 0.99));
        assertTrue(ShaftMobSpawns.replacesWithLittlefoot("RUBY_1", 0.005));
        assertFalse(ShaftMobSpawns.replacesWithLittlefoot("RUBY_1", 0.02));
        assertEquals(1, ShaftMobSpawns.LITTLEFOOT_REPLACEMENT_PERCENT);
    }

    private static void assertStats(ShaftMob mob, int level, double health, double damage) {
        assertEquals(level, mob.getLevel());
        assertEquals(health, mob.getBaseStatistics().getOverall(ItemStatistic.HEALTH));
        assertEquals(damage, mob.getBaseStatistics().getOverall(ItemStatistic.DAMAGE));
        assertEquals(1000, mob.getBaseStatistics().getOverall(ItemStatistic.DEFENSE));
    }
}
