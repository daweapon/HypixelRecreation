package dev.daweapon.type.skyblockgeneric.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.minestom.server.instance.InstanceContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kuudra is two stacked size-30 Magma Cubes; an arrow striking high on one is far from that cube's feet. */
class ArrowReachTest {
    private static final BoundingBox ARROW = new BoundingBox(0.5, 0.5, 0.5);
    private static InstanceContainer instance;
    private static Entity lower;
    private static Entity upper;

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        for (int cx = -2; cx <= 1; cx++) for (int cz = -2; cz <= 1; cz++) instance.loadChunk(cx, cz).join();
        lower = cube(new Pos(0, 0, 0));
        upper = cube(new Pos(0, 12, 0));
    }

    private static Entity cube(Pos at) {
        Entity cube = new Entity(EntityType.MAGMA_CUBE);
        cube.setNoGravity(true);
        cube.editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(30));
        cube.setInstance(instance, at).join();
        return cube;
    }

    private static List<Entity> struck(Pos from, Vec step, double reach) {
        return CollisionUtils.checkEntityCollisions(instance, ARROW, from, step, reach, entity -> true, null)
                .stream().map(EntityCollisionResult::entity).toList();
    }

    @Test
    void arrowsReachTheTopOfTheUpperCube() {
        Pos from = new Pos(5, 25, -9.5);
        Vec step = new Vec(0, 0, 3);
        assertTrue(struck(from, step, 3).isEmpty());
        assertTrue(struck(from, step, ArrowEntityImpl.LARGEST_TARGET_REACH).contains(upper));
    }

    @Test
    void arrowsReachTheSideOfTheLowerCube() {
        Pos from = new Pos(-6, 6, -9.5);
        Vec step = new Vec(0, 0, 3);
        assertTrue(struck(from, step, ArrowEntityImpl.LARGEST_TARGET_REACH).contains(lower));
    }
}
