import net.hollowcube.polar.*;
import java.nio.file.*;
import java.util.*;

class ScanEnd {
    public static void main(String[] args) throws Exception {
        PolarWorld w = PolarReader.read(Files.readAllBytes(Path.of(args[0])));
        Set<String> interesting = new HashSet<>(Arrays.asList(args).subList(1, args.length));
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        Map<String, Integer> counts = new TreeMap<>();
        Map<String, List<String>> hits = new TreeMap<>();
        int minSection = w.minSection();
        for (PolarChunk c : w.chunks()) {
            minX = Math.min(minX, c.x() * 16); maxX = Math.max(maxX, c.x() * 16 + 15);
            minZ = Math.min(minZ, c.z() * 16); maxZ = Math.max(maxZ, c.z() * 16 + 15);
            PolarSection[] secs = c.sections();
            for (int s = 0; s < secs.length; s++) {
                PolarSection sec = secs[s];
                if (sec.isEmpty()) continue;
                String[] pal = sec.blockPalette();
                int[] data = sec.blockData();
                for (int i = 0; i < 4096; i++) {
                    String b = pal[pal.length == 1 ? 0 : data[i]];
                    int bare = b.indexOf('[');
                    String name = bare < 0 ? b : b.substring(0, bare);
                    counts.merge(name, 1, Integer::sum);
                    if (interesting.contains(name)) {
                        int x = c.x() * 16 + (i & 15), z = c.z() * 16 + ((i >> 4) & 15), y = (s + minSection) * 16 + (i >> 8);
                        hits.computeIfAbsent(name, k -> new ArrayList<>()).add(x + " " + y + " " + z + " " + b);
                    }
                }
            }
        }
        System.out.println("chunks " + w.chunks().size() + " x " + minX + ".." + maxX + " z " + minZ + ".." + maxZ + " minSection " + minSection);
        counts.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).limit(25).forEach(e -> System.out.println(e.getKey() + " " + e.getValue()));
        hits.forEach((k, v) -> { System.out.println("== " + k + " " + v.size()); v.stream().limit(60).forEach(System.out::println); });
    }
}
