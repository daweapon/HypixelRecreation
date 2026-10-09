package net.swofty.type.skyblockgeneric.crimson.kuudra;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraArenaTest {
    private static final KuudraArena ARENA = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra")).arena();

    @Test
    void usesHypixelsOwnCoordinates() {
        assertEquals("kuudra_hollow.polar", ARENA.world());
        assertEquals(78, ARENA.floorY());
        assertEquals(75, ARENA.lavaSurfaceY());
        assertEquals(new Pos(-101.5, 82, -105.5), ARENA.ballista());
        assertEquals(new Pos(-98.5, 78.4, -113.5), ARENA.piles().get("SHOP"));
        assertEquals(new Pos(-67.5, 77, -122.5), ARENA.supplySpots().get("TRIANGLE").position());
        assertEquals(30, ARENA.supplySpots().get("X").radius());
    }

    @Test
    void hasSixPilesAndSevenSupplySpots() {
        assertEquals(Set.of("SHOP", "TRIANGLE", "EQUALS", "SLASH", "X_CANNON", "X"), ARENA.piles().keySet());
        assertEquals(Set.of("TRIANGLE", "X", "EQUALS", "SLASH", "SHOP", "X_CANNON", "SQUARE"), ARENA.supplySpots().keySet());
    }

    @Test
    void thePilesRingTheBallistaOnThePlatform() {
        assertTrue(ARENA.onPlatform(ARENA.ballista()));
        ARENA.piles().forEach((name, pile) -> {
            assertTrue(ARENA.onPlatform(pile), name);
            assertTrue(pile.distance(ARENA.ballista().withY(pile.y())) < 10, name);
        });
    }

    @Test
    void theBridgeLeadsFromTheSpawnToThePlatform() {
        assertFalse(ARENA.onPlatform(ARENA.spawn()));
        assertTrue(ARENA.bridge().containsColumn(ARENA.spawn().blockX(), ARENA.spawn().blockZ()));
        assertTrue(ARENA.bridge().containsColumn(ARENA.elle().blockX(), ARENA.elle().blockZ()));
        assertEquals(ARENA.platform().maxZ() + 1, ARENA.bridge().minZ());
        assertEquals(ARENA.floorY() - 1, ARENA.bridge().maxY());
        assertTrue(ARENA.onPlatform(ARENA.respawn()));
    }

    @Test
    void playersReturnToTheForgottenSkull() {
        Pos crag = new Pos(-370.5, 114, -1041.5);
        assertTrue(ARENA.exit().distance(crag) < 6);
    }

    @Test
    void boxesMustBeOrdered() {
        assertThrows(IllegalArgumentException.class, () -> new KuudraArena.Box(1, 0, 0, 0, 0, 0));
        KuudraArena.Box box = new KuudraArena.Box(0, 0, 0, 2, 2, 2);
        assertTrue(box.contains(2, 2, 2));
        assertFalse(box.contains(3, 2, 2));
    }
}
