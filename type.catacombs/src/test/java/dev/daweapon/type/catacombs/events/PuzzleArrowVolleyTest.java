package dev.daweapon.type.catacombs.events;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.InstanceContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * A shortbow volley (or an Archer's extra arrow) lands as several arrows on the same lantern; they share one volley so
 * the puzzle sees one shot instead of select, let go, select.
 */
class PuzzleArrowVolleyTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    private static void tick(InstanceContainer instance, List<Entity> arrows) {
        instance.tick(System.currentTimeMillis());
        arrows.forEach(arrow -> arrow.tick(System.currentTimeMillis()));
    }

    @Test
    void arrowsFiredTogetherShareAVolleyAndALaterShotDoesNot() {
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.loadChunk(0, 0).join();
        Entity first = new Entity(EntityType.ARROW);
        Entity second = new Entity(EntityType.ARROW);
        first.setInstance(instance, new Pos(0, 80, 0)).join();
        second.setInstance(instance, new Pos(0, 80, 0)).join();
        tick(instance, List.of(first, second));
        Entity later = new Entity(EntityType.ARROW);
        later.setInstance(instance, new Pos(0, 80, 0)).join();
        tick(instance, List.of(first, second, later));
        tick(instance, List.of(first, second, later));

        assertEquals(ActionCatacombsPuzzles.volley(first), ActionCatacombsPuzzles.volley(second));
        assertNotEquals(ActionCatacombsPuzzles.volley(first), ActionCatacombsPuzzles.volley(later));
        MinecraftServer.getInstanceManager().unregisterInstance(instance);
    }
}
