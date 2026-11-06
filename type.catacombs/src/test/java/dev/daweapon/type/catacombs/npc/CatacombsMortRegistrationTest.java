package dev.daweapon.type.catacombs.npc;

import dev.daweapon.type.catacombs.TypeCatacombsLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Catacombs server has no main world, and the generic loader only registers a server's NPCs when it has one or
 * asks for per-player NPC instances. Without the second, Mort was never registered and never appeared in any run.
 */
class CatacombsMortRegistrationTest {

    @Test
    void mortIsRegisteredOnAServerWithoutAMainWorld() {
        TypeCatacombsLoader loader = new TypeCatacombsLoader();
        assertNull(loader.getMainInstance());
        assertTrue(loader.usesPerPlayerNpcInstances());
        assertTrue(loader.getNPCs().stream().anyMatch(CatacombsMortNPC.class::isInstance));
    }
}
