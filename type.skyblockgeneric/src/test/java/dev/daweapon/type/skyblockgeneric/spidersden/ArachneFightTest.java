package dev.daweapon.type.skyblockgeneric.spidersden;

import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArachneFightTest {
    private static MobDrop row(List<MobDrop> drops, String id) {
        return drops.stream().filter(drop -> drop.choices().contains(id)).findFirst().orElseThrow();
    }

    private static boolean has(List<MobDrop> drops, String id) {
        return drops.stream().anyMatch(drop -> drop.choices().contains(id));
    }

    @Test
    void tiersMatchTheWiki() {
        assertEquals(20_000, ArachneTier.LESSER.arachne().health());
        assertEquals(300, ArachneTier.LESSER.arachne().damage());
        assertEquals(87, ArachneTier.LESSER.arachne().level());
        assertEquals(100_000, ArachneTier.GREATER.arachne().health());
        assertEquals(600, ArachneTier.GREATER.arachne().damage());
        assertEquals(143, ArachneTier.GREATER.arachne().level());
        assertEquals(4_000, ArachneTier.LESSER.brood().health());
        assertEquals(20_000, ArachneTier.GREATER.brood().health());
        assertEquals(2_000, ArachneTier.LESSER.broodHitCap());
        assertEquals(10_000, ArachneTier.GREATER.broodHitCap());
        assertEquals(4, ArachneTier.LESSER.itemsRequired());
        assertEquals(1, ArachneTier.GREATER.itemsRequired());
        assertEquals(15, ArachneTier.GREATER.combatLevel());
        assertEquals(8, ArachneTier.LESSER.essence());
        assertEquals(30, ArachneTier.GREATER.essence());
        assertEquals(60, ArachneTier.LESSER.firstKillSkyBlockXP() + ArachneTier.GREATER.firstKillSkyBlockXP());
    }

    @Test
    void everyProfileSharesItsBestiaryFamily() {
        assertEquals("ARACHNE", ArachneTier.LESSER.arachne().id());
        assertEquals("ARACHNE", ArachneTier.GREATER.arachne().id());
        assertEquals("ARACHNES_BROOD", ArachneTier.LESSER.brood().id());
        assertEquals("ARACHNES_KEEPER", SpiderDenMobs.ARACHNES_KEEPER.id());
    }

    @Test
    void lesserLootRanksSoulString() {
        List<MobDrop> top = ArachneFight.lootFor(ArachneTier.LESSER, 0, 4, false);
        assertEquals(9, row(top, "SOUL_STRING").min());
        assertEquals(10, row(top, "SOUL_STRING").max());
        List<MobDrop> low = ArachneFight.lootFor(ArachneTier.LESSER, 5, 2, false);
        assertEquals(3, row(low, "SOUL_STRING").min());
        assertEquals(3, row(low, "SOUL_STRING").max());
        assertEquals(100, row(top, "ARACHNE_FRAGMENT").chancePercent());
        assertEquals(10, row(top, "ARACHNE_FANG").chancePercent());
        assertEquals(25, row(top, "SHARD_ARACHNE").chancePercent());
        assertEquals(10, row(top, "ARACHNE_HELMET").chancePercent());
        assertEquals(2, row(top, "TARANTULA_PET").chancePercent());
        assertFalse(has(top, "ARACK"));
        assertFalse(has(top, "DARK_QUEENS_SOUL_DROP"));
    }

    @Test
    void greaterLootUsesTheCrystalRules() {
        List<MobDrop> placer = ArachneFight.lootFor(ArachneTier.GREATER, 0, 0, true);
        assertEquals(42, row(placer, "SOUL_STRING").min());
        assertEquals(44, row(placer, "SOUL_STRING").max());
        assertEquals(100, row(placer, "DARK_QUEENS_SOUL_DROP").chancePercent());
        List<MobDrop> third = ArachneFight.lootFor(ArachneTier.GREATER, 2, 0, false);
        assertEquals(50, row(third, "DARK_QUEENS_SOUL_DROP").chancePercent());
        assertEquals(30, row(third, "SOUL_STRING").min());
        List<MobDrop> sixth = ArachneFight.lootFor(ArachneTier.GREATER, 5, 0, false);
        assertFalse(has(sixth, "DARK_QUEENS_SOUL_DROP"));
        assertEquals(1, row(sixth, "SOUL_STRING").min());
        assertEquals(3, row(sixth, "SOUL_STRING").max());
        assertEquals(25, row(third, "ARACK").chancePercent());
        assertFalse(has(third, "ARACHNE_FRAGMENT"));
    }

    @Test
    void everyDropIsARealItem() {
        for (ArachneTier tier : ArachneTier.values()) {
            for (MobDrop drop : ArachneFight.lootFor(tier, 0, 1, true)) assertTrue(drop.isAvailable(), tier + " " + drop.choices());
        }
        for (MobDrop drop : SpiderDenMobs.ARACHNES_KEEPER.drops()) assertTrue(drop.isAvailable(), drop.choices().toString());
    }

    @Test
    void altarCoversTheCapturedBlockAndNotTheRoom() {
        assertTrue(ArachneFight.isAltar(-282, 48, -178));
        assertTrue(ArachneFight.isAltar(-283, 49, -177));
        assertFalse(ArachneFight.isAltar(-290, 47, -190));
    }

    @Test
    void keeperSpotsAreTheNineWikiSpots() {
        assertEquals(9, SpiderDenMobSpawner.KEEPER_SPOTS.size());
        assertEquals(60_000, SpiderDenMobSpawner.KEEPER_RESPAWN_MILLIS);
    }

    @Test
    void keeperDropsTheCalling() {
        assertEquals(100, row(SpiderDenMobs.ARACHNES_KEEPER.drops(), "ARACHNE_KEEPER_FRAGMENT").chancePercent());
        assertEquals(2.5, row(SpiderDenMobs.ARACHNES_KEEPER.drops(), "ARACHNE_NECKLACE").chancePercent());
    }
}
