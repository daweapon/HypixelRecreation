package dev.daweapon.type.skyblockgeneric.crimson.kuudra;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
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
        assertEquals(79, ARENA.floorY());
        assertEquals(75, ARENA.lavaSurfaceY());
        assertEquals(new Pos(-102.5, 80, -106.5), ARENA.ballista());
        assertEquals(new Pos(-98, 79, -112.94), ARENA.piles().get("SHOP"));
        assertEquals(new Pos(-67.5, 77, -122.5), ARENA.supplySpots().get("TRIANGLE").position());
        assertEquals(new Pos(-133.5, 75, -115.5), ARENA.supplySpots().get("X_CANNON").crate());
        assertEquals(30, ARENA.supplySpots().get("X").radius());
        assertEquals(new Pos(-101.5, 79, -186.5, 180, 0), ARENA.spawn());
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
        assertEquals(ARENA.bridge().maxZ() + 1, ARENA.platform().minZ());
        assertTrue(ARENA.bridge().minY() <= ARENA.floorY() - 1 && ARENA.bridge().maxY() >= ARENA.floorY() - 1);
        assertTrue(ARENA.onPlatform(ARENA.respawn()));
    }

    @Test
    void kuudraSurfacesInTheLavaOnFourSides() {
        assertEquals(Set.of("FRONT", "LEFT", "RIGHT", "BACK"), ARENA.kuudraSides().keySet());
        ARENA.kuudraSides().values().forEach(side -> assertFalse(ARENA.platform().containsColumn(side.blockX(), side.blockZ())));
        assertEquals(2, ARENA.cannons().size());
        ARENA.cannons().forEach(cannon -> assertTrue(ARENA.onPlatform(cannon)));
        assertTrue(ARENA.onPlatform(ARENA.perkShop()));
        assertTrue(ARENA.onPlatform(ARENA.ellePlatform()));
    }

    @Test
    void kuudraSurfacesWellClearOfThePlatformEdge() {
        double halfWidth = 0.52 * 30 / 2;
        ARENA.kuudraSides().forEach((name, side) -> {
            boolean clearX = side.x() + halfWidth < ARENA.platform().minX() || side.x() - halfWidth > ARENA.platform().maxX() + 1;
            boolean clearZ = side.z() + halfWidth < ARENA.platform().minZ() || side.z() - halfWidth > ARENA.platform().maxZ() + 1;
            assertTrue(clearX || clearZ, name + " overlaps the platform");
            double distance = Math.hypot(side.x() - ARENA.centre().x(), side.z() - ARENA.centre().z());
            assertTrue(distance > 40 && distance < 56, name + " is " + distance + " blocks from the Ballista");
        });
    }

    @Test
    void minionsSurfaceInTheLavaAroundThePlatform() {
        assertTrue(ARENA.minionSpawns().size() >= 8);
        ARENA.minionSpawns().forEach(spawn -> {
            assertTrue(ARENA.sea().containsColumn(spawn.blockX(), spawn.blockZ()), spawn.toString());
            assertTrue(spawn.y() < ARENA.floorY());
        });
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
