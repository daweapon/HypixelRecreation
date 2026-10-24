import net.hollowcube.polar.*;
import java.nio.file.*;

class MergeDen {
    public static void main(String[] args) throws Exception {
        PolarWorld base = PolarReader.read(Files.readAllBytes(Path.of(args[0])));
        PolarWorld fresh = PolarReader.read(Files.readAllBytes(Path.of(args[1])));
        System.out.println("base sections " + base.minSection() + ".." + base.maxSection() + " fresh " + fresh.minSection() + ".." + fresh.maxSection());
        if (base.minSection() != fresh.minSection() || base.maxSection() != fresh.maxSection()) throw new IllegalStateException("section ranges differ");
        int replaced = 0, added = 0;
        for (PolarChunk chunk : fresh.chunks()) {
            if (base.chunkAt(chunk.x(), chunk.z()) == null) added++; else replaced++;
            base.updateChunkAt(chunk.x(), chunk.z(), chunk);
        }
        Files.write(Path.of(args[2]), PolarWriter.write(base));
        System.out.println("replaced " + replaced + " added " + added + " total " + base.chunks().size());
    }
}
