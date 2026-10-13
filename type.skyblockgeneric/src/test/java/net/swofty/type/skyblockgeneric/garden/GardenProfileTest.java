package net.swofty.type.skyblockgeneric.garden;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardenProfileTest {
    @Test
    void roundTripsEveryField() {
        GardenProfile profile = new GardenProfile();
        profile.setGardenXp(1234.5);
        profile.setAccessUnlocked(true);
        profile.addCopper(10);
        profile.addSowdust(2_500);
        profile.unlockPlot(3);
        profile.setPlotPreset(3, "Sugar Cane");
        profile.markPlotCleaned(3);
        profile.setCleanTotal(4, 812);
        profile.addCropCount(GardenCrop.CARROT, 5_000);
        profile.setMilestoneTier(GardenCrop.CARROT, 4);
        profile.setCropUpgradeTier(GardenCrop.WHEAT, 2);
        profile.setSpeedCap(GardenCrop.MUSHROOM, 233);

        GardenProfile copy = GardenProfile.deserialize(profile.serialize());

        assertEquals(1234.5, copy.getGardenXp());
        assertTrue(copy.isAccessUnlocked());
        assertEquals(10, copy.getCopper());
        assertEquals(2_500, copy.getSowdust());
        assertTrue(copy.isPlotUnlocked(3));
        assertEquals("Sugar Cane", copy.plotPreset(3, "Wheat"));
        assertTrue(copy.isPlotCleaned(3));
        assertFalse(copy.isPlotCleaned(4));
        assertEquals(812, copy.cleanTotal(4));
        assertEquals(5_000, copy.cropCount(GardenCrop.CARROT));
        assertEquals(4, copy.milestoneTier(GardenCrop.CARROT));
        assertEquals(2, copy.cropUpgradeTier(GardenCrop.WHEAT));
        assertEquals(233, copy.speedCap(GardenCrop.MUSHROOM, 0));
    }

    @Test
    void emptyOrNullInputGivesAFreshProfile() {
        assertEquals(0, GardenProfile.deserialize(null).getCopper());
        assertEquals(0, GardenProfile.deserialize("null").getGardenXp());
    }

    @Test
    void spendingNeverGoesNegative() {
        GardenProfile profile = new GardenProfile();
        profile.addCopper(100);

        assertFalse(profile.spendCopper(101));
        assertTrue(profile.spendCopper(100));
        assertEquals(0, profile.getCopper());
    }

    @Test
    void onlyRealPlotsCanBeUnlocked() {
        GardenProfile profile = new GardenProfile();

        assertFalse(profile.unlockPlot(0));
        assertFalse(profile.unlockPlot(GardenProfile.PLOT_COUNT + 1));
        assertTrue(profile.unlockPlot(GardenProfile.PLOT_COUNT));
        assertFalse(profile.unlockPlot(GardenProfile.PLOT_COUNT));
    }
}
