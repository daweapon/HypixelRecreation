import net.hollowcube.polar.*;
import java.nio.file.*;

class RestoreChunks {
    public static void main(String[] args) throws Exception {
        PolarWorld base = PolarReader.read(Files.readAllBytes(Path.of(args[0])));
        PolarWorld source = PolarReader.read(Files.readAllBytes(Path.of(args[1])));
        for (int i = 3; i + 1 < args.length; i += 2) {
            int x = Integer.parseInt(args[i]);
            int z = Integer.parseInt(args[i + 1]);
            base.updateChunkAt(x, z, source.chunkAt(x, z));
        }
        Files.write(Path.of(args[2]), PolarWriter.write(base));
    }
}
