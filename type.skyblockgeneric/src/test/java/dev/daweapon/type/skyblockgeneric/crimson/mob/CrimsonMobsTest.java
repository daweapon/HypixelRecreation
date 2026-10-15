package dev.daweapon.type.skyblockgeneric.crimson.mob;

import dev.daweapon.commons.skyblock.item.ItemType;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoExplosivity;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoSpewEvent;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBarbarian;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobFireMage;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobGoliathBarbarian;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMagmaGlare;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrimsonMobsTest {
    private static final Set<String> NOT_YET_IN_GAME = Set.of("KUUDRA_TIER_KEY", "KUUDRA_HOT_TIER_KEY");

    private static List<CrimsonMobProfile> profiles() throws IllegalAccessException {
        List<CrimsonMobProfile> profiles = new ArrayList<>();
        for (Field field : CrimsonMobs.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == CrimsonMobProfile.class) {
                profiles.add((CrimsonMobProfile) field.get(null));
            }
        }
        return profiles;
    }

    @Test
    void onlyBestiaryFamilyMobsCountTowardTheBestiary() {
        for (BestiaryCategory.CRIMSON_ISLE family : BestiaryCategory.CRIMSON_ISLE.values()) {
            for (BestiaryMob mob : family.getMobs()) assertTrue(mob.countsForBestiary(), mob.getMobID());
        }
        assertFalse(new MobFireMage().countsForBestiary());
        assertFalse(new MobBarbarian().countsForBestiary());
        assertFalse(new MobGoliathBarbarian().countsForBestiary());
    }

    @Test
    void profileIdsAreUnique() throws IllegalAccessException {
        Set<String> ids = new HashSet<>();
        for (CrimsonMobProfile profile : profiles()) assertTrue(ids.add(profile.id()), profile.id());
    }

    @Test
    void everyDropIsARealItemOrAKnownGap() throws IllegalAccessException {
        List<MobDrop> drops = new ArrayList<>(CorruptedMobs.DROPS);
        for (CrimsonMobProfile profile : profiles()) drops.addAll(profile.drops());
        for (CrimsonMiniboss miniboss : CrimsonMiniboss.values()) drops.addAll(miniboss.getDrops());
        for (MobDrop drop : drops) {
            for (String id : drop.choices()) {
                assertTrue(ItemType.get(id) != null || NOT_YET_IN_GAME.contains(id), "unknown drop " + id);
            }
        }
    }

    @Test
    void wikiNumbersForTheBlazeAndMagmaBoss() {
        assertEquals(140, CrimsonMobs.BLAZE.level());
        assertEquals(250_000, CrimsonMobs.BLAZE.health());
        assertEquals(210, CrimsonMobs.BLAZE.combatXp());
        assertEquals(160_000_000, CrimsonMobs.MAGMA_BOSS.health());
        assertEquals(500, CrimsonMobs.KRONDOR_NECROMANCER_SUMMON.defense());
    }

    @Test
    void minibossesIgnoreMagicFindExceptCyclamen() {
        for (CrimsonMiniboss miniboss : CrimsonMiniboss.values()) {
            for (MobDrop drop : miniboss.getDrops()) {
                assertEquals(drop.choices().contains("DYE_CYCLAMEN"), drop.magicFind(), miniboss + " " + drop.choices());
            }
        }
    }

    @Test
    void minibossesAreNeverCorrupted() {
        assertFalse(CrimsonMobs.BLADESOUL.corruptible());
        assertFalse(CrimsonMobs.MAGE_GUARD.corruptible());
        assertTrue(CrimsonMobs.BLAZE.corruptible());
    }

    @Test
    void vanquisherPityStepsUp() {
        assertEquals(1, VanquisherSpawns.pityMultiplier(639));
        assertEquals(2, VanquisherSpawns.pityMultiplier(640));
        assertEquals(3, VanquisherSpawns.pityMultiplier(960));
        assertEquals(4, VanquisherSpawns.pityMultiplier(5_000));
    }

    @Test
    void blazesAndGhastsKeepTheirHours() {
        assertTrue(CrimsonMobSpawner.Hours.NIGHT_BLAZE.contains(17));
        assertTrue(CrimsonMobSpawner.Hours.NIGHT_BLAZE.contains(8));
        assertFalse(CrimsonMobSpawner.Hours.NIGHT_BLAZE.contains(12));
        assertFalse(CrimsonMobSpawner.Hours.NIGHT_GHAST.contains(6));
        assertTrue(CrimsonMobSpawner.Hours.ALWAYS.contains(12));
    }

    @Test
    void magmaGlareDamageScalesAndCaps() {
        assertEquals(2_000, MobMagmaGlare.damageFor(1));
        assertEquals(6_000, MobMagmaGlare.damageFor(3));
        assertEquals(14_000, MobMagmaGlare.damageFor(20));
    }

    @Test
    void twentyCrimsonBestiaryFamilies() {
        assertEquals(20, BestiaryCategory.CRIMSON_ISLE.values().length);
    }

    @Test
    void noSpawnSpotInTheStrongholdEntranceHall() throws IllegalAccessException {
        for (Field field : CrimsonSpawnPoints.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != List.class) continue;
            field.setAccessible(true);
            for (Object entry : (List<?>) field.get(null)) {
                Pos spot = (Pos) entry;
                boolean inHall = spot.x() >= -372 && spot.x() <= -347 && spot.z() >= -605 && spot.z() <= -414
                        && spot.y() < 96;
                assertFalse(inHall, field.getName() + " spawns in the entrance hall at " + spot);
            }
        }
    }

    @Test
    void eruptionWavesSpewTheWholeEruption() {
        for (int total : new int[]{1, 7, 20, 200}) {
            int sum = 0;
            for (int wave = 0; wave < VolcanoEruptionService.SPEW_WAVES; wave++) {
                sum += new VolcanoSpewEvent(VolcanoExplosivity.CHILL, wave, VolcanoEruptionService.SPEW_WAVES).share(total);
            }
            assertEquals(total, sum);
        }
    }
}
