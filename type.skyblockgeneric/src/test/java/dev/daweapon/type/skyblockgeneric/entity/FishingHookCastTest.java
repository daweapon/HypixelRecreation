package dev.daweapon.type.skyblockgeneric.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.skyblockgeneric.fishing.FishingMedium;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kuudra's crates float in lava a few blocks out and below the platform; a lava rod's cast has to reach them. */
class FishingHookCastTest {
    private static final int LAVA_TOP = 47;
    private static final int PLATFORM_Y = 50;
    private static final int PLATFORM_RADIUS = 3;
    private static InstanceContainer instance;

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, LAVA_TOP + 1, Block.LAVA));
        for (int cx = -4; cx <= 4; cx++) for (int cz = -4; cz <= 4; cz++) instance.loadChunk(cx, cz).join();
        for (int x = -PLATFORM_RADIUS; x <= PLATFORM_RADIUS; x++) {
            for (int z = -PLATFORM_RADIUS; z <= PLATFORM_RADIUS; z++) instance.setBlock(x, PLATFORM_Y, z, Block.STONE);
        }
    }

    private static Pos cast(Pos eye) {
        Entity controller = new Entity(EntityType.TEXT_DISPLAY);
        controller.setNoGravity(true);
        controller.setAerodynamics(FishingHook.BOBBER_AERODYNAMICS);
        controller.setInstance(instance, eye).join();
        controller.setVelocity(FishingHook.castVelocity(eye));
        long now = System.currentTimeMillis();
        for (int tick = 0; tick < 100; tick++) {
            controller.tick(now + tick * 50L);
            controller.setNoGravity(false);
            if (FishingMedium.fromBlock(instance.getBlock(controller.getPosition())) != null) break;
        }
        Pos landed = controller.getPosition();
        controller.remove();
        return landed;
    }

    @Test
    void aCastFromThePlatformLandsInTheLava() {
        Pos landed = cast(new Pos(0, PLATFORM_Y + 2.62, 0, 0, 30));
        assertEquals(FishingMedium.LAVA, FishingMedium.fromBlock(instance.getBlock(landed)), landed.toString());
        assertTrue(landed.z() > PLATFORM_RADIUS + 1, landed.toString());
    }

    @Test
    void aLevelCastFliesAboutVanillaDistance() {
        Pos landed = cast(new Pos(0, PLATFORM_Y + 2.62, 0, 90, 0));
        assertTrue(landed.x() < -8, landed.toString());
    }
}
