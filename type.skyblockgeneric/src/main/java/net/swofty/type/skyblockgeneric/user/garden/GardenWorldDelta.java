package net.swofty.type.skyblockgeneric.user.garden;

import net.hollowcube.polar.PolarChunk;
import net.hollowcube.polar.PolarSection;
import net.hollowcube.polar.PolarWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class GardenWorldDelta {
    private static final int SECTION_BLOCKS = 4096;

    private GardenWorldDelta() {
    }

    public static PolarWorld changedChunks(PolarWorld template, PolarWorld current) {
        List<PolarChunk> changed = new ArrayList<>();
        for (PolarChunk chunk : current.chunks()) {
            PolarChunk original = template.chunkAt(chunk.x(), chunk.z());
            if (original == null || !sameBlocks(original, template, chunk, current)) {
                changed.add(chunk);
            }
        }
        return withChunks(current, changed);
    }

    public static PolarWorld overlay(PolarWorld template, PolarWorld stored) {
        Map<Long, PolarChunk> merged = new LinkedHashMap<>();
        template.chunks().forEach(chunk -> merged.put(key(chunk.x(), chunk.z()), chunk));
        stored.chunks().forEach(chunk -> merged.put(key(chunk.x(), chunk.z()), chunk));
        return withChunks(template, new ArrayList<>(merged.values()));
    }

    public static boolean sameBlocks(PolarChunk a, PolarWorld aWorld, PolarChunk b, PolarWorld bWorld) {
        if (!a.blockEntities().equals(b.blockEntities())) return false;
        if (!Arrays.equals(a.userData(), b.userData())) return false;
        int lowest = Math.min(aWorld.minSection(), bWorld.minSection());
        int highest = Math.max(aWorld.maxSection(), bWorld.maxSection());
        for (int y = lowest; y <= highest; y++) {
            PolarSection first = section(a, aWorld, y);
            PolarSection second = section(b, bWorld, y);
            if (!sameSection(first, second)) return false;
        }
        return true;
    }

    public static String normalise(String block) {
        String name = block.startsWith("minecraft:") ? block.substring(10) : block;
        int open = name.indexOf('[');
        if (open < 0) return isAir(name) ? "air" : name;
        String base = name.substring(0, open);
        if (isAir(base)) return "air";
        String properties = name.substring(open + 1, name.length() - 1);
        if (properties.isEmpty()) return base;
        Map<String, String> sorted = new TreeMap<>();
        for (String property : properties.split(",")) {
            int equals = property.indexOf('=');
            sorted.put(property.substring(0, equals), property.substring(equals + 1));
        }
        StringBuilder builder = new StringBuilder(base).append('[');
        sorted.forEach((property, value) -> builder.append(property).append('=').append(value).append(','));
        builder.setLength(builder.length() - 1);
        return builder.append(']').toString();
    }

    private static boolean sameSection(PolarSection a, PolarSection b) {
        boolean aEmpty = a == null || a.isEmpty();
        boolean bEmpty = b == null || b.isEmpty();
        if (aEmpty && bEmpty) return true;
        if (aEmpty || bEmpty) return onlyAir(aEmpty ? b : a);
        if (Arrays.equals(a.blockPalette(), b.blockPalette()) && (a.blockPalette().length == 1 || Arrays.equals(a.blockData(), b.blockData()))) return true;
        for (int i = 0; i < SECTION_BLOCKS; i++) {
            if (!blockAt(a, i).equals(blockAt(b, i))) return false;
        }
        return true;
    }

    private static boolean onlyAir(PolarSection section) {
        for (int i = 0; i < SECTION_BLOCKS; i++) {
            if (!blockAt(section, i).equals("air")) return false;
        }
        return true;
    }

    private static String blockAt(PolarSection section, int index) {
        String[] palette = section.blockPalette();
        return normalise(palette.length == 1 ? palette[0] : palette[section.blockData()[index]]);
    }

    private static boolean isAir(String name) {
        return name.equals("air") || name.equals("cave_air") || name.equals("void_air");
    }

    private static PolarSection section(PolarChunk chunk, PolarWorld world, int y) {
        if (y < world.minSection() || y > world.maxSection()) return null;
        return chunk.sections()[y - world.minSection()];
    }

    private static PolarWorld withChunks(PolarWorld base, List<PolarChunk> chunks) {
        return new PolarWorld(base.version(), base.dataVersion(), base.compression(), base.minSection(),
                base.maxSection(), base.userData(), chunks);
    }

    private static long key(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
