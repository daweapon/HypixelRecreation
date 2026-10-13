import net.hollowcube.polar.PolarChunk;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarSection;
import net.hollowcube.polar.PolarWorld;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class ExtractGreenhousePreset {
    private static final int ORIGIN_X = 144;
    private static final int ORIGIN_Z = 144;
    private static final int SIZE = 96;
    private static final int MIN_Y = 70;
    private static final int MAX_Y = 108;

    public static void main(String[] args) throws Exception {
        PolarWorld world = PolarReader.read(Files.readAllBytes(Path.of(args[0])));
        Map<String, Integer> palette = new LinkedHashMap<>();
        palette.put("minecraft:air", 0);
        int[][][] blocks = new int[SIZE][SIZE][MAX_Y - MIN_Y + 1];
        for (int lx = 0; lx < SIZE; lx++) {
            for (int lz = 0; lz < SIZE; lz++) {
                int x = ORIGIN_X + lx;
                int z = ORIGIN_Z + lz;
                PolarChunk chunk = world.chunkAt(x >> 4, z >> 4);
                if (chunk == null) throw new IllegalStateException("missing chunk " + (x >> 4) + "," + (z >> 4));
                for (int y = MIN_Y; y <= MAX_Y; y++) {
                    int sectionIndex = (y >> 4) - world.minSection();
                    PolarSection section = chunk.sections()[sectionIndex];
                    String state = "minecraft:air";
                    if (!section.isEmpty()) {
                        String[] blockPalette = section.blockPalette();
                        int[] data = section.blockData();
                        int index = blockPalette.length == 1 || data == null ? 0 : data[((y & 15) << 8) | ((z & 15) << 4) | (x & 15)];
                        state = blockPalette[index];
                    }
                    blocks[lx][lz][y - MIN_Y] = palette.computeIfAbsent(state, key -> palette.size());
                }
            }
        }
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(args[1])))) {
            out.writeInt(SIZE);
            out.writeInt(MIN_Y);
            out.writeInt(MAX_Y);
            List<String> states = new ArrayList<>(palette.keySet());
            out.writeInt(states.size());
            for (String state : states) out.writeUTF(state);
            for (int lx = 0; lx < SIZE; lx++) {
                for (int lz = 0; lz < SIZE; lz++) {
                    int[] column = blocks[lx][lz];
                    List<int[]> runs = new ArrayList<>();
                    for (int value : column) {
                        if (!runs.isEmpty() && runs.getLast()[0] == value) runs.getLast()[1]++;
                        else runs.add(new int[]{value, 1});
                    }
                    out.writeShort(runs.size());
                    for (int[] run : runs) {
                        out.writeShort(run[0]);
                        out.writeShort(run[1]);
                    }
                }
            }
        }
        System.out.println("palette " + palette.size() + " minSection " + world.minSection());
    }
}
