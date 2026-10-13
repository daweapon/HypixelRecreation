package net.swofty.type.skyblockgeneric.schematic;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Schematic {
    @FunctionalInterface
    public interface BlockVisitor {
        void visit(int x, int y, int z, Block block);
    }

    private final int width;
    private final int height;
    private final int length;
    private final int dataVersion;
    private final Block[] palette;
    private final int[] indices;
    private final Map<Integer, CompoundBinaryTag> blockEntities;

    Schematic(int width, int height, int length, int dataVersion, Block[] palette, int[] indices,
              Map<Integer, CompoundBinaryTag> blockEntities) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.dataVersion = dataVersion;
        this.palette = palette;
        this.indices = indices;
        this.blockEntities = blockEntities;
    }

    public static Schematic of(int width, int height, int length, Block[] palette, int[] indices) {
        if ((long) width * height * length != indices.length) {
            throw new IllegalArgumentException("Expected " + (long) width * height * length + " blocks but got " + indices.length);
        }
        return new Schematic(width, height, length, 0, palette.clone(), indices.clone(), Map.of());
    }

    public static Schematic capture(Block.Getter getter, Point min, int minWorldY, int maxWorldY, int width, int height, int length) {
        Map<Block, Integer> ids = new LinkedHashMap<>();
        int[] indices = new int[width * height * length];
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    int worldY = min.blockY() + y;
                    Block block = worldY < minWorldY || worldY > maxWorldY
                            ? Block.AIR
                            : getter.getBlock(min.blockX() + x, worldY, min.blockZ() + z);
                    indices[index(x, y, z, width, length)] = ids.computeIfAbsent(block, _ -> ids.size());
                }
            }
        }
        return new Schematic(width, height, length, 0, ids.keySet().toArray(new Block[0]), indices, Map.of());
    }

    static int index(int x, int y, int z, int width, int length) {
        return x + z * width + y * width * length;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int length() {
        return length;
    }

    public int dataVersion() {
        return dataVersion;
    }

    public long volume() {
        return (long) width * height * length;
    }

    public int paletteSize() {
        return palette.length;
    }

    public Block blockAt(int x, int y, int z) {
        int index = index(x, y, z, width, length);
        Block block = palette[indices[index]];
        if (block == null) return null;
        CompoundBinaryTag nbt = blockEntities.get(index);
        return nbt == null ? block : block.withNbt(nbt);
    }

    public int unknownBlocks() {
        int count = 0;
        for (int paletteIndex : indices) if (palette[paletteIndex] == null) count++;
        return count;
    }

    public void forEach(BlockVisitor visitor) {
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    Block block = blockAt(x, y, z);
                    if (block != null) visitor.visit(x, y, z, block);
                }
            }
        }
    }

    public Schematic rotated(int degrees) {
        int turns = SchematicRotation.quarterTurns(degrees);
        if (turns == 0) return this;
        boolean swap = turns % 2 == 1;
        int newWidth = swap ? length : width;
        int newLength = swap ? width : length;
        Block[] newPalette = new Block[palette.length];
        for (int i = 0; i < palette.length; i++) {
            newPalette[i] = palette[i] == null ? null : SchematicRotation.rotate(palette[i], turns);
        }
        int[] newIndices = new int[indices.length];
        Map<Integer, CompoundBinaryTag> newEntities = new HashMap<>();
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    int nx = SchematicRotation.rotatedX(x, z, width, length, turns);
                    int nz = SchematicRotation.rotatedZ(x, z, width, length, turns);
                    int from = index(x, y, z, width, length);
                    int to = index(nx, y, nz, newWidth, newLength);
                    newIndices[to] = indices[from];
                    CompoundBinaryTag nbt = blockEntities.get(from);
                    if (nbt != null) newEntities.put(to, nbt);
                }
            }
        }
        return new Schematic(newWidth, height, newLength, dataVersion, newPalette, newIndices, newEntities);
    }
}
