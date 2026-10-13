package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public final class GreenhousePreset {
    private static final Path FILE = Path.of("./configuration/skyblock/garden/greenhouse_plot.bin");
    private static GreenhousePreset instance;

    private final int size;
    private final int minY;
    private final int maxY;
    private final Block[] palette;
    private final int[][][] runs;

    private GreenhousePreset(DataInputStream in) throws IOException {
        size = in.readInt();
        minY = in.readInt();
        maxY = in.readInt();
        int paletteSize = in.readInt();
        palette = new Block[paletteSize];
        for (int i = 0; i < paletteSize; i++) {
            String state = in.readUTF();
            Block block = Block.fromState(state);
            palette[i] = block == null ? Block.AIR : block;
        }
        runs = new int[size * size][][];
        for (int i = 0; i < runs.length; i++) {
            int count = in.readUnsignedShort();
            runs[i] = new int[count][2];
            for (int r = 0; r < count; r++) {
                runs[i][r][0] = in.readUnsignedShort();
                runs[i][r][1] = in.readUnsignedShort();
            }
        }
    }

    public static synchronized GreenhousePreset get() {
        if (instance == null) {
            try (InputStream stream = new GZIPInputStream(Files.newInputStream(FILE)); DataInputStream in = new DataInputStream(stream)) {
                instance = new GreenhousePreset(in);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return instance;
    }

    public int size() {
        return size;
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    public Block blockAt(int lx, int lz, int y) {
        int offset = y - minY;
        if (offset < 0 || y > maxY) return Block.AIR;
        for (int[] run : runs[lx * size + lz]) {
            if (offset < run[1]) return palette[run[0]];
            offset -= run[1];
        }
        return Block.AIR;
    }

    public void paste(Instance instance, int plot, Runnable done) {
        int[] origin = GardenLayout.plotOrigin(plot);
        Thread.startVirtualThread(() -> {
            for (int cx = origin[0] >> 4; cx <= (origin[0] + size - 1) >> 4; cx++) {
                for (int cz = origin[1] >> 4; cz <= (origin[1] + size - 1) >> 4; cz++) {
                    instance.loadChunk(cx, cz).join();
                }
            }
            AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
            for (int lx = 0; lx < size; lx++) {
                for (int lz = 0; lz < size; lz++) {
                    int x = origin[0] + lx;
                    int z = origin[1] + lz;
                    boolean bed = lx >= GreenhouseLayout.BED_X && lx < GreenhouseLayout.BED_X + GreenhouseData.SIZE
                            && lz >= GreenhouseLayout.BED_Z && lz < GreenhouseLayout.BED_Z + GreenhouseData.SIZE;
                    for (int y = minY; y <= maxY; y++) {
                        Block block = blockAt(lx, lz, y);
                        if (bed && y == GreenhouseLayout.FLOOR_Y) block = Block.FARMLAND.withProperty("moisture", "0");
                        if (bed && y == GreenhouseLayout.CROP_Y) block = Block.AIR;
                        if (!instance.getBlock(x, y, z).compare(block)) batch.setBlock(x, y, z, block);
                    }
                }
            }
            batch.apply(instance, _ -> done.run());
        });
    }
}
