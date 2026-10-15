package dev.daweapon.type.kuudra.fight;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplyAndCargoTest {
    private static final KuudraArena ARENA = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra")).arena();
    private static final Predicate<Pos> WEST_OF_MINUS_TEN = position -> position.x() < -10;

    @Test
    void sixOfTheSevenSpotsGetACrateAndEverySpotIsSometimesEmpty() {
        Set<String> leftOut = new HashSet<>();
        Random random = new Random(7);
        for (int run = 0; run < 200; run++) {
            List<String> picked = SupplySpots.pick(ARENA.supplySpots().keySet(), 6, random);
            assertEquals(6, picked.size());
            assertEquals(6, new HashSet<>(picked).size());
            Set<String> missing = new HashSet<>(ARENA.supplySpots().keySet());
            picked.forEach(missing::remove);
            assertEquals(1, missing.size());
            leftOut.addAll(missing);
        }
        assertEquals(ARENA.supplySpots().keySet(), leftOut);
    }

    @Test
    void capturedCratesFloatNearTheirCapturedSpotAndOthersOutInTheLava() {
        Random random = new Random(1);
        KuudraArena.SupplySpot xCannon = ARENA.supplySpots().get("X_CANNON");
        Pos crate = SupplySpots.crateFor(ARENA, xCannon, 8, 3, random);
        assertTrue(crate.distance(xCannon.crate()) <= Math.sqrt(18) + 1e-9);
        KuudraArena.SupplySpot equals = ARENA.supplySpots().get("EQUALS");
        Pos out = SupplySpots.crateFor(ARENA, equals, 8, 3, random);
        assertFalse(ARENA.platform().containsColumn(out.blockX(), out.blockZ()));
        assertEquals(ARENA.lavaSurfaceY(), out.y());
    }

    @Test
    void reelingPullsTowardThePlayerButNeverOutOfTheLava() {
        Pos crate = new Pos(-20, 75, 0);
        Pos player = new Pos(0, 79, 0);
        Pos once = SupplySpots.reel(crate, player, 2, WEST_OF_MINUS_TEN);
        assertEquals(-18, once.x(), 1e-9);
        Pos again = SupplySpots.reel(once, player, 20, WEST_OF_MINUS_TEN);
        assertTrue(again.x() < -10 && again.x() >= -10.5);
        assertTrue(SupplySpots.atShore(again, player, WEST_OF_MINUS_TEN));
        assertFalse(SupplySpots.atShore(crate, player, WEST_OF_MINUS_TEN));
    }

    @Test
    void pickingUpTakesTheTiersTimeLessSteadyHandsAndStandingStill() {
        assertEquals(100, CargoPickup.ticksNeeded(5, 0));
        assertEquals(50, CargoPickup.ticksNeeded(5, 1));
        assertEquals(58, CargoPickup.ticksNeeded(5, 0.75));

        CargoPickup pickup = new CargoPickup(UUID.randomUUID(), 0, 0, 0.35, 4);
        assertEquals(CargoPickup.Result.PROGRESS, pickup.tick(0, 0));
        assertEquals(CargoPickup.Result.PROGRESS, pickup.tick(0.2, 0.1));
        assertEquals(50, pickup.percent());
        assertEquals(CargoPickup.Result.SLIPPED, pickup.tick(0.5, 0));
        assertEquals(CargoPickup.Result.PROGRESS, pickup.tick(0, 0));
        assertEquals(CargoPickup.Result.DONE, pickup.tick(0, 0));
    }

    @Test
    void theTitleBarMatchesTheCapture() {
        CargoPickup pickup = new CargoPickup(UUID.randomUUID(), 0, 0, 1, 20);
        assertEquals("<8>[<c><f>||||||||||||||||||||<8>] <b>0%", pickup.bar());
        pickup.tick(0, 0);
        assertEquals("<8>[<c>|<f>|||||||||||||||||||<8>] <b>5%", pickup.bar());
        for (int i = 0; i < 9; i++) pickup.tick(0, 0);
        assertTrue(pickup.bar().startsWith("<8>[<e>||||||||||<f>"));
        for (int i = 0; i < 10; i++) pickup.tick(0, 0);
        assertEquals("<8>[<a>||||||||||||||||||||<f><8>] <b>100%", pickup.bar());
    }

    @Test
    void carriedSuppliesDropWhenTooSlowOrHitHard() {
        CargoCarry carry = new CargoCarry(UUID.randomUUID(), 2);
        for (int i = 0; i < 39; i++) assertFalse(carry.tick());
        assertEquals(1, carry.secondsLeft());
        assertTrue(carry.tick());

        assertTrue(CargoCarry.dropsOnHit(2_500, 10_000, 0.25));
        assertFalse(CargoCarry.dropsOnHit(2_499, 10_000, 0.25));
        assertFalse(CargoCarry.dropsOnHit(5_000, 0, 0.25));
    }
}
