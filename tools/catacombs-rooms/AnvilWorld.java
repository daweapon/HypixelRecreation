import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

final class AnvilWorld {
    static final String AIR = "minecraft:air";

    private record Section(String[] palette, int[] indices) {
    }

    private record Chunk(Map<Integer, Section> sections, Map<Long, Nbt.Tag> blockEntities) {
    }

    private final Path regionDirectory;
    private final Map<Long, Chunk> chunks = new HashMap<>();

    AnvilWorld(Path regionDirectory) {
        this.regionDirectory = regionDirectory;
    }

    String block(int x, int y, int z) {
        Chunk chunk = chunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        if (chunk == null) return AIR;
        Section section = chunk.sections().get(Math.floorDiv(y, 16));
        if (section == null) return AIR;
        if (section.indices() == null) return section.palette()[0];
        int index = (Math.floorMod(y, 16) << 8) | (Math.floorMod(z, 16) << 4) | Math.floorMod(x, 16);
        return section.palette()[section.indices()[index]];
    }

    Nbt.Tag blockEntity(int x, int y, int z) {
        Chunk chunk = chunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        return chunk == null ? null : chunk.blockEntities().get(key(x, y, z));
    }

    private static long key(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    private Chunk chunk(int chunkX, int chunkZ) {
        long id = ((long) chunkX << 32) ^ (chunkZ & 0xFFFFFFFFL);
        if (chunks.containsKey(id)) return chunks.get(id);
        Chunk chunk;
        try {
            chunk = load(chunkX, chunkZ);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read chunk " + chunkX + "," + chunkZ, e);
        }
        chunks.put(id, chunk);
        return chunk;
    }

    private Chunk load(int chunkX, int chunkZ) throws IOException {
        Path file = regionDirectory.resolve("r." + Math.floorDiv(chunkX, 32) + "." + Math.floorDiv(chunkZ, 32) + ".mca");
        if (!Files.exists(file)) return null;
        byte[] payload;
        int compression;
        try (RandomAccessFile region = new RandomAccessFile(file.toFile(), "r")) {
            int slot = Math.floorMod(chunkX, 32) + Math.floorMod(chunkZ, 32) * 32;
            region.seek(slot * 4L);
            int location = region.readInt();
            int sectorOffset = location >>> 8;
            if (sectorOffset == 0) return null;
            region.seek(sectorOffset * 4096L);
            int length = region.readInt();
            compression = region.readUnsignedByte();
            payload = new byte[length - 1];
            region.readFully(payload);
        }
        Nbt.Tag root = switch (compression) {
            case 1 -> Nbt.read(new GZIPInputStream(new ByteArrayInputStream(payload)));
            case 2 -> Nbt.read(new InflaterInputStream(new ByteArrayInputStream(payload)));
            case 3 -> Nbt.read(payload);
            default -> throw new IOException("Unsupported chunk compression " + compression);
        };
        Map<Integer, Section> sections = new HashMap<>();
        Nbt.Tag sectionList = root.get("sections");
        if (sectionList != null) {
            for (Nbt.Tag section : sectionList.list()) {
                Nbt.Tag states = section.get("block_states");
                if (states == null) continue;
                String[] palette = states.get("palette").list().stream().map(AnvilWorld::stateString).toArray(String[]::new);
                Nbt.Tag data = states.get("data");
                int[] indices = data == null ? null : unpack(data.longs(), palette.length);
                sections.put(((Number) section.get("Y").value()).intValue(), new Section(palette, indices));
            }
        }
        Map<Long, Nbt.Tag> entities = new HashMap<>();
        Nbt.Tag entityList = root.get("block_entities");
        if (entityList != null) {
            for (Nbt.Tag entity : entityList.list()) {
                entities.put(key(entity.get("x").intValue(), entity.get("y").intValue(), entity.get("z").intValue()), entity);
            }
        }
        return new Chunk(sections, entities);
    }

    private static int[] unpack(long[] data, int paletteSize) {
        int bits = Math.max(4, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
        int perLong = 64 / bits;
        long mask = (1L << bits) - 1;
        int[] indices = new int[4096];
        for (int i = 0; i < 4096; i++) {
            long word = data[i / perLong];
            indices[i] = (int) ((word >>> ((i % perLong) * bits)) & mask);
        }
        return indices;
    }

    static String stateString(Nbt.Tag entry) {
        String name = entry.get("Name").string();
        Nbt.Tag properties = entry.get("Properties");
        if (properties == null || properties.compound().isEmpty()) return name;
        StringBuilder out = new StringBuilder(name).append('[');
        boolean first = true;
        for (Map.Entry<String, Nbt.Tag> property : new TreeMap<>(properties.compound()).entrySet()) {
            if (!first) out.append(',');
            out.append(property.getKey()).append('=').append(property.getValue().string());
            first = false;
        }
        return out.append(']').toString();
    }

    static String bare(String state) {
        int bracket = state.indexOf('[');
        return bracket < 0 ? state : state.substring(0, bracket);
    }

    static boolean isAir(String state) {
        String name = bare(state);
        return name.equals(AIR) || name.equals("minecraft:cave_air") || name.equals("minecraft:void_air");
    }
}
