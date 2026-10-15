package dev.daweapon.type.skyblockgeneric.garden.pest;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.garden.chip.ChipData;
import dev.daweapon.type.skyblockgeneric.garden.chip.ChipService;
import dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeEffects;
import dev.daweapon.type.skyblockgeneric.reforge.abilities.PestReforgeAbilities;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PestMechanicsTest {
    @Test
    void nozzlesFollowTheWikiTable() {
        assertEquals(25, PestNozzle.BASIC.bonusChance());
        assertEquals(1.5, PestNozzle.BASIC.spawnMultiplier());
        assertEquals(1, PestNozzle.BASIC.materialCost());
        assertEquals(30 * 60_000L, PestNozzle.BASIC.durationMillis());
        assertEquals(50, PestNozzle.JUICY.bonusChance());
        assertEquals(3, PestNozzle.JUICY.materialCost());
        assertEquals(45, PestNozzle.JUICY.minutes());
        assertEquals(75, PestNozzle.SALTY.bonusChance());
        assertEquals(3.0, PestNozzle.SALTY.spawnMultiplier());
        assertEquals(5, PestNozzle.SALTY.materialCost());
        assertEquals(60, PestNozzle.SALTY.minutes());
    }

    @Test
    void nozzleCyclingStaysWithinTheSprayonatorTier() {
        assertEquals(PestNozzle.BASIC, PestNozzle.BASIC.cycle(PestNozzle.BASIC));
        assertEquals(PestNozzle.JUICY, PestNozzle.BASIC.cycle(PestNozzle.SALTY));
        assertEquals(PestNozzle.BASIC, PestNozzle.JUICY.cycle(PestNozzle.JUICY));
        assertEquals(PestNozzle.BASIC, PestNozzle.SALTY.cycle(PestNozzle.SALTY));
        assertEquals(PestNozzle.JUICY, PestNozzle.SALTY.clamp(PestNozzle.JUICY));
        assertEquals(PestNozzle.BASIC, PestNozzle.parse("unknown"));
        assertEquals(PestNozzle.SALTY, PestNozzle.ofSprayonator("SALTY_SPRAYONATOR"));
        assertNull(PestNozzle.ofSprayonator("PEST_VACUUM"));
    }

    @Test
    void sprayedSpawnChanceScalesWithTheNozzle() {
        assertEquals(0.2, PestRules.spawnChancePercent(1), 1e-9);
        assertEquals(0.3, PestRules.spawnChancePercent(PestNozzle.BASIC.spawnMultiplier()), 1e-9);
        assertEquals(0.6, PestRules.spawnChancePercent(PestNozzle.SALTY.spawnMultiplier()), 1e-9);
    }

    @Test
    void cooldownReductionsStackAdditively() {
        double fullSet = 4 * PestRules.COOLDOWN_REDUCTION_PER_PESTHUNTER_PIECE + PestRules.COOLDOWN_REDUCTION_PEST_VEST;
        assertEquals(135_000, PestRules.cooldownMillis(false, false, fullSet));
        assertEquals(270_000, PestRules.cooldownMillis(false, false, PestRules.COOLDOWN_REDUCTION_PER_PESTHUNTER_PIECE));
        assertEquals(292_500, PestRules.cooldownMillis(false, false, PestRules.COOLDOWN_REDUCTION_SQUEAKY));
        assertEquals(270_000, PestRules.cooldownMillis(true, false, fullSet));
    }

    @Test
    void eradicatorGrowsWithThePesthunterPieces() {
        assertEquals(0, PestRules.eradicatorFortune(0));
        assertEquals(0, PestRules.eradicatorFortune(1));
        assertEquals(50, PestRules.eradicatorFortune(2));
        assertEquals(75, PestRules.eradicatorFortune(3));
        assertEquals(100, PestRules.eradicatorFortune(4));
        assertEquals(100, PestRules.eradicatorFortune(9));
    }

    @Test
    void vacuumDamageAddsBooksAndReforgeThenBuzzingDoubles() {
        assertEquals(400, PestRules.vacuumDamage(400, 0, 0, false));
        assertEquals(500, PestRules.vacuumDamage(400, 5, 0, false));
        assertEquals(500, PestRules.vacuumDamage(400, 9, 0, false));
        assertEquals(520, PestRules.vacuumDamage(400, 5, 20, false));
        assertEquals(1000, PestRules.vacuumDamage(400, 5, 0, true));
    }

    @Test
    void pestReforgesPlugIntoTheEffects() {
        assertEquals(100, ReforgeEffects.pestFortune(List.of(new PestReforgeAbilities.Beady())));
        assertEquals(2, ReforgeEffects.pestDamageMultiplier(List.of(new PestReforgeAbilities.Buzzing())));
        assertEquals(5, ReforgeEffects.pestCooldownReduction(List.of(new PestReforgeAbilities.Squeaky(), new PestReforgeAbilities.Squeaky())));
        assertEquals(0, ReforgeEffects.pestFortune(List.of(new PestReforgeAbilities.Buzzing())));
    }

    @Test
    void everyNaturalPestHasAMatchingVinylThatDropsAtFivePercent() {
        for (PestCatalogue.Pest pest : PestCatalogue.get().natural()) {
            String vinyl = PestVinyls.vinylOf(pest.id());
            assertNotNull(vinyl, pest.id());
            assertEquals(pest.id(), PestVinyls.pestOf(vinyl));
            assertNotNull(ItemType.get(vinyl), vinyl);
            assertTrue(pest.rare().stream().anyMatch(drop -> drop.choices().contains(vinyl) && drop.chancePercent() == PestVinyls.DROP_CHANCE_PERCENT),
                    pest.id() + " should drop " + vinyl);
        }
        assertEquals(13, PestVinyls.all().size());
    }

    @Test
    void stereoHarmonyCyclesThroughOwnedVinylsAndBackToNone() {
        List<String> options = List.of("", "BEETLE", "MOTH");
        assertEquals("BEETLE", PestVinyls.next(options, ""));
        assertEquals("MOTH", PestVinyls.next(options, "BEETLE"));
        assertEquals("", PestVinyls.next(options, "MOTH"));
        assertEquals("", PestVinyls.next(options, "RAT"));
        assertEquals(1000, PestVinyls.weight("MOTH", "MOTH"));
        assertEquals(0, PestVinyls.weight("MOTH", "BEETLE"));
        assertEquals(0, PestVinyls.weight("", "BEETLE"));
        assertEquals(1500, PestRules.vinylWeight(0.5));
    }

    @Test
    void bookwormDropsFromTheEarthwormWithARealItem() {
        PestCatalogue.Pest earthworm = PestCatalogue.get().pest("EARTHWORM");
        MobDrop book = earthworm.rare().stream().filter(drop -> drop.choices().contains("BOOKWORM_BOOK")).findFirst().orElseThrow();
        assertEquals(4, book.chancePercent());
        assertFalse(book.availableItems().isEmpty());
        assertNotNull(ItemType.get("FARMING_FOR_DUMMIES"));
    }

    @Test
    void rareCropDropsCarryTheWikiAmounts() {
        MobDrop hay = PestCatalogue.get().pest("FLY").rare().stream().filter(drop -> drop.choices().contains("ENCHANTED_HAY_BALE")).findFirst().orElseThrow();
        assertEquals(3, hay.rollAmount());
        assertEquals(2, PestCatalogue.get().pest("SLUG").rare().stream().filter(drop -> drop.choices().get(0).endsWith("MUSHROOM_BLOCK")).count());
    }

    @Test
    void pesterminatorBookDropsAndGrantsFortuneAndPestChance() {
        MobDrop book = PestCatalogue.get().pest("BEETLE").rare().stream().filter(drop -> drop.enchantment() != null).findFirst().orElseThrow();
        assertEquals(7, book.chancePercent());
        assertFalse(book.availableItems().isEmpty());
        assertEquals(EnchantmentType.PESTERMINATOR, book.enchantmentType());
        var stats = EnchantmentType.PESTERMINATOR.getEnch().getStatistics(6);
        assertEquals(12.0, stats.getOverall(ItemStatistic.FARMING_FORTUNE), 1e-9);
        assertEquals(6.0, stats.getOverall(ItemStatistic.BONUS_PEST_CHANCE), 1e-9);
    }

    @Test
    void pestDataKeepsNozzleSpraysDiscoAndSpawnPoints() {
        PestData data = new PestData();
        data.setSelectedNozzle("SALTY");
        data.setPlayingVinyl("MOTH");
        data.setSqueakyGiven(true);
        data.getSprays().put(3, new PestData.Spray("DUNG", 123L, "JUICY"));
        data.getAlive().add(PestData.Alive.createAt("MOTH", 0, 4.5, -7.25));
        data.getAlive().add(PestData.Alive.create("FLY", 2));
        PestData.Disco disco = new PestData.Disco(0, 1, 70, 2);
        disco.getVinyls().add("MOTH");
        data.setDisco(disco);

        PestData loaded = PestData.fromJson(data.toJson());
        assertEquals("SALTY", loaded.getSelectedNozzle());
        assertEquals("MOTH", loaded.getPlayingVinyl());
        assertTrue(loaded.isSqueakyGiven());
        assertEquals("JUICY", loaded.getSprays().get(3).nozzle());
        assertTrue(loaded.getAlive().get(0).hasSpawnPoint());
        assertEquals(4.5, loaded.getAlive().get(0).getSpawnX(), 1e-9);
        assertFalse(loaded.getAlive().get(1).hasSpawnPoint());
        assertTrue(PestDisco.attracts(loaded, "MOTH"));
        assertFalse(PestDisco.attracts(loaded, "FLY"));
    }

    @Test
    void oldSaveWithoutNozzleDefaultsToBasic() {
        var json = new PestData().toJson();
        json.remove("selectedNozzle");
        assertEquals("BASIC", PestData.fromJson(json).getSelectedNozzle());
    }

    @Test
    void discoSpawnsStayWithinTenBlocks() {
        PestData.Disco disco = new PestData.Disco(0, 10, 70, -20);
        for (double fraction : new double[]{0, 0.5, 1}) {
            for (double angle : new double[]{0, 1, 2.5, 4}) {
                double[] at = PestDisco.spawnPoint(disco, angle, fraction);
                assertTrue(Math.hypot(at[0] - 10, at[1] + 20) <= PestDisco.SPAWN_RADIUS + 1e-9);
            }
        }
    }

    @Test
    void sowledgeAndOverdriveScaleWithRarityAndLevel() {
        ChipData data = new ChipData();
        data.setConsumed(GardenChip.SOWLEDGE, 21);
        data.setLevel(GardenChip.SOWLEDGE, 20);
        data.setConsumed(GardenChip.OVERDRIVE, 1);
        data.setLevel(GardenChip.OVERDRIVE, 10);
        assertEquals(30, ChipService.bonus(data, GardenChip.SOWLEDGE), 1e-9);
        assertEquals(50, ChipService.bonus(data, GardenChip.OVERDRIVE), 1e-9);
        assertTrue(GardenChip.SOWLEDGE.isActive());
        assertTrue(GardenChip.OVERDRIVE.isActive());
    }

    @Test
    void jeffsProgressSurvivesSaving() {
        ChipData data = new ChipData();
        data.setJeffStage(2);
        assertEquals(2, ChipData.fromJson(data.toJson()).jeffStage());
        assertEquals(0, ChipData.fromJson(new ChipData().toJson()).jeffStage());
    }
}
