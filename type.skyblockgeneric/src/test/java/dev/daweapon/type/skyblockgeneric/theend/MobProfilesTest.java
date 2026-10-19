package dev.daweapon.type.skyblockgeneric.theend;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.EndMobSpecs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobProfilesTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    private static void assertProfile(CrystalHollowsMobSpec spec, int level, double health, double damage,
                                      long xp, int coins, int orbs) {
        assertEquals(level, spec.level(), spec.id() + " level");
        assertEquals(health, spec.health(), spec.id() + " health");
        assertEquals(damage, spec.damage(), spec.id() + " damage");
        assertEquals(xp, spec.combatXp(), spec.id() + " combat xp");
        assertEquals(coins, spec.coins(), spec.id() + " coins");
        assertEquals(orbs, spec.orbs(), spec.id() + " orbs");
    }

    @Test
    void endermenGetStrongerTheLowerTheySpawn() {
        assertProfile(EndMobSpecs.ENDERMAN_LOW, 45, 4_500, 500, 67, 15, 49);
        assertProfile(EndMobSpecs.ENDERMAN_MID, 51, 6_000, 600, 76, 17, 56);
        assertProfile(EndMobSpecs.ENDERMAN_HIGH, 59, 9_000, 700, 88, 19, 64);
    }

    @Test
    void endermitesMatchTheWiki() {
        assertProfile(EndMobSpecs.ENDERMITE_LOW, 34, 2_000, 400, 51, 11, 37);
        assertProfile(EndMobSpecs.ENDERMITE_HIGH, 37, 2_300, 475, 55, 12, 40);
        assertProfile(EndMobSpecs.NEST_ENDERMITE, 50, 4_500, 750, 75, 16, 55);
    }

    @Test
    void dragonsNestMobsMatchTheWiki() {
        assertProfile(EndMobSpecs.SEER, 55, 9_500, 500, 82, 18, 60);
        assertProfile(EndMobSpecs.OBSIDIAN_DEFENDER, 44, 10_000, 200, 66, 14, 48);
    }

    @Test
    void voidSepultureMobsMatchTheWiki() {
        assertProfile(EndMobSpecs.VOIDLING_FANATIC, 183, 750_000, 3_500, 274, 61, 201);
        assertProfile(EndMobSpecs.VOIDLING_EXTREMIST, 305, 8_000_000, 13_500, 457, 101, 335);
    }

    @Test
    void zealotFamilyMatchesTheWiki() {
        assertProfile(EndMobSpecs.ZEALOT, 73, 13_000, 1_250, 109, 24, 80);
        assertProfile(EndMobSpecs.ZEALOT_WITH_CHEST, 73, 13_000, 1_250, 109, 24, 80);
        assertProfile(EndMobSpecs.SPECIAL_ZEALOT, 73, 13_000, 1_250, 109, 24, 80);
        assertProfile(EndMobSpecs.ZEALOT_BRUISER, 115, 65_000, 2_500, 172, 38, 126);
    }

    @Test
    void zealotFamilyBestiaryIsBracketFiveWithTwentyFiveTiers() {
        for (CrystalHollowsMobSpec spec : new CrystalHollowsMobSpec[]{EndMobSpecs.ZEALOT, EndMobSpecs.ZEALOT_BRUISER, EndMobSpecs.SPECIAL_ZEALOT}) {
            assertEquals(5, spec.bracket());
            assertEquals(25, spec.maxTier());
        }
    }

    @Test
    void chestCarryingZealotsAddExactlyOneExclusiveDropPool() {
        assertTrue(EndMobSpecs.ZEALOT.exclusiveDrops().isEmpty());
        double total = EndMobSpecs.ZEALOT_WITH_CHEST.exclusiveDrops().stream().mapToDouble(CrystalHollowsMobSpec.Drop::chance).sum();
        assertEquals(87.8, total, 1e-9);
    }

    @Test
    void endermanArmorPiecesShareOnePercent() {
        double total = EndMobSpecs.ENDERMAN_LOW.exclusiveDrops().stream().mapToDouble(CrystalHollowsMobSpec.Drop::chance).sum();
        assertEquals(1.0, total, 1e-9);
        assertEquals(4, EndMobSpecs.ENDERMAN_HIGH.exclusiveDrops().size());
    }

    @Test
    void specialZealotEyeIsAwardedToTheSummonerNotDropped() {
        assertTrue(EndMobSpecs.SPECIAL_ZEALOT.drops().stream().noneMatch(drop -> drop.item() == ItemType.SUMMONING_EYE));
    }
}
