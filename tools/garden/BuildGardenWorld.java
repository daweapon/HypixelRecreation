import net.hollowcube.polar.PolarChunk;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BuildGardenWorld {
    private static final int CELL_CHUNKS = 6;
    private static final int ORIGIN_CHUNK = -3;
    private static final int RADIUS = 2;
    private static final Set<String> CAPTURED = Set.of("0,0", "0,-1", "-1,0", "1,0", "0,1", "1,-1");
    private static final int[][] WILDERNESS_SOURCES = {{0, -1}, {-1, 0}, {1, 0}, {0, 1}};

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: <plain-garden.polar> <output.polar>");
        PolarWorld source = PolarReader.read(Files.readAllBytes(Path.of(args[0])));
        List<PolarChunk> out = new ArrayList<>();
        int copied = 0;
        int tiled = 0;
        for (int i = -RADIUS; i <= RADIUS; i++) {
            for (int j = -RADIUS; j <= RADIUS; j++) {
                int[] from = CAPTURED.contains(i + "," + j) ? new int[]{i, j} : WILDERNESS_SOURCES[Math.floorMod(i * 7 + j * 13, WILDERNESS_SOURCES.length)];
                boolean tile = from[0] != i || from[1] != j;
                for (int a = 0; a < CELL_CHUNKS; a++) {
                    for (int b = 0; b < CELL_CHUNKS; b++) {
                        PolarChunk chunk = source.chunkAt(ORIGIN_CHUNK + CELL_CHUNKS * from[0] + a, ORIGIN_CHUNK + CELL_CHUNKS * from[1] + b);
                        if (chunk == null) {
                            System.out.println("missing source chunk for cell " + i + "," + j + " from " + from[0] + "," + from[1]);
                            continue;
                        }
                        out.add(new PolarChunk(ORIGIN_CHUNK + CELL_CHUNKS * i + a, ORIGIN_CHUNK + CELL_CHUNKS * j + b,
                                chunk.sections(), chunk.blockEntities(), chunk.heightmaps(), chunk.userData()));
                        if (tile) tiled++; else copied++;
                    }
                }
            }
        }
        PolarWorld world = new PolarWorld(source.version(), source.dataVersion(), source.compression(),
                source.minSection(), source.maxSection(), source.userData(), out);
        Files.write(Path.of(args[1]), PolarWriter.write(world));
        System.out.println("wrote " + out.size() + " chunks (" + copied + " captured, " + tiled + " tiled) to " + args[1]);
    }
}
