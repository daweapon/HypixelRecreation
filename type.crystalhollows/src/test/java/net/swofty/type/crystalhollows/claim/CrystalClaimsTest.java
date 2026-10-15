package net.swofty.type.crystalhollows.claim;

import net.swofty.type.crystalhollows.gate.EntryGate;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalNucleus.State;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.region.RegionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystalClaimsTest {
    @Test
    void amethystNeedsAnActiveKeyAndEveryParkour() {
        assertEquals(CrystalClaims.Result.GRANTED, CrystalClaims.amethyst(true, true, State.NOT_FOUND));
        assertEquals(CrystalClaims.Result.NOT_EARNED, CrystalClaims.amethyst(false, true, State.NOT_FOUND));
        assertEquals(CrystalClaims.Result.NOT_EARNED, CrystalClaims.amethyst(true, false, State.NOT_FOUND));
    }

    @Test
    void amberNeedsAScentedDenEntry() {
        assertEquals(CrystalClaims.Result.GRANTED, CrystalClaims.amber(true, State.NOT_FOUND));
        assertEquals(CrystalClaims.Result.NOT_EARNED, CrystalClaims.amber(false, State.NOT_FOUND));
    }

    @Test
    void aCrystalAlreadyFoundOrPlacedCannotBeClaimedAgain() {
        assertEquals(CrystalClaims.Result.ALREADY_HAVE, CrystalClaims.amethyst(true, true, State.NOT_PLACED));
        assertEquals(CrystalClaims.Result.ALREADY_HAVE, CrystalClaims.amber(true, State.PLACED));
    }

    @Test
    void resolveGrantsOnlyAnEarnedCrystalThatIsNotYetFound() {
        assertEquals(CrystalClaims.Result.GRANTED, CrystalClaims.resolve(true, State.NOT_FOUND));
        assertEquals(CrystalClaims.Result.NOT_EARNED, CrystalClaims.resolve(false, State.NOT_FOUND));
        assertEquals(CrystalClaims.Result.ALREADY_HAVE, CrystalClaims.resolve(true, State.NOT_PLACED));
        assertEquals(CrystalClaims.Result.ALREADY_HAVE, CrystalClaims.resolve(false, State.PLACED));
    }

    @Test
    void everyNucleusCrystalHasAClaimSpotInsideItsCube() {
        var spots = ClaimSpots.read(java.nio.file.Path.of("../configuration/skyblock/crystal_hollows/claims.yml"));
        assertEquals(NucleusCrystal.values().length, spots.size());
        spots.values().forEach(spot -> {
            assertTrue(spot.cube().contains(spot.crystalAt()), spot.crystal().name());
            assertTrue(spot.walls().isEmpty() || spot.walls().stream().noneMatch(java.util.Objects::isNull));
        });
    }

    @Test
    void gatesTriggerOnlyOnEnteringTheirRegion() {
        assertTrue(EntryGate.entered(RegionType.JUNGLE_TEMPLE, RegionType.JUNGLE, RegionType.JUNGLE_TEMPLE));
        assertFalse(EntryGate.entered(RegionType.JUNGLE_TEMPLE, RegionType.JUNGLE_TEMPLE, RegionType.JUNGLE_TEMPLE));
        assertFalse(EntryGate.entered(RegionType.JUNGLE, RegionType.JUNGLE_TEMPLE, RegionType.JUNGLE_TEMPLE));
        assertEquals(1, EntryGate.GATES.size());
    }
}
