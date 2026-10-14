package net.swofty.type.dungeonhub;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelTypeLoader.LoaderValues;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeDungeonHubLoaderTest {
    private final TypeDungeonHubLoader loader = new TypeDungeonHubLoader();

    @Test
    void loaderServesTheDungeonHubServerType() {
        assertEquals(ServerType.SKYBLOCK_DUNGEON_HUB, loader.getType());
    }

    @Test
    void spawnPositionIsFixedRegardlessOfServerType() {
        LoaderValues values = loader.getLoaderValues();
        Pos expected = new Pos(-28.5, 121, 0.5, 90, 0);
        assertEquals(expected, values.spawnPosition().apply(ServerType.SKYBLOCK_DUNGEON_HUB));
        assertEquals(expected, values.spawnPosition().apply(ServerType.SKYBLOCK_HUB));
        assertFalse(values.announceDeathMessages());
    }

    @Test
    void customEventsAreEmpty() {
        assertTrue(loader.getCustomEvents().isEmpty());
    }
}
