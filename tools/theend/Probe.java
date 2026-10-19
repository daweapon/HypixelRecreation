import net.hollowcube.polar.*;
import java.nio.file.*;
import java.util.*;

class Probe {
    static PolarWorld w;
    static String block(int x, int y, int z) {
        PolarChunk c = w.chunkAt(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        if (c == null) return "none";
        int s = Math.floorDiv(y, 16) - w.minSection();
        if (s < 0 || s >= c.sections().length) return "minecraft:air";
        PolarSection sec = c.sections()[s];
        if (sec.isEmpty()) return "minecraft:air";
        String[] pal = sec.blockPalette();
        int i = ((y & 15) << 8) | ((z & 15) << 4) | (x & 15);
        String b = pal[pal.length == 1 ? 0 : sec.blockData()[i]];
        int k = b.indexOf('[');
        return k < 0 ? b : b.substring(0, k);
    }
    static boolean isAir(String b) { return b.equals("air") || b.equals("minecraft:air") || b.equals("minecraft:cave_air") || b.equals("none"); }
    static int top(int x, int z, int from, int to) {
        for (int y = to; y >= from; y--) if (!isAir(block(x, y, z))) return y;
        return Integer.MIN_VALUE;
    }
    public static void main(String[] a) throws Exception {
        w = PolarReader.read(Files.readAllBytes(Path.of(a[1])));
        if (a[0].equals("col")) {
            int x = Integer.parseInt(a[2]), z = Integer.parseInt(a[3]);
            String prev = null; int start = 0;
            for (int y = Integer.parseInt(a[4]); y <= Integer.parseInt(a[5]); y++) {
                String b = block(x, y, z);
                if (!b.equals(prev)) { if (prev != null) System.out.println(start + ".." + (y - 1) + " " + prev); prev = b; start = y; }
            }
            System.out.println(start + ".. " + prev);
        } else if (a[0].equals("map")) {
            int x0 = Integer.parseInt(a[2]), z0 = Integer.parseInt(a[3]), x1 = Integer.parseInt(a[4]), z1 = Integer.parseInt(a[5]), st = Integer.parseInt(a[6]);
            int lo = Integer.parseInt(a[7]), hi = Integer.parseInt(a[8]);
            for (int z = z0; z <= z1; z += st) {
                StringBuilder sb = new StringBuilder(String.format("%5d ", z));
                for (int x = x0; x <= x1; x += st) { int t = top(x, z, lo, hi); sb.append(t == Integer.MIN_VALUE ? "  . " : String.format("%3d ", t)); }
                System.out.println(sb);
            }
        } else if (a[0].equals("floors")) {
            int x = Integer.parseInt(a[2]), z = Integer.parseInt(a[3]);
            for (int y = Integer.parseInt(a[4]); y <= Integer.parseInt(a[5]); y++)
                if (!isAir(block(x, y, z)) && isAir(block(x, y + 1, z)) && isAir(block(x, y + 2, z)))
                    System.out.println("standable y=" + (y + 1) + " on " + block(x, y, z));
        } else if (a[0].equals("spikes")) {
            int x0 = Integer.parseInt(a[2]), z0 = Integer.parseInt(a[3]), x1 = Integer.parseInt(a[4]), z1 = Integer.parseInt(a[5]);
            int minRise = Integer.parseInt(a[6]);
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) {
                int t = top(x, z, 0, 110);
                if (t == Integer.MIN_VALUE || !block(x, t, z).equals("minecraft:obsidian")) continue;
                int lowest = Integer.MAX_VALUE;
                for (int[] d : new int[][]{{6,0},{-6,0},{0,6},{0,-6}}) { int n = top(x + d[0], z + d[1], 0, 110); lowest = Math.min(lowest, n == Integer.MIN_VALUE ? -1 : n); }
                boolean peak = true;
                for (int dx = -2; dx <= 2 && peak; dx++) for (int dz = -2; dz <= 2; dz++) { int n = top(x + dx, z + dz, 0, 110); if (n > t) { peak = false; break; } }
                if (peak && t - lowest >= minRise) System.out.println(x + " " + (t + 1) + " " + z + " rise " + (t - lowest));
            }
        } else if (a[0].equals("ring")) {
            double cx = Double.parseDouble(a[2]), cz = Double.parseDouble(a[3]);
            for (int i = 0; i < 10; i++) {
                double ang = Math.PI * 2 * i / 10;
                StringBuilder sb = new StringBuilder("angle " + i + ":");
                for (int r = 18; r <= 30; r += 4) {
                    int x = (int) Math.round(cx + Math.cos(ang) * r), z = (int) Math.round(cz + Math.sin(ang) * r);
                    int t = top(x, z, 0, 108);
                    sb.append(String.format("  r%d (%d,%d) top=%s %s", r, x, z, t, t == Integer.MIN_VALUE ? "-" : block(x, t, z).replace("minecraft:", "")));
                }
                System.out.println(sb);
            }
        } else if (a[0].equals("spots")) {
            double cx = Double.parseDouble(a[2]), cz = Double.parseDouble(a[3]);
            for (int i = 0; i < 16; i++) {
                double ang = Math.PI * 2 * i / 16;
                for (int r : new int[]{14, 10, 18, 8, 22}) {
                    int x = (int) Math.round(cx + Math.cos(ang) * r), z = (int) Math.round(cz + Math.sin(ang) * r);
                    boolean ok = true;
                    int fy = -1;
                    for (int y = 3; y <= 14 && fy < 0; y++) if (!isAir(block(x, y, z)) && isAir(block(x, y + 1, z)) && isAir(block(x, y + 2, z)) && isAir(block(x, y + 3, z))) fy = y + 1;
                    if (fy < 0) continue;
                    System.out.println("new Pos(" + x + ".5, " + fy + ", " + z + ".5),");
                    break;
                }
            }
        } else if (a[0].equals("find")) {
            Set<String> names = new HashSet<>(Arrays.asList(a).subList(7, a.length));
            int x0 = Integer.parseInt(a[2]), z0 = Integer.parseInt(a[3]), x1 = Integer.parseInt(a[4]), z1 = Integer.parseInt(a[5]);
            int ylo = Integer.parseInt(a[6].split(":")[0]), yhi = Integer.parseInt(a[6].split(":")[1]);
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) for (int y = ylo; y <= yhi; y++) { String b = block(x, y, z); if (names.contains(b)) System.out.println(x + " " + y + " " + z + " " + b); }
        }
    }
}
