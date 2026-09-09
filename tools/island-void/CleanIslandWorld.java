import net.hollowcube.polar.*;
import java.nio.file.*;
import java.util.*;

public class CleanIslandWorld {
    private static final Map<String, String> STATES = new HashMap<>();
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Usage: <original-template> <input-world> <output-world>");
        var template = PolarReader.read(Files.readAllBytes(Path.of(args[0])));
        var world = PolarReader.read(Files.readAllBytes(Path.of(args[1])));
        var kept = new ArrayList<PolarChunk>();
        int removed = 0;
        var modified = new ArrayList<String>();
        for (var chunk : world.chunks()) {
            if (starter(chunk.x(), chunk.z())) { kept.add(chunk); continue; }
            var original = template.chunkAt(chunk.x(), chunk.z());
            if (original != null && sameBlocks(chunk, world, original, template)) {
                removed++;
            } else {
                kept.add(chunk);
                if (original != null) modified.add(chunk.x() + "," + chunk.z());
            }
        }
        if (!modified.isEmpty()) throw new IllegalStateException("Modified terrain chunks require review: " + modified);
        var clean = new PolarWorld(world.version(), world.dataVersion(), world.compression(),
                world.minSection(), world.maxSection(), world.userData(), kept);
        Files.write(Path.of(args[2]), PolarWriter.write(clean));
        System.out.println(Path.of(args[1]).getFileName() + ": removed " + removed + " inherited chunks, preserved " + kept.size());
    }

    private static boolean starter(int x, int z) {
        return x >= -1 && x <= 0 && z >= 0 && z <= 2;
    }

    private static boolean sameBlocks(PolarChunk a, PolarWorld aw, PolarChunk b, PolarWorld bw) {
        if (!a.blockEntities().equals(b.blockEntities())) return false;
        if (!Arrays.equals(a.userData(), b.userData())) return false;
        for (int y = Math.min(aw.minSection(), bw.minSection()); y <= Math.max(aw.maxSection(), bw.maxSection()); y++) {
            var as = section(a, aw, y); var bs = section(b, bw, y);
            if (as != null && bs != null && Arrays.equals(as.blockPalette(), bs.blockPalette())
                    && Arrays.equals(as.blockData(), bs.blockData())) continue;
            for (int i = 0; i < 4096; i++) if (!block(as, i).equals(block(bs, i))) return false;
        }
        return true;
    }

    private static PolarSection section(PolarChunk c, PolarWorld w, int y) {
        return y < w.minSection() || y > w.maxSection() ? null : c.sections()[y - w.minSection()];
    }

    private static String block(PolarSection section, int index) {
        if (section == null || section.isEmpty()) return "air";
        var data = section.blockData();
        String block = section.blockPalette()[data == null ? 0 : data[index]];
        if (block.startsWith("minecraft:")) block = block.substring(10);
        return STATES.computeIfAbsent(block, state -> {
            if (state.equals("air") || state.equals("cave_air") || state.equals("void_air")) return "air";
            var parsed = net.minestom.server.instance.block.Block.fromState(state);
            if (parsed == null) throw new IllegalArgumentException("Unknown block " + state);
            return Integer.toString(parsed.stateId());
        });
    }
}
