package net.swofty.type.crystalhollows.temple;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.swofty.type.crystalhollows.layout.SchematicFiles;
import net.swofty.type.skyblockgeneric.schematic.Schematic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TempleDoorTest {
    private static final Path SCHEMATICS = Path.of("../configuration/skyblock/crystal_hollows/schematics");

    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void doorFillsFiveWideEightHighTwoDeep() {
        List<Vec> offsets = TempleDoor.offsets();
        assertEquals(5 * 8 * 2, offsets.size());
        assertEquals(offsets.size(), new HashSet<>(offsets).size());
        assertEquals(10, TempleDoor.row(TempleDoor.MIN_Y).size());
    }

    @Test
    void doorSitsInsideTheShippedTempleCorridor() {
        Schematic schematic = SchematicFiles.probe(SCHEMATICS, "jungle_temple").schematic();
        for (Vec offset : TempleDoor.offsets()) {
            assertTrue(schematic.blockAt(offset.blockX(), offset.blockY(), offset.blockZ()).isAir(), offset.toString());
        }
        for (int y = TempleDoor.MIN_Y; y <= TempleDoor.MAX_Y; y++) {
            for (int z = TempleDoor.MIN_Z; z <= TempleDoor.MAX_Z; z++) {
                assertFalse(schematic.blockAt(TempleDoor.MIN_X - 1, y, z).isAir());
                assertFalse(schematic.blockAt(TempleDoor.MAX_X + 1, y, z).isAir());
            }
        }
        for (int x = TempleDoor.MIN_X; x <= TempleDoor.MAX_X; x++) {
            assertFalse(schematic.blockAt(x, TempleDoor.MAX_Y + 1, TempleDoor.MIN_Z).isAir());
        }
    }

    @Test
    void doorStaysShutWhilePlayersStandNearIt() {
        Pos origin = new Pos(100, 64, 200);
        assertTrue(TempleDoor.occupied(List.of(origin.add(34.5, 46, 41.5)), origin));
        assertTrue(TempleDoor.occupied(List.of(origin.add(34.5, 46, 43.5)), origin));
        assertFalse(TempleDoor.occupied(List.of(origin.add(34.5, 46, 50)), origin));
        assertFalse(TempleDoor.occupied(List.of(), origin));
    }
}
