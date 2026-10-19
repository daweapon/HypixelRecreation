package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.EmployeeType;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChocolateFactoryDataTest {
    private final DatapointChocolateFactory datapoint = new DatapointChocolateFactory("test");

    private ChocolateFactoryData roundTrip(ChocolateFactoryData data) {
        return datapoint.getSerializer().deserialize(datapoint.getSerializer().serialize(data));
    }

    @Test
    void savesWithoutPrestigeFieldKeepTheirOldDerivedLevel() {
        JSONObject legacy = new JSONObject().put("chocolate", 5).put("chocolateAllTime", 2_000_000_000L);
        ChocolateFactoryData data = datapoint.getSerializer().deserialize(legacy.toString());
        assertEquals(4, data.getPrestigeLevel());
        assertEquals(2_000_000_000L, data.getChocolateThisPrestige());
        assertEquals(4, roundTrip(data).getPrestigeLevel());
    }

    @Test
    void newFieldsSurviveARoundTrip() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.setPrestigeLevel(2);
        data.getClaimedSpawns().add("1:5");
        data.getUniqueEggLocations().add("HUB:3");
        data.addDuplicate(ChocolateRabbit.AARON);
        data.setMythicPity(17);
        data.setHitmanSlots(4);
        data.getHitmanEggs().add("1:7|HUB:3");
        data.setHoppityShopYear(3);
        data.getHoppityStock().add("AARON");
        data.getHoppityPurchases().add("AARON");
        ChocolateFactoryData copy = roundTrip(data);
        assertEquals(2, copy.getPrestigeLevel());
        assertTrue(copy.hasClaimedSpawn("1:5"));
        assertEquals(1, copy.uniqueLocationsOn("HUB"));
        assertEquals(1, copy.getDuplicates(ChocolateRabbit.AARON));
        assertEquals(17, copy.getMythicPity());
        assertEquals(4, copy.getHitmanSlots());
        assertEquals("1:7|HUB:3", copy.getHitmanEggs().getFirst());
        assertEquals(3, copy.getHoppityShopYear());
        assertTrue(copy.getHoppityPurchases().contains("AARON"));
    }

    @Test
    void prestigingNeedsTheThresholdAndResetsEverythingButTheBarn() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.addChocolate(149_999_999L);
        assertFalse(data.canPrestige());
        data.addChocolate(1);
        assertTrue(data.canPrestige());
        data.setEmployee(EmployeeType.RABBIT_BRO, 50);
        data.setRabbitBarnLevel(7);
        data.setHandBakedChocolateLevel(3);
        data.setCoachJackrabbitLevel(2);
        assertTrue(data.prestige());
        assertEquals(0, data.getChocolate());
        assertEquals(1, data.getPrestigeLevel());
        assertEquals(0, data.getChocolateThisPrestige());
        assertTrue(data.getEmployees().isEmpty());
        assertEquals(0, data.getHandBakedChocolateLevel());
        assertEquals(0, data.getCoachJackrabbitLevel());
        assertEquals(7, data.getRabbitBarnLevel());
        assertFalse(data.prestige());
    }

    @Test
    void prestigeTablesFollowTheWiki() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        long[] thresholds = {150_000_000L, 1_000_000_000L, 4_000_000_000L, 10_000_000_000L, 30_000_000_000L};
        double[] multipliers = {1.0, 1.1, 1.25, 1.5, 2.0, 2.5};
        long[] maxChocolate = {500_000_000L, 1_200_000_000L, 5_000_000_000L, 25_000_000_000L, 50_000_000_000L, 100_000_000_000L};
        for (int level = 0; level <= 5; level++) {
            data.setPrestigeLevel(level);
            assertEquals(multipliers[level], data.getPrestigeMultiplier(), 1e-9);
            assertEquals(maxChocolate[level], data.getMaxChocolate());
            assertEquals(120 + 20 * level, data.getEmployeeCap());
            if (level < 5) assertEquals(thresholds[level], data.getNextPrestigeThreshold());
        }
    }

    @Test
    void chocolateIsCappedByTheFactoryLevel() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.addChocolate(900_000_000L);
        assertEquals(500_000_000L, data.getChocolate());
        assertEquals(500_000_000L, data.getChocolateAllTime());
    }

    @Test
    void divineRabbitsRaiseTheEmployeeCap() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.addFoundRabbit(ChocolateRabbit.AURORA);
        data.addFoundRabbit(ChocolateRabbit.VEGA);
        assertEquals(122, data.getEmployeeCap());
        data.setRabbitYouthLevels(5);
        assertEquals(127, data.getEmployeeCap(EmployeeType.RABBIT_SIS));
        assertEquals(122, data.getEmployeeCap(EmployeeType.RABBIT_DADDY));
    }

    @Test
    void aCollectionRabbitAddsItsFlatAndMultiplierProduction() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.addFoundRabbit(ChocolateRabbit.AARON);
        assertEquals(1.0, data.getCollectionFlatProduction(), 1e-9);
        assertEquals(0.002, data.getCollectionMultiplierBonus(), 1e-9);
        assertEquals(1.002, data.getProductionMultiplier(), 1e-9);
        assertEquals(1.002, data.getChocolatePerSecond(), 1e-9);
    }

    @Test
    void mythicRabbitPerksApply() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.addFoundRabbit(ChocolateRabbit.DANTE);
        data.addFoundRabbit(ChocolateRabbit.GALAXY);
        data.addFoundRabbit(ChocolateRabbit.SIGMA);
        assertEquals(50 + 5 * 3, data.getCollectionFlatProduction(), 1e-9);
        assertEquals(0.05, data.getCollectionMultiplierBonus(), 1e-9);
        data.addFoundRabbit(ChocolateRabbit.ZETA);
        assertEquals(0.99, data.getEmployeeCostFactor(), 1e-9);
    }

    @Test
    void timeTowerChargesAccrueEveryEightHoursUpToThree() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.setPrestigeLevel(1);
        long start = 1_000_000_000L;
        data.accrueTimeTowerCharges(start);
        data.accrueTimeTowerCharges(start + 16L * 3_600_000L);
        assertEquals(2, data.getTimeTowerCharges());
        data.accrueTimeTowerCharges(start + 100L * 3_600_000L);
        assertEquals(3, data.getTimeTowerCharges());
        data.accrueTimeTowerCharges(start + 200L * 3_600_000L);
        assertEquals(3, data.getTimeTowerCharges());
    }

    @Test
    void einsteinShortensTheChargeToSevenHours() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.setPrestigeLevel(1);
        data.addFoundRabbit(ChocolateRabbit.EINSTEIN);
        long start = 1_000_000_000L;
        data.accrueTimeTowerCharges(start);
        data.accrueTimeTowerCharges(start + 14L * 3_600_000L);
        assertEquals(2, data.getTimeTowerCharges());
    }

    @Test
    void timeTowerDoesNotChargeBeforeFactoryTwo() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.accrueTimeTowerCharges(1_000_000_000L);
        data.accrueTimeTowerCharges(1_000_000_000L + 99L * 3_600_000L);
        assertEquals(0, data.getTimeTowerCharges());
    }

    @Test
    void duplicateChocolateScalesWithRarityAndStacksUpToTen() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        data.setEmployee(EmployeeType.RABBIT_BRO, 100);
        data.addFoundRabbit(ChocolateRabbit.AARON);
        double cps = data.getChocolatePerSecond();
        data.addDuplicate(ChocolateRabbit.AARON);
        assertEquals((long) (cps * 150 * 1.1), HoppityEggService.duplicateChocolate(data, ChocolateRabbit.AARON, 0, 1), 1);
        for (int i = 0; i < 20; i++) data.addDuplicate(ChocolateRabbit.AARON);
        assertEquals((long) (cps * 150 * 2.0), HoppityEggService.duplicateChocolate(data, ChocolateRabbit.AARON, 0, 1), 1);
        data.addFoundRabbit(ChocolateRabbit.KING);
        double withKing = HoppityEggService.duplicateChocolate(data, ChocolateRabbit.AARON, 0, 1);
        assertTrue(withKing > cps * 150 * 2.0);
    }

    @Test
    void theBarnHoldsTwentyRabbitsPlusTwoPerLevel() {
        ChocolateFactoryData data = new ChocolateFactoryData();
        assertEquals(20, data.getBarnCapacity());
        data.setRabbitBarnLevel(10);
        assertEquals(40, data.getBarnCapacity());
        for (ChocolateRabbit rabbit : ChocolateRabbit.values()) {
            if (data.getFoundRabbitCount() < 40) data.addFoundRabbit(rabbit);
        }
        assertTrue(data.isBarnFull());
    }
}
