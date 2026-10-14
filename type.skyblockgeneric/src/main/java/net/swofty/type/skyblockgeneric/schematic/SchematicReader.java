package net.swofty.type.skyblockgeneric.schematic;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.minestom.server.instance.block.Block;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class SchematicReader {
    private static final long SIZE_LIMIT = 256L * 1024 * 1024;

    private SchematicReader() {
    }

    public static Schematic read(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return read(in);
        }
    }

    public static Schematic read(byte[] bytes) throws IOException {
        return read(new ByteArrayInputStream(bytes));
    }

    public static Schematic read(InputStream in) throws IOException {
        return parse(BinaryTagIO.reader(SIZE_LIMIT).read(in, BinaryTagIO.Compression.GZIP));
    }

    public static Schematic parse(CompoundBinaryTag file) {
        boolean nested = file.get("Schematic") instanceof CompoundBinaryTag;
        CompoundBinaryTag root = nested ? file.getCompound("Schematic") : file;
        int version = intOf(root, "Version", nested ? 3 : 2);
        if (version < 2 || version > 3) throw new IllegalArgumentException("Unsupported schematic version " + version);
        int width = dimension(root, "Width");
        int height = dimension(root, "Height");
        int length = dimension(root, "Length");
        long volume = (long) width * height * length;
        if (volume <= 0 || volume > Integer.MAX_VALUE) throw new IllegalArgumentException("Invalid schematic size " + width + "x" + height + "x" + length);

        CompoundBinaryTag blocks = nested ? root.getCompound("Blocks") : root;
        CompoundBinaryTag paletteTag = blocks.getCompound("Palette");
        byte[] data = bytes(blocks, nested ? "Data" : "BlockData");
        if (paletteTag.size() == 0 || data.length == 0) throw new IllegalArgumentException("Schematic has no palette or block data");

        String[] names = new String[paletteTag.size()];
        for (String key : paletteTag.keySet()) {
            int id = intOf(paletteTag, key, -1);
            if (id < 0) throw new IllegalArgumentException("Bad palette id for " + key);
            if (id >= names.length) names = java.util.Arrays.copyOf(names, id + 1);
            names[id] = key;
        }
        Block[] palette = new Block[names.length];
        for (int i = 0; i < names.length; i++) palette[i] = names[i] == null ? null : Block.fromState(names[i]);

        int[] indices = decodeVarInts(data, (int) volume);
        for (int index : indices) {
            if (index >= palette.length) throw new IllegalArgumentException("Block data references palette entry " + index + " of " + palette.length);
        }
        return new Schematic(width, height, length, intOf(root, "DataVersion", 0), palette, indices,
                blockEntities(blocks, nested, width, length));
    }

    static int[] decodeVarInts(byte[] data, int count) {
        int[] values = new int[count];
        int position = 0;
        for (int i = 0; i < count; i++) {
            int value = 0;
            int shift = 0;
            while (true) {
                if (position >= data.length) throw new IllegalArgumentException("Block data ended after " + i + " of " + count + " blocks");
                int next = data[position++] & 0xFF;
                value |= (next & 0x7F) << shift;
                if ((next & 0x80) == 0) break;
                shift += 7;
                if (shift > 28) throw new IllegalArgumentException("Varint too long at block " + i);
            }
            values[i] = value;
        }
        return values;
    }

    private static Map<Integer, CompoundBinaryTag> blockEntities(CompoundBinaryTag blocks, boolean nested, int width, int length) {
        Map<Integer, CompoundBinaryTag> result = new HashMap<>();
        ListBinaryTag list = blocks.getList("BlockEntities");
        for (BinaryTag entry : list) {
            if (!(entry instanceof CompoundBinaryTag compound) || !(compound.get("Pos") instanceof IntArrayBinaryTag pos)) continue;
            int[] at = pos.value();
            if (at.length != 3) continue;
            CompoundBinaryTag payload = nested && compound.get("Data") instanceof CompoundBinaryTag data ? data : compound;
            CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
            for (String key : payload.keySet()) {
                if (key.equals("Id") || key.equals("Pos") || (!nested && key.equals("Extra"))) continue;
                builder.put(key, payload.get(key));
            }
            CompoundBinaryTag nbt = builder.build();
            if (nbt.size() == 0) continue;
            result.put(Schematic.index(at[0], at[1], at[2], width, length), nbt);
        }
        return result;
    }

    private static byte[] bytes(CompoundBinaryTag tag, String key) {
        return tag.get(key) instanceof ByteArrayBinaryTag array ? array.value() : new byte[0];
    }

    private static int dimension(CompoundBinaryTag tag, String key) {
        BinaryTag value = tag.get(key);
        if (value instanceof ShortBinaryTag s) return s.value() & 0xFFFF;
        if (value instanceof NumberBinaryTag n) return n.intValue();
        throw new IllegalArgumentException("Missing " + key);
    }

    private static int intOf(CompoundBinaryTag tag, String key, int fallback) {
        return tag.get(key) instanceof NumberBinaryTag n ? n.intValue() : fallback;
    }
}
