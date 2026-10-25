import net.hollowcube.polar.*;
import java.nio.file.*;

class MergeGalatea {
    static int nonAir(PolarChunk chunk) {
        int count = 0;
        for (PolarSection section : chunk.sections()) {
            String[] palette = section.blockPalette();
            int[] data = section.blockData();
            for (int i = 0; i < 4096; i++) {
                String name = palette[data == null ? 0 : data[i]];
                if (!name.startsWith("minecraft:air") && !name.startsWith("air")) count++;
            }
        }
        return count;
    }

    public static void main(String[] args) throws Exception {
        PolarWorld base = PolarReader.read(Files.readAllBytes(Path.of(args[0])));
        int replaced = 0, kept = 0;
        for (int i = 2; i < args.length; i++) {
            PolarWorld fresh = PolarReader.read(Files.readAllBytes(Path.of(args[i])));
            if (base.minSection() != fresh.minSection() || base.maxSection() != fresh.maxSection())
                throw new IllegalStateException("section ranges differ for " + args[i]);
            for (PolarChunk chunk : fresh.chunks()) {
                PolarChunk existing = base.chunkAt(chunk.x(), chunk.z());
                if (existing != null && nonAir(chunk) < nonAir(existing) * 0.8) {
                    kept++;
                    continue;
                }
                base.updateChunkAt(chunk.x(), chunk.z(), chunk);
                replaced++;
            }
        }
        Files.write(Path.of(args[1]), PolarWriter.write(base));
        System.out.println("replaced " + replaced + " keptOld " + kept + " total " + base.chunks().size());
    }
}
