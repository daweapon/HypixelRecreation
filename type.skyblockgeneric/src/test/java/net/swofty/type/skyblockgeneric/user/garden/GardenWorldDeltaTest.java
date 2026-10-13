package net.swofty.type.skyblockgeneric.user.garden;

import net.hollowcube.polar.PolarChunk;
import net.hollowcube.polar.PolarSection;
import net.hollowcube.polar.PolarWorld;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardenWorldDeltaTest {
    private static final byte MIN = 0;
    private static final byte MAX = 1;

    private static PolarSection filled(String block) {
        return new PolarSection(new String[]{block}, null, new String[]{"minecraft:plains"}, null,
                PolarSection.LightContent.MISSING, null, PolarSection.LightContent.MISSING, null);
    }

    private static PolarSection singleBlock(String base, String block, int index) {
        int[] data = new int[4096];
        data[index] = 1;
        return new PolarSection(new String[]{base, block}, data, new String[]{"minecraft:plains"}, null,
                PolarSection.LightContent.MISSING, null, PolarSection.LightContent.MISSING, null);
    }

    private static PolarChunk chunk(int x, int z, PolarSection... sections) {
        return new PolarChunk(x, z, sections, List.of(), new int[32][], new byte[0]);
    }

    private static PolarWorld world(PolarChunk... chunks) {
        return new PolarWorld((short) 1, 4000, PolarWorld.CompressionType.NONE, MIN, MAX, new byte[0],
                new ArrayList<>(List.of(chunks)));
    }

    @Test
    void identicalChunksAreNotStored() {
        PolarWorld template = world(chunk(0, 0, filled("minecraft:stone"), new PolarSection()));
        PolarWorld current = world(chunk(0, 0, filled("minecraft:stone"), new PolarSection()));

        assertTrue(GardenWorldDelta.changedChunks(template, current).chunks().isEmpty());
    }

    @Test
    void changedBlocksAreStoredAndUnchangedChunksAreNot() {
        PolarWorld template = world(
                chunk(0, 0, filled("minecraft:stone"), new PolarSection()),
                chunk(1, 0, filled("minecraft:stone"), new PolarSection()));
        PolarWorld current = world(
                chunk(0, 0, singleBlock("minecraft:stone", "minecraft:dirt", 5), new PolarSection()),
                chunk(1, 0, filled("minecraft:stone"), new PolarSection()));

        PolarWorld delta = GardenWorldDelta.changedChunks(template, current);

        assertEquals(1, delta.chunks().size());
        assertNotNull(delta.chunkAt(0, 0));
        assertNull(delta.chunkAt(1, 0));
    }

    @Test
    void chunksOutsideTheTemplateAreStored() {
        PolarWorld template = world(chunk(0, 0, new PolarSection(), new PolarSection()));
        PolarWorld current = world(chunk(0, 0, new PolarSection(), new PolarSection()),
                chunk(9, 9, new PolarSection(), new PolarSection()));

        assertNotNull(GardenWorldDelta.changedChunks(template, current).chunkAt(9, 9));
    }

    @Test
    void equivalentStatesCompareEqual() {
        assertEquals(GardenWorldDelta.normalise("minecraft:oak_stairs[half=top,facing=north]"),
                GardenWorldDelta.normalise("oak_stairs[facing=north,half=top]"));
        assertEquals("air", GardenWorldDelta.normalise("minecraft:cave_air"));
        assertEquals("stone", GardenWorldDelta.normalise("minecraft:stone"));
    }

    @Test
    void airSectionsMatchEmptySections() {
        PolarWorld template = world(chunk(0, 0, new PolarSection(), new PolarSection()));
        PolarWorld current = world(chunk(0, 0, filled("minecraft:air"), filled("minecraft:cave_air")));

        assertTrue(GardenWorldDelta.changedChunks(template, current).chunks().isEmpty());
    }

    @Test
    void overlayReplacesTemplateChunksWithStoredOnes() {
        PolarChunk untouched = chunk(1, 0, filled("minecraft:stone"), new PolarSection());
        PolarWorld template = world(chunk(0, 0, filled("minecraft:stone"), new PolarSection()), untouched);
        PolarChunk edited = chunk(0, 0, singleBlock("minecraft:stone", "minecraft:dirt", 0), new PolarSection());
        PolarChunk extra = chunk(5, 5, new PolarSection(), new PolarSection());

        PolarWorld merged = GardenWorldDelta.overlay(template, world(edited, extra));

        assertEquals(3, merged.chunks().size());
        assertSame(edited, merged.chunkAt(0, 0));
        assertSame(untouched, merged.chunkAt(1, 0));
        assertSame(extra, merged.chunkAt(5, 5));
    }

    @Test
    void overlayOfTheDeltaRebuildsTheCurrentWorld() {
        PolarWorld template = world(
                chunk(0, 0, filled("minecraft:stone"), new PolarSection()),
                chunk(1, 0, filled("minecraft:stone"), new PolarSection()));
        PolarWorld current = world(
                chunk(0, 0, filled("minecraft:stone"), new PolarSection()),
                chunk(1, 0, singleBlock("minecraft:stone", "minecraft:gold_block", 100), new PolarSection()));

        PolarWorld rebuilt = GardenWorldDelta.overlay(template, GardenWorldDelta.changedChunks(template, current));

        assertTrue(GardenWorldDelta.changedChunks(current, rebuilt).chunks().isEmpty());
        assertTrue(GardenWorldDelta.changedChunks(rebuilt, current).chunks().isEmpty());
    }
}
