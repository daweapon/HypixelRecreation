package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.dungeons.catacombs.layout.Rotation;
import dev.daweapon.type.catacombs.engine.GateLift;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Entrance gate is solid in the generated world while the party waits and sinks away with the lift effect. */
class EntranceGateLiftTest {
    private static final Path ROOMS = Path.of("..", "configuration", "skyblock", "dungeons", "rooms");

    @BeforeAll
    static void init() {
        Assumptions.assumeTrue(Files.isDirectory(ROOMS), "room templates not found next to the module");
        MinecraftServer.init();
    }

    @Test
    void theGateBlocksAreTheOnesTheCapturedFallingBlocksCarried() {
        assertEquals(DoorGates.ENTRANCE, Block.fromStateId(7765));
        assertEquals(DoorGates.WITHER, Block.fromStateId(12913));
        assertEquals(DoorGates.BLOOD, Block.fromStateId(11458));
    }

    @Test
    void theGateIsClosedWhileWaitingAndOpensWithTheLiftInEveryTurn() {
        RoomTemplateLibrary library = RoomTemplateLibrary.fromDirectory(ROOMS);
        RoomStructures structures = new RoomStructures(ROOMS);
        for (String id : List.of("entrance_room", "start")) {
            for (Rotation rotation : Rotation.values()) {
                FloorLayout layout = RoomFeaturesTest.around(library, library.find(id).orElseThrow(), rotation);
                CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, structures);
                InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
                instance.setGenerator(new CatacombsArenaGenerator(blocks));
                LayoutDoor exit = blocks.gates().entranceDoors().getFirst();
                List<int[]> passage = DoorFrame.passage(exit).blocks();
                for (int[] at : passage) instance.loadChunk(at[0] >> 4, at[2] >> 4).join();
                String context = id + " " + rotation;
                for (int[] at : passage) {
                    assertTrue(instance.getBlock(at[0], at[1], at[2]).compare(DoorGates.ENTRANCE), context + " gate missing while waiting");
                }

                GateLift lift = GateLift.start(instance, passage, DoorGates.ENTRANCE);
                List<Entity> riders = lift.riders();
                assertEquals(passage.size(), riders.size());
                riders.forEach(rider -> assertTrue(((FallingBlockMeta) rider.getEntityMeta()).getBlock().compare(DoorGates.ENTRANCE)));
                for (int[] at : passage) assertEquals(Block.BARRIER, instance.getBlock(at[0], at[1], at[2]), context);
                boolean done = false;
                for (int tick = 1; tick <= GateLift.DURATION_TICKS; tick++) {
                    done = lift.tick();
                    boolean open = instance.getBlock(passage.getFirst()[0], passage.getFirst()[1], passage.getFirst()[2]).isAir();
                    assertEquals(tick >= GateLift.OPEN_TICK, open, context + " tick " + tick);
                }
                assertTrue(done, context);
                for (int[] at : passage) assertTrue(instance.getBlock(at[0], at[1], at[2]).isAir(), context);
                riders.forEach(rider -> assertTrue(rider.isRemoved(), context));
                MinecraftServer.getInstanceManager().unregisterInstance(instance);
            }
        }
    }
}
