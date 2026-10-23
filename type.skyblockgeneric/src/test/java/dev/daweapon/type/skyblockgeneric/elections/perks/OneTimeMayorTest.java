package dev.daweapon.type.skyblockgeneric.elections.perks;

import dev.daweapon.type.skyblockgeneric.elections.ElectionData;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor.Perk;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OneTimeMayorTest {

    @Test
    void oneTimeMayorsAreNeverInTheRotation() {
        assertEquals(List.of(SkyBlockMayor.DANTE, SkyBlockMayor.TECHNOBLADE), SkyBlockMayor.getOneTimeMayors());
        assertFalse(SkyBlockMayor.getRegularMayors().contains(SkyBlockMayor.DANTE));
        assertFalse(SkyBlockMayor.getRegularMayors().contains(SkyBlockMayor.TECHNOBLADE));
        assertEquals(List.of(SkyBlockMayor.SCORPIUS, SkyBlockMayor.DERPY, SkyBlockMayor.JERRY), SkyBlockMayor.getSpecialMayors());
    }

    @Test
    void perkCountsFollowTheWiki() {
        assertEquals(2, SkyBlockMayor.DANTE.getAllPerks().length);
        assertEquals(5, SkyBlockMayor.TECHNOBLADE.getAllPerks().length);
    }

    @Test
    void generatedElectionsNeverOfferOneTimeMayors() {
        for (int year = 1; year <= 200; year++) {
            ElectionData data = new ElectionData();
            data.startNewElection(year);
            boolean offered = data.getCandidates().stream()
                    .anyMatch(candidate -> Arrays.asList("DANTE", "TECHNOBLADE").contains(candidate.getMayorName()));
            assertFalse(offered, "year " + year);
        }
    }

    @Test
    void danteDoublesTaxesAndTechnobladeRemovesThem() {
        ActivePerks dante = ActivePerks.of(election("DANTE", List.of("ECONOMY_RESTORATION", "INCREASED_SECURITY")), null);
        ActivePerks technoblade = ActivePerks.of(election("TECHNOBLADE", Arrays.stream(SkyBlockMayor.TECHNOBLADE.getAllPerks()).map(Enum::name).toList()), null);
        ActivePerks derpy = ActivePerks.of(election("DERPY", List.of("QUAD_TAXES")), null);

        assertEquals(2D, TaxPerks.multiplier(dante));
        assertEquals(0D, TaxPerks.multiplier(technoblade));
        assertEquals(4D, TaxPerks.multiplier(derpy));
        assertEquals(1D, TaxPerks.multiplier(ActivePerks.NONE));
    }

    @Test
    void technobladeMagnitudes() {
        ActivePerks perks = ActivePerks.of(election("TECHNOBLADE", Arrays.stream(SkyBlockMayor.TECHNOBLADE.getAllPerks()).map(Enum::name).toList()), null);

        assertEquals(1.5D, perks.multiplier(Perk.BLOOD_GODS_BLESSING));
        assertEquals(50D, perks.bonus(Perk.UNLIMITED_SPEED));
        assertEquals(2D, perks.factor(Perk.POTATO_CROWNS_RADIANCE));
    }

    @Test
    void danteElectionsOfferFiveCloneCandidates() {
        ElectionData data = new ElectionData();
        data.setCurrentMayor("DANTE");
        data.startNewElection(130);

        assertEquals(5, data.getCandidates().size());
        assertTrue(data.getCandidates().stream().allMatch(candidate -> candidate.getMayorName().equals("DANTE")));
        assertTrue(data.getCandidates().stream().allMatch(candidate -> candidate.getMinisterPerk() == null));
    }

    @Test
    void retiredAndOneTimeMayorsCannotBeMinister() {
        assertFalse(SkyBlockMayor.BARRY.canBeMinister());
        assertFalse(SkyBlockMayor.getRegularMayors().contains(SkyBlockMayor.BARRY));
        assertFalse(SkyBlockMayor.DANTE.canBeMinister());
        assertTrue(SkyBlockMayor.DIANA.canBeMinister());
    }

    private static ElectionData election(String mayor, List<String> perks) {
        ElectionData data = new ElectionData();
        data.setCurrentMayor(mayor);
        data.setCurrentMayorPerks(perks);
        return data;
    }
}
