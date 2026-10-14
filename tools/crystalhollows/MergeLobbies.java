import net.hollowcube.polar.*;
import net.kyori.adventure.nbt.*;

import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class MergeLobbies {
    private static final int X0 = 192, Z0 = 192, SIZE = 640, Y0 = 0, HEIGHT = 192;
    private static final int MIN_SECTION = -4, MAX_SECTION = 19;
    private static final int[][] SIX = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    record Box(int x1, int y1, int z1, int x2, int y2, int z2) {
        boolean has(int x, int y, int z) {
            return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
        }

        Box shift(int dx, int dy, int dz) {
            return new Box(x1 + dx, y1 + dy, z1 + dz, x2 + dx, y2 + dy, z2 + dz);
        }
    }

    record Placement(int lobby, int tx, int ty, int tz) {
    }

    record Structure(String id, Box box, List<Placement> placements) {
        Box at(Placement placement) {
            return box.shift(placement.tx(), placement.ty(), placement.tz());
        }
    }

    static final class Lobby {
        final short[] states = new short[SIZE * SIZE * HEIGHT];
        final boolean[] covered = new boolean[(SIZE / 16) * (SIZE / 16)];
        final Map<Long, PolarChunk.BlockEntity> entities = new HashMap<>();
        final Map<Long, PolarChunk> chunks = new HashMap<>();
        byte[] thick;
    }

    static final List<String> NAMES = new ArrayList<>();
    static final Map<String, Short> IDS = new HashMap<>();
    static short AIR, STONE;

    static short id(String name) {
        return IDS.computeIfAbsent(name, n -> {
            NAMES.add(n);
            return (short) (NAMES.size() - 1);
        });
    }

    static boolean airName(String name) {
        return name.equals("minecraft:air") || name.equals("minecraft:cave_air") || name.equals("minecraft:void_air");
    }

    static int index(int x, int y, int z) {
        return ((y - Y0) * SIZE + (z - Z0)) * SIZE + (x - X0);
    }

    static boolean inside(int x, int y, int z) {
        return x >= X0 && x < X0 + SIZE && z >= Z0 && z < Z0 + SIZE && y >= Y0 && y < Y0 + HEIGHT;
    }

    static boolean isAir(short state) {
        return state == AIR || airIds.get(state);
    }

    static final BitSet airIds = new BitSet();

    public static void main(String[] args) throws Exception {
        Path config = Path.of(args[0]), outDir = Path.of(args[1]);
        net.minestom.server.MinecraftServer.init();
        AIR = id("minecraft:air");
        STONE = id("minecraft:stone");

        Map<Integer, Path> files = new TreeMap<>();
        Map<String, Structure> structures = new LinkedHashMap<>();
        List<Integer> priority = new ArrayList<>();
        List<Box> exempt = new ArrayList<>();
        for (String line : Files.readAllLines(config)) {
            String[] p = line.trim().split("\\s+");
            if (p.length < 2 || p[0].startsWith("#")) continue;
            switch (p[0]) {
                case "lobby" -> files.put(Integer.parseInt(p[1]), Path.of(p[2]));
                case "structure" -> structures.put(p[1], new Structure(p[1], box(p, 2), new ArrayList<>()));
                case "place" -> structures.get(p[1]).placements().add(new Placement(Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4]), Integer.parseInt(p[5])));
                case "priority" -> { for (int n = 1; n < p.length; n++) priority.add(Integer.parseInt(p[n])); }
                case "exempt" -> exempt.add(box(p, 1));
                default -> {
                }
            }
        }

        Map<Integer, Lobby> lobbies = new TreeMap<>();
        for (var entry : files.entrySet()) lobbies.put(entry.getKey(), load(entry.getValue()));
        for (int i = 0; i < NAMES.size(); i++) if (airName(NAMES.get(i))) airIds.set(i);
        for (Lobby lobby : lobbies.values()) lobby.thick = thick(lobby);
        lobbies.forEach((k, v) -> {
            long thin = 0, air = 0;
            for (int i = 0; i < v.states.length; i++) {
                if (isAir(v.states[i])) {
                    air++;
                    if (v.thick[i] == 0) thin++;
                }
            }
            System.out.println("lobby " + k + " chunks " + v.chunks.size() + " air " + air + " thin air " + thin);
        });

        Lobby merged = new Lobby();
        Stats stats = new Stats();
        for (int cz = 0; cz < SIZE / 16; cz++) {
            for (int cx = 0; cx < SIZE / 16; cx++) {
                int ci = cz * (SIZE / 16) + cx;
                boolean any = false;
                for (Lobby lobby : lobbies.values()) any |= lobby.covered[ci];
                if (!any) continue;
                merged.covered[ci] = true;
                for (int y = Y0; y < Y0 + HEIGHT; y++) {
                    for (int lz = 0; lz < 16; lz++) {
                        for (int lx = 0; lx < 16; lx++) {
                            int x = X0 + cx * 16 + lx, z = Z0 + cz * 16 + lz;
                            int i = index(x, y, z);
                            int[] out = decide(lobbies, priority, ci, i, x, y, z, structures, exempt, stats);
                            merged.states[i] = (short) out[0];
                            if (out[1] != 0) {
                                PolarChunk.BlockEntity be = lobbies.get(out[1]).entities.get(pos(x, y, z));
                                if (be != null) merged.entities.put(pos(x, y, z), be);
                            }
                        }
                    }
                }
            }
        }
        System.out.println(stats);
        fillMissingChunks(merged);

        Files.createDirectories(outDir);
        PolarWorld world = build(merged, lobbies);
        Files.write(outDir.resolve("merged.polar"), PolarWriter.write(world));
        System.out.println("wrote merged.polar chunks " + world.chunks().size());

        Path schematics = outDir.resolve("schematics");
        Files.createDirectories(schematics);
        for (Structure structure : structures.values()) writeStructure(structure, lobbies, schematics);
        net.minestom.server.MinecraftServer.stopCleanly();
    }

    static Box box(String[] p, int at) {
        return new Box(Integer.parseInt(p[at]), Integer.parseInt(p[at + 1]), Integer.parseInt(p[at + 2]), Integer.parseInt(p[at + 3]), Integer.parseInt(p[at + 4]), Integer.parseInt(p[at + 5]));
    }

    static void fillMissingChunks(Lobby merged) {
        int[][] around = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int per = SIZE / 16;
        for (int cz = 0; cz < per; cz++) {
            for (int cx = 0; cx < per; cx++) {
                if (merged.covered[cz * per + cx]) continue;
                for (int[] d : around) {
                    int nx = cx + d[0], nz = cz + d[1];
                    if (nx < 0 || nz < 0 || nx >= per || nz >= per || !merged.covered[nz * per + nx]) continue;
                    for (int y = Y0; y < Y0 + HEIGHT; y++) {
                        for (int lz = 0; lz < 16; lz++) {
                            for (int lx = 0; lx < 16; lx++) {
                                int from = index(X0 + nx * 16 + lx, y, Z0 + nz * 16 + lz), to = index(X0 + cx * 16 + lx, y, Z0 + cz * 16 + lz);
                                merged.states[to] = merged.states[from];
                            }
                        }
                    }
                    merged.covered[cz * per + cx] = true;
                    System.out.println("filled missing chunk " + (X0 / 16 + cx) + "," + (Z0 / 16 + cz) + " from a neighbour");
                    break;
                }
            }
        }
    }

    static final class Stats {
        long filledThin, pairClean;

        public String toString() {
            return "mined holes restored or filled " + filledThin + ", structure-site voxels cleaned " + pairClean;
        }
    }

    static int[] decide(Map<Integer, Lobby> lobbies, List<Integer> priority, int ci, int i, int x, int y, int z,
                        Map<String, Structure> structures, List<Box> exempt, Stats stats) {
        Set<Integer> excluded = new HashSet<>();
        for (Structure structure : structures.values()) {
            for (Placement placement : structure.placements()) {
                if (structure.at(placement).has(x, y, z)) excluded.add(placement.lobby());
            }
        }
        if (!excluded.isEmpty()) stats.pairClean++;
        List<Integer> usable = new ArrayList<>();
        for (int k : priority) {
            if (lobbies.get(k).covered[ci] && !excluded.contains(k)) usable.add(k);
        }
        if (usable.isEmpty()) return new int[]{STONE, 0};
        int primary = usable.get(0);
        short state = lobbies.get(primary).states[i];
        if (!isAir(state) || lobbies.get(primary).thick[i] != 0) return new int[]{state, primary};
        for (int k : usable.subList(1, usable.size())) {
            short other = lobbies.get(k).states[i];
            if (!isAir(other)) {
                stats.filledThin++;
                return new int[]{other, k};
            }
        }
        boolean fillable = usable.size() == 1 && !has(exempt, x, y, z) && y > 36;
        if (fillable) {
            stats.filledThin++;
            return new int[]{STONE, 0};
        }
        return new int[]{AIR, 0};
    }

    static boolean has(List<Box> boxes, int x, int y, int z) {
        for (Box box : boxes) if (box.has(x, y, z)) return true;
        return false;
    }

    static long pos(int x, int y, int z) {
        return ((long) (x + 2048) << 40) | ((long) (y + 64) << 20) | (z + 2048);
    }

    static Lobby load(Path file) throws Exception {
        PolarWorld world = PolarReader.read(Files.readAllBytes(file));
        Lobby lobby = new Lobby();
        Arrays.fill(lobby.states, AIR);
        for (PolarChunk chunk : world.chunks()) {
            int cx = chunk.x() - X0 / 16, cz = chunk.z() - Z0 / 16;
            if (cx < 0 || cz < 0 || cx >= SIZE / 16 || cz >= SIZE / 16) continue;
            lobby.covered[cz * (SIZE / 16) + cx] = true;
            lobby.chunks.put(pos(chunk.x(), 0, chunk.z()), chunk);
            for (int s = 0; s < chunk.sections().length; s++) {
                PolarSection section = chunk.sections()[s];
                if (section.isEmpty()) continue;
                int baseY = (s + world.minSection()) * 16;
                if (baseY + 16 <= Y0 || baseY >= Y0 + HEIGHT) continue;
                String[] palette = section.blockPalette();
                short[] ids = new short[palette.length];
                for (int p = 0; p < palette.length; p++) ids[p] = id(palette[p]);
                int[] data = section.blockData();
                for (int n = 0; n < 4096; n++) {
                    int y = baseY + (n >> 8);
                    if (y < Y0 || y >= Y0 + HEIGHT) continue;
                    int x = chunk.x() * 16 + (n & 15), z = chunk.z() * 16 + ((n >> 4) & 15);
                    lobby.states[index(x, y, z)] = ids[data == null ? 0 : data[n]];
                }
            }
            for (PolarChunk.BlockEntity be : chunk.blockEntities()) {
                lobby.entities.put(pos(chunk.x() * 16 + be.x(), be.y(), chunk.z() * 16 + be.z()), be);
            }
        }
        return lobby;
    }

    static byte[] thick(Lobby lobby) {
        int n = lobby.states.length;
        byte[] air = new byte[n];
        for (int i = 0; i < n; i++) {
            int cx = ((i % SIZE) >> 4), cz = (((i / SIZE) % SIZE) >> 4);
            air[i] = (byte) (lobby.covered[cz * (SIZE / 16) + cx] && isAir(lobby.states[i]) ? 1 : 0);
        }
        byte[] eroded = filter(air, true);
        byte[] dilated = filter(eroded, false);
        for (int i = 0; i < n; i++) dilated[i] = (byte) (dilated[i] != 0 && air[i] != 0 ? 1 : 0);
        return dilated;
    }

    static byte[] filter(byte[] source, boolean min) {
        byte[] current = source;
        for (int axis = 0; axis < 3; axis++) {
            byte[] next = new byte[current.length];
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < SIZE; z++) {
                    for (int x = 0; x < SIZE; x++) {
                        int i = (y * SIZE + z) * SIZE + x;
                        int lo = axis == 0 ? x - 1 : axis == 1 ? z - 1 : y - 1;
                        int hi = axis == 0 ? x + 1 : axis == 1 ? z + 1 : y + 1;
                        int max = axis == 0 ? SIZE : axis == 1 ? SIZE : HEIGHT;
                        int stride = axis == 0 ? 1 : axis == 1 ? SIZE : SIZE * SIZE;
                        int here = axis == 0 ? x : axis == 1 ? z : y;
                        boolean result = min;
                        for (int k = lo; k <= hi; k++) {
                            boolean v = k >= 0 && k < max && current[i + (k - here) * stride] != 0;
                            if (min && !v) {
                                result = false;
                                break;
                            }
                            if (!min && v) {
                                result = true;
                                break;
                            }
                        }
                        next[i] = (byte) (result ? 1 : 0);
                    }
                }
            }
            current = next;
        }
        return current;
    }

    static PolarWorld build(Lobby merged, Map<Integer, Lobby> lobbies) {
        List<PolarChunk> chunks = new ArrayList<>();
        for (int cz = 0; cz < SIZE / 16; cz++) {
            for (int cx = 0; cx < SIZE / 16; cx++) {
                if (!merged.covered[cz * (SIZE / 16) + cx]) continue;
                int chunkX = X0 / 16 + cx, chunkZ = Z0 / 16 + cz;
                PolarChunk template = null;
                for (Lobby lobby : lobbies.values()) {
                    template = lobby.chunks.get(pos(chunkX, 0, chunkZ));
                    if (template != null) break;
                }
                PolarSection[] sections = new PolarSection[MAX_SECTION - MIN_SECTION + 1];
                for (int s = 0; s < sections.length; s++) {
                    int baseY = (s + MIN_SECTION) * 16;
                    PolarSection source = template != null && s < template.sections().length ? template.sections()[s] : null;
                    String[] biomes = source != null ? source.biomePalette() : new String[]{"minecraft:plains"};
                    int[] biomeData = source != null ? source.biomeData() : null;
                    if (baseY + 16 <= Y0 || baseY >= Y0 + HEIGHT) {
                        sections[s] = new PolarSection(new String[]{"minecraft:air"}, null, biomes, biomeData,
                                PolarSection.LightContent.MISSING, null, PolarSection.LightContent.MISSING, null);
                        continue;
                    }
                    Map<Short, Integer> local = new LinkedHashMap<>();
                    int[] data = new int[4096];
                    for (int n = 0; n < 4096; n++) {
                        int x = chunkX * 16 + (n & 15), z = chunkZ * 16 + ((n >> 4) & 15), y = baseY + (n >> 8);
                        short state = merged.states[index(x, y, z)];
                        data[n] = local.computeIfAbsent(state, k -> local.size());
                    }
                    String[] palette = new String[local.size()];
                    for (var entry : local.entrySet()) palette[entry.getValue()] = NAMES.get(entry.getKey());
                    sections[s] = new PolarSection(palette, palette.length == 1 ? null : data, biomes, biomeData,
                            PolarSection.LightContent.MISSING, null, PolarSection.LightContent.MISSING, null);
                }
                List<PolarChunk.BlockEntity> entities = new ArrayList<>();
                for (int lz = 0; lz < 16; lz++) {
                    for (int lx = 0; lx < 16; lx++) {
                        int x = chunkX * 16 + lx, z = chunkZ * 16 + lz;
                        for (int y = Y0; y < Y0 + HEIGHT; y++) {
                            PolarChunk.BlockEntity be = merged.entities.get(pos(x, y, z));
                            if (be != null) entities.add(new PolarChunk.BlockEntity(lx, y, lz, be.id(), be.data()));
                        }
                    }
                }
                chunks.add(new PolarChunk(chunkX, chunkZ, sections, entities, template == null ? null : template.heightmaps(), template == null ? null : template.userData()));
            }
        }
        return new PolarWorld((short) 7, 4790, PolarWorld.CompressionType.ZSTD, (byte) MIN_SECTION, (byte) MAX_SECTION, new byte[0], chunks);
    }

    static void writeStructure(Structure structure, Map<Integer, Lobby> lobbies, Path dir) throws Exception {
        Box box = structure.box();
        int w = box.x2() - box.x1() + 1, h = box.y2() - box.y1() + 1, l = box.z2() - box.z1() + 1;
        Map<String, Integer> palette = new LinkedHashMap<>();
        int[] indices = new int[w * h * l];
        List<CompoundBinaryTag> entities = new ArrayList<>();
        long restored = 0;
        for (int y = 0; y < h; y++) {
            for (int z = 0; z < l; z++) {
                for (int x = 0; x < w; x++) {
                    Map<Short, Integer> votes = new LinkedHashMap<>();
                    Map<Short, Placement> origin = new HashMap<>();
                    boolean anyAir = false;
                    for (Placement placement : structure.placements()) {
                        int px = box.x1() + x + placement.tx(), py = box.y1() + y + placement.ty(), pz = box.z1() + z + placement.tz();
                        short state = inside(px, py, pz) ? lobbies.get(placement.lobby()).states[index(px, py, pz)] : AIR;
                        if (isAir(state)) {
                            anyAir = true;
                            continue;
                        }
                        votes.merge(state, 1, Integer::sum);
                        origin.putIfAbsent(state, placement);
                    }
                    short pick = AIR;
                    Placement from = null;
                    int top = 0;
                    for (var entry : votes.entrySet()) {
                        if (entry.getValue() > top) {
                            top = entry.getValue();
                            pick = entry.getKey();
                            from = origin.get(pick);
                        }
                    }
                    if (anyAir && from != null) restored++;
                    String name = NAMES.get(pick);
                    int slot = palette.computeIfAbsent(name, k -> palette.size());
                    indices[(y * l + z) * w + x] = slot;
                    if (from != null) {
                        long at = pos(box.x1() + x + from.tx(), box.y1() + y + from.ty(), box.z1() + z + from.tz());
                        PolarChunk.BlockEntity be = lobbies.get(from.lobby()).entities.get(at);
                        if (be != null && be.data() != null) {
                            CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                            for (String key : be.data().keySet()) builder.put(key, be.data().get(key));
                            builder.put("Id", StringBinaryTag.stringBinaryTag(be.id()));
                            builder.put("Pos", IntArrayBinaryTag.intArrayBinaryTag(x, y, z));
                            entities.add(builder.build());
                        }
                    }
                }
            }
        }
        ByteArrayOutputStream blockData = new ByteArrayOutputStream();
        for (int value : indices) {
            int v = value;
            while ((v & 0xFFFFFF80) != 0) {
                blockData.write((v & 0x7F) | 0x80);
                v >>>= 7;
            }
            blockData.write(v);
        }
        CompoundBinaryTag.Builder paletteTag = CompoundBinaryTag.builder();
        for (var entry : palette.entrySet()) paletteTag.putInt(entry.getKey(), entry.getValue());
        ListBinaryTag.Builder<CompoundBinaryTag> list = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        entities.forEach(list::add);
        CompoundBinaryTag root = CompoundBinaryTag.builder()
                .putInt("Version", 2)
                .putInt("DataVersion", 4790)
                .putShort("Width", (short) w).putShort("Height", (short) h).putShort("Length", (short) l)
                .putIntArray("Offset", new int[]{0, 0, 0})
                .putInt("PaletteMax", palette.size())
                .put("Palette", paletteTag.build())
                .putByteArray("BlockData", blockData.toByteArray())
                .put("BlockEntities", list.build())
                .build();
        Path file = dir.resolve(structure.id() + ".schem");
        try (var out = new GZIPOutputStream(Files.newOutputStream(file))) {
            BinaryTagIO.writer().write(root, out);
        }
        System.out.println("schematic " + file.getFileName() + " " + w + "x" + h + "x" + l + " palette " + palette.size()
                + " block entities " + entities.size() + " placements " + structure.placements().size() + " restored " + restored);
    }
}
