package net.swofty.type.skyblockgeneric.schematic;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchematicTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    private static byte[] varints(int... values) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int value : values) {
            while ((value & ~0x7F) != 0) {
                out.write((value & 0x7F) | 0x80);
                value >>>= 7;
            }
            out.write(value);
        }
        return out.toByteArray();
    }

    private static CompoundBinaryTag palette(String... names) {
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        for (int i = 0; i < names.length; i++) builder.putInt(names[i], i);
        return builder.build();
    }

    private static byte[] gzip(CompoundBinaryTag root) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryTagIO.writer().write(root, out, BinaryTagIO.Compression.GZIP);
        return out.toByteArray();
    }

    private static byte[] v2(int w, int h, int l, CompoundBinaryTag palette, byte[] data, ListBinaryTag entities) throws IOException {
        return gzip(CompoundBinaryTag.builder().putInt("Version", 2).putInt("DataVersion", 3700)
                .putShort("Width", (short) w).putShort("Height", (short) h).putShort("Length", (short) l)
                .put("Palette", palette).putByteArray("BlockData", data).put("BlockEntities", entities).build());
    }

    private static byte[] v3(int w, int h, int l, CompoundBinaryTag palette, byte[] data, ListBinaryTag entities) throws IOException {
        CompoundBinaryTag blocks = CompoundBinaryTag.builder().put("Palette", palette).putByteArray("Data", data).put("BlockEntities", entities).build();
        CompoundBinaryTag inner = CompoundBinaryTag.builder().putInt("Version", 3).putInt("DataVersion", 3700)
                .putShort("Width", (short) w).putShort("Height", (short) h).putShort("Length", (short) l)
                .put("Blocks", blocks).build();
        return gzip(CompoundBinaryTag.builder().put("Schematic", inner).build());
    }

    private static final ListBinaryTag NO_ENTITIES = ListBinaryTag.empty();

    @Test
    void readsV2WithPropertiesAndUnknownBlocks() throws IOException {
        CompoundBinaryTag palette = palette("minecraft:air", "minecraft:oak_stairs[facing=north,half=bottom]", "minecraft:not_a_block");
        Schematic schematic = SchematicReader.read(v2(2, 1, 2, palette, varints(0, 1, 2, 1), NO_ENTITIES));
        assertEquals(2, schematic.width());
        assertEquals(1, schematic.height());
        assertEquals(2, schematic.length());
        assertEquals(3700, schematic.dataVersion());
        assertTrue(schematic.blockAt(0, 0, 0).isAir());
        assertEquals("north", schematic.blockAt(1, 0, 0).properties().get("facing"));
        assertNull(schematic.blockAt(0, 0, 1));
        assertEquals(1, schematic.unknownBlocks());
        int[] visited = {0};
        schematic.forEach((_, _, _, _) -> visited[0]++);
        assertEquals(3, visited[0]);
    }

    @Test
    void readsV3AndMatchesV2() throws IOException {
        CompoundBinaryTag palette = palette("minecraft:stone", "minecraft:dirt");
        Schematic a = SchematicReader.read(v2(2, 2, 1, palette, varints(0, 1, 1, 0), NO_ENTITIES));
        Schematic b = SchematicReader.read(v3(2, 2, 1, palette, varints(0, 1, 1, 0), NO_ENTITIES));
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) assertEquals(a.blockAt(x, y, 0), b.blockAt(x, y, 0));
        }
        assertEquals(Block.DIRT, b.blockAt(1, 0, 0));
    }

    @Test
    void varintsBeyondOneByteDecode() throws IOException {
        String[] names = new String[300];
        for (int i = 0; i < names.length; i++) names[i] = i == 0 ? "minecraft:stone" : "minecraft:test_" + i;
        names[200] = "minecraft:oak_planks";
        names[299] = "minecraft:dirt";
        Schematic schematic = SchematicReader.read(v2(3, 1, 1, palette(names), varints(200, 299, 0), NO_ENTITIES));
        assertEquals(Block.OAK_PLANKS, schematic.blockAt(0, 0, 0));
        assertEquals(Block.DIRT, schematic.blockAt(1, 0, 0));
        assertEquals(Block.STONE, schematic.blockAt(2, 0, 0));
        assertEquals(2, varints(200).length);
        assertEquals(0xC8, varints(200)[0] & 0xFF);
        assertEquals(0x01, varints(200)[1]);
    }

    @Test
    void truncatedDataAndBadVersionAreRejected() throws IOException {
        CompoundBinaryTag palette = palette("minecraft:stone");
        assertThrows(IllegalArgumentException.class, () -> SchematicReader.read(v2(2, 2, 2, palette, varints(0, 0), NO_ENTITIES)));
        byte[] unsupported = gzip(CompoundBinaryTag.builder().putInt("Version", 1).build());
        assertThrows(IllegalArgumentException.class, () -> SchematicReader.read(unsupported));
        assertThrows(IllegalArgumentException.class, () -> SchematicReader.decodeVarInts(new byte[]{(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x01}, 1));
    }

    @Test
    void blockEntitiesAttachToTheirBlock() throws IOException {
        CompoundBinaryTag chest = CompoundBinaryTag.builder().putString("Id", "minecraft:chest")
                .put("Pos", IntArrayBinaryTag.intArrayBinaryTag(1, 0, 0)).putString("CustomName", "Loot").build();
        ListBinaryTag entities = ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, java.util.List.of(chest));
        Schematic schematic = SchematicReader.read(v2(2, 1, 1, palette("minecraft:stone", "minecraft:chest"), varints(0, 1), entities));
        assertEquals("Loot", schematic.blockAt(1, 0, 0).nbt().getString("CustomName"));
        assertNull(schematic.blockAt(0, 0, 0).nbt());

        CompoundBinaryTag nested = CompoundBinaryTag.builder().putString("Id", "minecraft:chest")
                .put("Pos", IntArrayBinaryTag.intArrayBinaryTag(1, 0, 0))
                .put("Data", CompoundBinaryTag.builder().putString("CustomName", "Loot3").build()).build();
        ListBinaryTag entities3 = ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, java.util.List.of(nested));
        Schematic v3 = SchematicReader.read(v3(2, 1, 1, palette("minecraft:stone", "minecraft:chest"), varints(0, 1), entities3));
        assertEquals("Loot3", v3.blockAt(1, 0, 0).nbt().getString("CustomName"));
    }

    @Test
    void rotationMovesBlocksAndFacings() {
        Block stairs = Block.OAK_STAIRS.withProperty("facing", "north");
        Schematic schematic = Schematic.of(2, 1, 3, new Block[]{Block.AIR, stairs}, new int[]{1, 0, 0, 0, 0, 0});
        Schematic turned = schematic.rotated(90);
        assertEquals(3, turned.width());
        assertEquals(2, turned.length());
        assertEquals("east", turned.blockAt(2, 0, 0).properties().get("facing"));
        assertEquals(Block.OAK_STAIRS.id(), turned.blockAt(2, 0, 0).id());
        assertEquals("south", schematic.rotated(180).blockAt(1, 0, 2).properties().get("facing"));
        assertEquals("west", schematic.rotated(270).blockAt(0, 0, 1).properties().get("facing"));
        assertEquals(schematic.blockAt(0, 0, 0), schematic.rotated(360).blockAt(0, 0, 0));
        assertEquals(schematic.blockAt(0, 0, 0), schematic.rotated(90).rotated(270).blockAt(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> schematic.rotated(45));
    }

    @Test
    void rotatesAxesSidesAndSignRotation() {
        assertEquals("z", SchematicRotation.rotate(Block.OAK_LOG.withProperty("axis", "x"), 1).properties().get("axis"));
        assertEquals("y", SchematicRotation.rotate(Block.OAK_LOG.withProperty("axis", "y"), 1).properties().get("axis"));
        Block fence = Block.OAK_FENCE.withProperty("north", "true");
        Map<String, String> turned = SchematicRotation.rotate(fence, 1).properties();
        assertEquals("true", turned.get("east"));
        assertEquals("false", turned.get("north"));
        assertEquals("4", SchematicRotation.rotate(Block.OAK_SIGN.withProperty("rotation", "0"), 1).properties().get("rotation"));
        assertEquals("2", SchematicRotation.rotate(Block.OAK_SIGN.withProperty("rotation", "14"), 1).properties().get("rotation"));
    }

    @Test
    void pastesRotatedSchematicIntoAnInstanceAndRestoresFromUndo() {
        Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.loadChunk(0, 0).join();
        instance.setBlock(5, 70, 5, Block.GOLD_BLOCK);
        Schematic schematic = Schematic.of(2, 1, 1, new Block[]{Block.AIR, Block.STONE}, new int[]{1, 0});
        SchematicPaster.Result result = SchematicPaster.paste(instance, new Vec(5, 70, 5), schematic,
                SchematicPaster.Options.defaults().withRotation(90).withUndo(true), SchematicPaster.CALLER).join();
        assertEquals(2, result.placed());
        assertEquals(1, result.width());
        assertEquals(2, result.length());
        assertEquals(Block.STONE, instance.getBlock(5, 70, 5));
        assertEquals(Block.AIR, instance.getBlock(5, 70, 6));

        SchematicPaster.paste(instance, new Vec(5, 70, 5), result.undo(), SchematicPaster.Options.defaults(), SchematicPaster.CALLER).join();
        assertEquals(Block.GOLD_BLOCK, instance.getBlock(5, 70, 5));
    }

    @Test
    void skipAirKeepsTerrainAndLargePastesSpanChunks() {
        Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.loadChunk(0, 0).join();
        instance.setBlock(1, 70, 1, Block.GOLD_BLOCK);
        Schematic schematic = Schematic.of(1, 1, 2, new Block[]{Block.AIR, Block.STONE}, new int[]{0, 1});
        SchematicPaster.Result kept = SchematicPaster.paste(instance, new Vec(1, 70, 1), schematic,
                SchematicPaster.Options.defaults().withSkipAir(true), SchematicPaster.CALLER).join();
        assertEquals(1, kept.skippedAir());
        assertEquals(Block.GOLD_BLOCK, instance.getBlock(1, 70, 1));

        int size = 90;
        Schematic big = Schematic.of(size, 1, size, new Block[]{Block.STONE}, new int[size * size]);
        SchematicPaster.Result result = SchematicPaster.paste(instance, new Vec(-40, 70, -40), big, SchematicPaster.Options.defaults(), SchematicPaster.CALLER).join();
        assertEquals(size * size, result.placed());
        assertEquals(Block.STONE, instance.getBlock(-40, 70, -40));
        assertEquals(Block.STONE, instance.getBlock(49, 70, 49));
    }
}
