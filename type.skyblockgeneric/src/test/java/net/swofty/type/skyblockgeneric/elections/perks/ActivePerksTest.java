package net.swofty.type.skyblockgeneric.elections.perks;

import net.swofty.type.skyblockgeneric.elections.ElectionData;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor.Perk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivePerksTest {

    @Test
    void mayorPerksAreActiveAndReportedAsMayorPerks() {
        ActivePerks perks = ActivePerks.of(election("DERPY", List.of("MOAR_SKILLZ", "QUAD_TAXES"), null), null);

        assertTrue(perks.isActive(Perk.MOAR_SKILLZ));
        assertTrue(perks.isMayorPerk(Perk.QUAD_TAXES));
        assertFalse(perks.isMinisterPerk(Perk.QUAD_TAXES));
        assertFalse(perks.isActive(Perk.TURBO_MINIONS));
    }

    @Test
    void ministerPerkIsActiveWithTheSameMagnitudeAsAMayorPerk() {
        ActivePerks asMinister = ActivePerks.of(election("COLE", List.of("PROSPECTION"), "PET_XP_BUFF"), null);
        ActivePerks asMayor = ActivePerks.of(election("DIANA", List.of("PET_XP_BUFF"), null), null);

        assertTrue(asMinister.isActive(Perk.PET_XP_BUFF));
        assertTrue(asMinister.isMinisterPerk(Perk.PET_XP_BUFF));
        assertFalse(asMinister.isMayorPerk(Perk.PET_XP_BUFF));
        assertEquals(asMayor.multiplier(Perk.PET_XP_BUFF), asMinister.multiplier(Perk.PET_XP_BUFF));
    }

    @Test
    void perkpocalypseGrantsEveryPerkOfTheRotatedMayor() {
        ActivePerks perks = ActivePerks.of(election("JERRY", List.of("PERKPOCALYPSE", "STATSPOCALYPSE"), null),
                SkyBlockMayor.MARINA);

        for (Perk perk : SkyBlockMayor.MARINA.getAllPerks()) {
            assertTrue(perks.isActive(perk), perk.name());
            assertFalse(perks.isMayorPerk(perk), perk.name());
        }
        assertTrue(perks.isActive(Perk.STATSPOCALYPSE));
    }

    @Test
    void magnitudesFollowTheWiki() {
        ActivePerks perks = ActivePerks.of(election("DERPY",
                List.of("QUAD_TAXES", "TURBO_MINIONS", "DOUBLE_MOBS_HP", "MOAR_SKILLZ"), "SLAYER_XP_BUFF"), null);

        assertEquals(4D, perks.factor(Perk.QUAD_TAXES));
        assertEquals(2D, perks.factor(Perk.TURBO_MINIONS));
        assertEquals(2D, perks.factor(Perk.DOUBLE_MOBS_HP));
        assertEquals(1.5D, perks.multiplier(Perk.MOAR_SKILLZ));
        assertEquals(1.25D, perks.multiplier(Perk.SLAYER_XP_BUFF));
    }

    @Test
    void inactivePerksAreNeutral() {
        ActivePerks perks = ActivePerks.of(election("COLE", List.of("PROSPECTION"), null), null);

        assertEquals(0D, perks.bonus(Perk.MINING_XP_BUFF));
        assertEquals(1D, perks.multiplier(Perk.MOAR_SKILLZ));
        assertEquals(1D, perks.factor(Perk.QUAD_TAXES));
        assertFalse(ActivePerks.NONE.isActive(Perk.PROSPECTION));
    }

    @Test
    void snapshotIsReusedUntilTheElectionStateChanges() {
        ElectionData data = election("COLE", List.of("PROSPECTION"), "STOCK_EXCHANGE");
        ActivePerks perks = ActivePerks.of(data, null);

        assertTrue(perks.reflects(data, null));
        assertFalse(perks.reflects(data, SkyBlockMayor.AATROX));

        data.setMinisterPerk("SHOPPING_SPREE");
        assertFalse(perks.reflects(data, null));

        ActivePerks refreshed = ActivePerks.of(data, null);
        data.setCurrentMayorPerks(List.of("PROSPECTION", "MINING_XP_BUFF"));
        assertFalse(refreshed.reflects(data, null));
        assertFalse(refreshed.reflects(new ElectionData(), null));
    }

    @Test
    void unknownPerkNamesAreIgnored() {
        ActivePerks perks = ActivePerks.of(election("COLE", List.of("PROSPECTION", "RETIRED_PERK"), "NOT_A_PERK"), null);

        assertEquals(1, perks.all().size());
    }

    static ElectionData election(String mayor, List<String> mayorPerks, String ministerPerk) {
        ElectionData data = new ElectionData();
        data.setCurrentMayor(mayor);
        data.setCurrentMayorPerks(mayorPerks);
        data.setMinisterPerk(ministerPerk);
        return data;
    }
}
