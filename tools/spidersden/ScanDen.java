import net.hollowcube.polar.*;
import java.nio.file.*;
import java.util.*;

class ScanDen {
    static PolarWorld w;
    static Map<Long, PolarChunk> chunks = new HashMap<>();

    static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }

    static String block(int x, int y, int z) {
        return blockIn(w, chunks, x, y, z);
    }

    static String blockIn(PolarWorld w, Map<Long, PolarChunk> chunks, int x, int y, int z) {
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

    static boolean air(String b) {
        return b == null || b.equals("air") || b.equals("minecraft:air") || b.equals("minecraft:cave_air") || b.equals("minecraft:void_air");
    }

    static boolean solid(String b) {
        return !air(b) && !b.contains("water") && !b.contains("lava") && !b.contains("cobweb") && !b.contains("grass") && !b.contains("fern")
                && !b.contains("flower") && !b.contains("leaves") && !b.contains("vine") && !b.contains("torch") && !b.contains("rail");
    }

    static List<int[]> standable(int x0, int x1, int z0, int z1, int y0, int y1, int step) {
        List<int[]> out = new ArrayList<>();
        for (int x = x0; x <= x1; x += step) for (int z = z0; z <= z1; z += step) {
            for (int y = y1; y >= y0; y--) {
                if (solid(block(x, y, z)) && air(block(x, y + 1, z)) && air(block(x, y + 2, z))) { out.add(new int[]{x, y + 1, z}); }
            }
        }
        return out;
    }

    public static void main(String[] args) throws Exception {
        w = PolarReader.read(Files.readAllBytes(Path.of(args[0])));
        for (PolarChunk c : w.chunks()) chunks.put(key(c.x(), c.z()), c);
        String mode = args[1];
        if (mode.equals("extent")) {
            int minX = 1 << 30, maxX = -(1 << 30), minZ = 1 << 30, maxZ = -(1 << 30);
            for (PolarChunk c : w.chunks()) { minX = Math.min(minX, c.x()); maxX = Math.max(maxX, c.x()); minZ = Math.min(minZ, c.z()); maxZ = Math.max(maxZ, c.z()); }
            System.out.println("chunks " + w.chunks().size() + " x " + minX * 16 + ".." + (maxX * 16 + 15) + " z " + minZ * 16 + ".." + (maxZ * 16 + 15) + " minSection " + w.minSection());
        } else if (mode.equals("top")) {
            int x0 = Integer.parseInt(args[2]), x1 = Integer.parseInt(args[3]), z0 = Integer.parseInt(args[4]), z1 = Integer.parseInt(args[5]), step = Integer.parseInt(args[6]);
            for (int z = z0; z <= z1; z += step) {
                StringBuilder sb = new StringBuilder(String.format("%5d ", z));
                for (int x = x0; x <= x1; x += step) {
                    int top = -999;
                    for (int y = 200; y >= 0; y--) { if (solid(block(x, y, z))) { top = y; break; } }
                    sb.append(top < 0 ? ' ' : (char) ('0' + Math.min(top / 10, 9)));
                }
                System.out.println(sb);
            }
        } else if (mode.equals("surface")) {
            int x0 = Integer.parseInt(args[2]), x1 = Integer.parseInt(args[3]), z0 = Integer.parseInt(args[4]), z1 = Integer.parseInt(args[5]), y0 = Integer.parseInt(args[6]), y1 = Integer.parseInt(args[7]), step = Integer.parseInt(args[8]);
            for (int[] p : standable(x0, x1, z0, z1, y0, y1, step)) System.out.println(p[0] + " " + p[1] + " " + p[2] + " " + block(p[0], p[1] - 1, p[2]));
        } else if (mode.equals("compare")) {
            PolarWorld other = PolarReader.read(Files.readAllBytes(Path.of(args[2])));
            Map<Long, PolarChunk> otherChunks = new HashMap<>();
            for (PolarChunk c : other.chunks()) otherChunks.put(key(c.x(), c.z()), c);
            int both = 0, onlyThis = 0, onlyOther = 0;
            long same = 0, differ = 0;
            for (PolarChunk c : w.chunks()) {
                if (!otherChunks.containsKey(key(c.x(), c.z()))) { onlyThis++; continue; }
                both++;
                for (int x = 0; x < 16; x += 2) for (int z = 0; z < 16; z += 2) for (int y = 0; y < 200; y += 3) {
                    int bx = c.x() * 16 + x, bz = c.z() * 16 + z;
                    String a = blockIn(w, chunks, bx, y, bz);
                    String b = blockIn(other, otherChunks, bx, y, bz);
                    if (air(a) == air(b)) same++; else differ++;
                }
            }
            for (Long k : otherChunks.keySet()) if (!chunks.containsKey(k)) onlyOther++;
            System.out.println("this " + w.chunks().size() + " other " + other.chunks().size() + " both " + both + " onlyThis " + onlyThis + " onlyOther " + onlyOther + " sameAirness " + same + " differ " + differ);
        } else if (mode.equals("col")) {
            int x = Integer.parseInt(args[2]), z = Integer.parseInt(args[3]);
            String last = "";
            for (int y = 250; y >= -60; y--) {
                String b = block(x, y, z);
                if (b == null) b = "NULL";
                if (!b.equals(last)) { System.out.println(y + " " + b); last = b; }
            }
        } else if (mode.equals("find")) {
            int x0 = Integer.parseInt(args[2]), x1 = Integer.parseInt(args[3]), y0 = Integer.parseInt(args[4]), y1 = Integer.parseInt(args[5]), z0 = Integer.parseInt(args[6]), z1 = Integer.parseInt(args[7]);
            Set<String> names = new HashSet<>(Arrays.asList(args).subList(8, args.length));
            for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) {
                String b = block(x, y, z);
                if (b != null && names.contains(b)) System.out.println(b + " " + x + " " + y + " " + z);
            }
        }
    }
}
