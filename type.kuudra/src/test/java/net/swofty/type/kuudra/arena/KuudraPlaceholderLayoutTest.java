package net.swofty.type.kuudra.arena;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraPlaceholderLayoutTest {
    private static final KuudraArena ARENA = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra")).arena();
    private static final KuudraPlaceholderLayout LAYOUT = new KuudraPlaceholderLayout(ARENA);

    private static KuudraPlaceholderLayout.Tile under(Pos position) {
        return LAYOUT.tileAt(position.blockX(), ARENA.floorY() - 1, position.blockZ());
    }

    @Test
    void playersStandOnTheFloorAndLavaSurroundsThePlatform() {
        assertEquals(KuudraPlaceholderLayout.Tile.FLOOR, LAYOUT.tileAt(-120, ARENA.floorY() - 1, -130));
        assertEquals(KuudraPlaceholderLayout.Tile.AIR, LAYOUT.tileAt(-120, ARENA.floorY(), -130));
        assertEquals(KuudraPlaceholderLayout.Tile.LAVA, LAYOUT.tileAt(-170, ARENA.lavaSurfaceY() - 1, -170));
        assertEquals(KuudraPlaceholderLayout.Tile.AIR, LAYOUT.tileAt(-170, ARENA.lavaSurfaceY(), -170));
        assertEquals(KuudraPlaceholderLayout.Tile.BEDROCK, LAYOUT.tileAt(-170, ARENA.sea().minY() - 1, -170));
    }

    @Test
    void theSpawnElleAndRespawnHaveGroundUnderThem() {
        assertEquals(KuudraPlaceholderLayout.Tile.BRIDGE, under(ARENA.spawn()));
        assertEquals(KuudraPlaceholderLayout.Tile.BRIDGE, under(ARENA.elle()));
        assertTrue(ARENA.onPlatform(ARENA.respawn()));
        assertEquals(KuudraPlaceholderLayout.Tile.FLOOR, under(ARENA.respawn()));
    }

    @Test
    void everyPileSpotAndTheBallistaIsMarked() {
        ARENA.piles().values().forEach(pile -> assertEquals(KuudraPlaceholderLayout.Tile.PILE, under(pile)));
        ARENA.supplySpots().values().forEach(spot -> {
            Pos position = spot.position();
            boolean onPlatform = ARENA.platform().containsColumn(position.blockX(), position.blockZ());
            int y = onPlatform ? ARENA.floorY() - 1 : ARENA.sea().maxY();
            assertEquals(KuudraPlaceholderLayout.Tile.SUPPLY_SPOT, LAYOUT.tileAt(position.blockX(), y, position.blockZ()), spot.name());
        });
        assertEquals(KuudraPlaceholderLayout.Tile.BALLISTA, under(ARENA.ballista()));
    }

    @Test
    void theLayoutSpansFromTheBedrockToTheFloor() {
        assertEquals(ARENA.sea().minY() - 1, LAYOUT.minY());
        assertEquals(ARENA.floorY() - 1, LAYOUT.maxY());
    }
}
