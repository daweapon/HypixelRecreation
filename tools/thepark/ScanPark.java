import net.hollowcube.polar.*;
import java.nio.file.*;
import java.util.*;

class ScanPark {
    static PolarWorld w;
    static Map<Long, PolarChunk> chunks = new HashMap<>();

    static String block(int x, int y, int z) {
        PolarChunk c = chunks.get(key(Math.floorDiv(x, 16), Math.floorDiv(z, 16)));
        if (c == null) return null;
        int s = Math.floorDiv(y, 16) - w.minSection();
        if (s < 0 || s >= c.sections().length) return "minecraft:air";
        PolarSection sec = c.sections()[s];
        if (sec.isEmpty()) return "minecraft:air";
        String[] pal = sec.blockPalette();
        int[] data = sec.blockData();
        int i = (Math.floorMod(y, 16) << 8) | (Math.floorMod(z, 16) << 4) | Math.floorMod(x, 16);
        String b = pal[pal.length == 1 ? 0 : data[i]];
        int bare = b.indexOf('[');
        return bare < 0 ? b : b.substring(0, bare);
    }

    static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }

    static boolean air(String b) { return b == null || b.equals("minecraft:air") || b.equals("air") || b.equals("minecraft:cave_air"); }

    public static void main(String[] args) throws Exception {
        w = PolarReader.read(Files.readAllBytes(Path.of(args[0])));
        for (PolarChunk c : w.chunks()) chunks.put(key(c.x(), c.z()), c);
        String mode = args[1];
        if (mode.equals("find")) {
            int x0 = Integer.parseInt(args[2]), x1 = Integer.parseInt(args[3]), y0 = Integer.parseInt(args[4]), y1 = Integer.parseInt(args[5]), z0 = Integer.parseInt(args[6]), z1 = Integer.parseInt(args[7]);
            Set<String> names = new HashSet<>(Arrays.asList(args).subList(8, args.length));
            int count = 0;
            for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) {
                String b = block(x, y, z);
                if (b != null && names.contains(b)) { System.out.println(b + " " + x + " " + y + " " + z); count++; }
            }
            System.out.println("total " + count);
        } else if (mode.equals("flood")) {
            int sx = Integer.parseInt(args[2]), sy = Integer.parseInt(args[3]), sz = Integer.parseInt(args[4]);
            int limit = Integer.parseInt(args[5]);
            Deque<int[]> queue = new ArrayDeque<>();
            Set<Long> seen = new HashSet<>();
            queue.add(new int[]{sx, sy, sz});
            seen.add(pack(sx, sy, sz));
            int minX = sx, maxX = sx, minY = sy, maxY = sy, minZ = sz, maxZ = sz, n = 0;
            boolean escaped = false;
            int[][] d = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
            while (!queue.isEmpty() && n < limit) {
                int[] p = queue.poll();
                n++;
                minX = Math.min(minX, p[0]); maxX = Math.max(maxX, p[0]); minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]); minZ = Math.min(minZ, p[2]); maxZ = Math.max(maxZ, p[2]);
                for (int[] o : d) {
                    int x = p[0] + o[0], y = p[1] + o[1], z = p[2] + o[2];
                    if (!seen.add(pack(x, y, z))) continue;
                    String b = block(x, y, z);
                    if (b == null) { escaped = true; continue; }
                    if (air(b)) queue.add(new int[]{x, y, z});
                }
            }
            System.out.println("filled " + n + (queue.isEmpty() ? " (closed)" : " (LIMIT)") + " bbox x " + minX + ".." + maxX + " y " + minY + ".." + maxY + " z " + minZ + ".." + maxZ + " escapedToUnloaded " + escaped);
        } else if (mode.equals("slicez")) {
            int z = Integer.parseInt(args[2]), x0 = Integer.parseInt(args[3]), x1 = Integer.parseInt(args[4]), y0 = Integer.parseInt(args[5]), y1 = Integer.parseInt(args[6]);
            for (int y = y1; y >= y0; y--) {
                StringBuilder sb = new StringBuilder(String.format("%4d ", y));
                for (int x = x0; x <= x1; x++) { String b = block(x, y, z); sb.append(b == null ? '?' : air(b) ? '.' : '#'); }
                System.out.println(sb);
            }
        } else if (mode.equals("slicey")) {
            int y = Integer.parseInt(args[2]), x0 = Integer.parseInt(args[3]), x1 = Integer.parseInt(args[4]), z0 = Integer.parseInt(args[5]), z1 = Integer.parseInt(args[6]);
            for (int z = z0; z <= z1; z++) {
                StringBuilder sb = new StringBuilder(String.format("%5d ", z));
                for (int x = x0; x <= x1; x++) { String b = block(x, y, z); sb.append(b == null ? '?' : air(b) ? '.' : '#'); }
                System.out.println(sb);
            }
        } else if (mode.equals("column")) {
            int x = Integer.parseInt(args[2]), z = Integer.parseInt(args[3]);
            for (int y = 200; y >= 0; y--) { String b = block(x, y, z); if (!air(b)) System.out.println(y + " " + b); }
        }
    }

    static long pack(int x, int y, int z) { return ((long) (x + 100000) << 40) | ((long) (y + 1000) << 20) | (z + 100000); }
}
