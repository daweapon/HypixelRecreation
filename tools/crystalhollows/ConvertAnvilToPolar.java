import net.hollowcube.polar.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;

public class ConvertAnvilToPolar {
    private static final int TARGET_MIN_SECTION = -4;
    private static final int TARGET_MAX_SECTION = 19;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("Usage: <anvil-world-folder> <output.polar> [report-dir]");
        Path reportDir = args.length > 2 ? Path.of(args[2]) : null;
        net.minestom.server.MinecraftServer.init();
        PolarWorld source = AnvilPolar.anvilToPolar(Path.of(args[0]), ChunkSelector.all());
        System.out.println("anvil import: " + source.chunks().size() + " chunks, sections " + source.minSection() + ".." + source.maxSection()
                + ", version " + source.version() + ", dataVersion " + source.dataVersion() + ", " + source.compression());

        PolarWorld world = normalise(source);
        Files.createDirectories(Path.of(args[1]).toAbsolutePath().getParent());
        Files.write(Path.of(args[1]), PolarWriter.write(world));
        System.out.println("wrote " + Path.of(args[1]) + " (" + Files.size(Path.of(args[1])) + " bytes)");
        report(world, reportDir);
        net.minestom.server.MinecraftServer.stopCleanly();
    }

    private static PolarWorld normalise(PolarWorld source) {
        int from = source.minSection();
        int total = TARGET_MAX_SECTION - TARGET_MIN_SECTION + 1;
        List<PolarChunk> chunks = new ArrayList<>();
        for (PolarChunk chunk : source.chunks()) {
            PolarSection[] sections = new PolarSection[total];
            for (int i = 0; i < total; i++) {
                int y = TARGET_MIN_SECTION + i;
                int src = y - from;
                sections[i] = src >= 0 && src < chunk.sections().length ? chunk.sections()[src]
                        : new PolarSection(new String[]{"minecraft:air"}, null, new String[]{"minecraft:plains"}, null,
                        PolarSection.LightContent.MISSING, null, PolarSection.LightContent.MISSING, null);
            }
            chunks.add(new PolarChunk(chunk.x(), chunk.z(), sections, chunk.blockEntities(), chunk.heightmaps(), chunk.userData()));
        }
        return new PolarWorld(source.version(), source.dataVersion(), source.compression(), (byte) TARGET_MIN_SECTION, (byte) TARGET_MAX_SECTION, source.userData(), chunks);
    }

    private static void report(PolarWorld world, Path reportDir) throws Exception {
        int minCx = Integer.MAX_VALUE, maxCx = Integer.MIN_VALUE, minCz = Integer.MAX_VALUE, maxCz = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int blockEntities = 0;
        Map<String, Integer> beTypes = new TreeMap<>();
        Set<String> biomes = new TreeSet<>();
        Set<Long> present = new HashSet<>();
        Set<Long> nonEmpty = new HashSet<>();
        for (PolarChunk chunk : world.chunks()) {
            present.add(key(chunk.x(), chunk.z()));
            blockEntities += chunk.blockEntities().size();
            for (var be : chunk.blockEntities()) beTypes.merge(String.valueOf(be.id()), 1, Integer::sum);
            for (int s = 0; s < chunk.sections().length; s++) {
                PolarSection section = chunk.sections()[s];
                biomes.addAll(Arrays.asList(section.biomePalette()));
                if (section.isEmpty()) continue;
                String[] palette = section.blockPalette();
                int[] data = section.blockData();
                for (int i = 0; i < 4096; i++) {
                    String block = palette[data == null ? 0 : data[i]];
                    if (isAir(block)) continue;
                    int x = chunk.x() * 16 + (i & 15);
                    int z = chunk.z() * 16 + ((i >> 4) & 15);
                    int y = (s + world.minSection()) * 16 + (i >> 8);
                    minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                    minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
                    nonEmpty.add(key(chunk.x(), chunk.z()));
                }
            }
            minCx = Math.min(minCx, chunk.x()); maxCx = Math.max(maxCx, chunk.x());
            minCz = Math.min(minCz, chunk.z()); maxCz = Math.max(maxCz, chunk.z());
        }
        System.out.println("chunks " + world.chunks().size() + " (non-empty " + nonEmpty.size() + "), chunk x " + minCx + ".." + maxCx + ", z " + minCz + ".." + maxCz);
        System.out.println("non-air block bounds x " + minX + ".." + maxX + ", y " + minY + ".." + maxY + ", z " + minZ + ".." + maxZ);
        System.out.println("block entities " + blockEntities + " " + beTypes);
        System.out.println("biomes " + biomes);

        int missing = 0;
        int w = maxCx - minCx + 1, h = maxCz - minCz + 1;
        StringBuilder text = new StringBuilder();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int cz = minCz; cz <= maxCz; cz++) {
            for (int cx = minCx; cx <= maxCx; cx++) {
                boolean has = present.contains(key(cx, cz));
                if (!has) missing++;
                text.append(has ? (nonEmpty.contains(key(cx, cz)) ? '#' : '.') : ' ');
                image.setRGB(cx - minCx, cz - minCz, has ? (nonEmpty.contains(key(cx, cz)) ? 0x40c060 : 0x808080) : 0x101010);
            }
            text.append('\n');
        }
        System.out.println("missing chunks inside the bounding rectangle: " + missing + " of " + (w * h));
        if (reportDir != null) {
            Files.createDirectories(reportDir);
            Files.writeString(reportDir.resolve("coverage.txt"), "origin chunk x=" + minCx + " z=" + minCz + " ('#' chunk, '.' air-only chunk, ' ' missing)\n" + text);
            BufferedImage scaled = new BufferedImage(w * 4, h * 4, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < h * 4; y++) for (int x = 0; x < w * 4; x++) scaled.setRGB(x, y, image.getRGB(x / 4, y / 4));
            ImageIO.write(scaled, "png", reportDir.resolve("coverage.png").toFile());
        }
    }

    private static boolean isAir(String block) {
        String name = block.startsWith("minecraft:") ? block.substring(10) : block;
        return name.equals("air") || name.equals("cave_air") || name.equals("void_air");
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }
}
