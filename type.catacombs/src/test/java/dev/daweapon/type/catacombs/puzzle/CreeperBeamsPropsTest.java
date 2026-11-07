package dev.daweapon.type.catacombs.puzzle;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.InstanceContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** An arrow shot down through the creeper at the lantern under it reaches the lantern, as on Hypixel. */
class CreeperBeamsPropsTest {
    private static final BoundingBox ARROW = new BoundingBox(0.5, 0.5, 0.5);
    private static final Pos CREEPER = new Pos(0.5, 75, 0.5);
    private static final Pos SHOOTER_EYE = new Pos(0.5, 76.5, 0.5);
    private static final Vec DOWN = new Vec(0, -2, 0);

    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    private static boolean stopsArrow(Entity creeper) {
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.loadChunk(0, 0).join();
        creeper.setInstance(instance, CREEPER).join();
        boolean hit = CollisionUtils.checkEntityCollisions(instance, ARROW, SHOOTER_EYE, DOWN, 24, entity -> true, null).stream()
                .anyMatch(result -> result.entity() == creeper);
        MinecraftServer.getInstanceManager().unregisterInstance(instance);
        return hit;
    }

    @Test
    void aPlainCreeperWouldSwallowTheArrow() {
        assertTrue(stopsArrow(new Entity(EntityType.CREEPER)));
    }

    @Test
    void thePuzzleCreeperAndBeamGuardiansLetArrowsThrough() {
        assertFalse(stopsArrow(CreeperBeamsRoom.prop(EntityType.CREEPER)));
        assertFalse(stopsArrow(CreeperBeamsRoom.prop(EntityType.GUARDIAN)));
    }
}
