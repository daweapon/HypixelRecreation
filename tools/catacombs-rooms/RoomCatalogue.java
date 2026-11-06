import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The community room catalogue (rooms.json and secretlocations.json from the Dungeon Rooms Mod data, as shipped by
 * github.com/yourboykyle/SecretRoutes, GPL-3.0). Read from a local download at extraction time and never copied into
 * the repository. A room is named by hashing the block column at its cell centre ("core"); its blue terracotta marker
 * gives the corner and rotation that the secret coordinates are relative to.
 */
final class RoomCatalogue {
    static final int CENTRE_START = -185;

    interface Blocks {
        String block(int x, int y, int z);
    }

    record Entry(String name, String type, String shape, List<Integer> cores, int crypts, int secrets) {
    }

    record Point(String name, String category, int x, int y, int z) {
    }

    record Found(Entry entry, String rotation, int cornerX, int cornerZ, int height, List<int[]> centres) {
        int[] toWorld(int x, int z) {
            return switch (rotation) {
                case "W" -> new int[]{cornerX - z, cornerZ + x};
                case "N" -> new int[]{cornerX - x, cornerZ - z};
                case "E" -> new int[]{cornerX + z, cornerZ - x};
                default -> new int[]{cornerX + x, cornerZ + z};
            };
        }
    }

    private final Map<Integer, Entry> byCore = new HashMap<>();
    private final Map<String, Entry> byName = new LinkedHashMap<>();
    private final Map<String, List<Point>> points = new HashMap<>();

    @SuppressWarnings("unchecked")
    RoomCatalogue(Path directory) throws IOException {
        for (Object raw : (List<Object>) Json.parse(Files.readString(directory.resolve("rooms.json")))) {
            Map<String, Object> room = (Map<String, Object>) raw;
            List<Integer> cores = new ArrayList<>();
            for (Object core : (List<Object>) room.getOrDefault("cores", List.of())) cores.add(((Double) core).intValue());
            Entry entry = new Entry((String) room.get("name"), (String) room.get("type"), (String) room.get("shape"), cores,
                    number(room.get("crypts")), number(room.get("secrets")));
            byName.put(entry.name(), entry);
            for (int core : cores) byCore.put(core, entry);
        }
        Map<String, Object> secrets = (Map<String, Object>) Json.parse(Files.readString(directory.resolve("secretlocations.json")));
        secrets.forEach((name, value) -> {
            if (!(value instanceof List<?> list)) return;
            List<Point> roomPoints = new ArrayList<>();
            for (Object raw : list) {
                Map<String, Object> point = (Map<String, Object>) raw;
                roomPoints.add(new Point((String) point.get("secretName"), ((String) point.get("category")).toLowerCase(),
                        number(point.get("x")), number(point.get("y")), number(point.get("z"))));
            }
            points.put(name, roomPoints);
        });
    }

    private static int number(Object value) {
        return value == null ? 0 : ((Double) value).intValue();
    }

    Map<String, Entry> entries() {
        return byName;
    }

    List<Point> points(String name) {
        return points.getOrDefault(name, List.of());
    }

    static int centre(int cell) {
        return CENTRE_START + 32 * cell;
    }

    Found identify(Blocks world, int centreX, int centreZ) {
        int height = topLayer(world, centreX, centreZ);
        Entry entry = byCore.get(core(world, centreX, centreZ, height));
        if (entry == null) return null;
        Set<Long> seen = new LinkedHashSet<>();
        List<int[]> centres = new ArrayList<>();
        collect(world, entry, centreX, centreZ, height, seen, centres);
        if (entry.name().equals("Fairy Room")) {
            return new Found(entry, "S", centres.getFirst()[0] - 15, centres.getFirst()[1] - 15, height, centres);
        }
        String[] names = {"N", "S", "W", "E"};
        int[][] offsets = {{15, 15}, {-15, -15}, {15, -15}, {-15, 15}};
        for (int r = 0; r < 4; r++) {
            for (int[] centre : centres) {
                int x = centre[0] + offsets[r][0], z = centre[1] + offsets[r][1];
                if (!blue(world.block(x, height, z))) continue;
                boolean valid = true;
                if (centres.size() > 1) {
                    int[][] around = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                    for (int[] step : around) {
                        String next = world.block(x + step[0], height, z + step[1]);
                        if (!blue(next) && !AnvilWorld.bare(next).equals(AnvilWorld.AIR)) valid = false;
                    }
                }
                if (valid) return new Found(entry, names[r], x, z, height, centres);
            }
        }
        return new Found(entry, null, 0, 0, height, centres);
    }

    private void collect(Blocks world, Entry entry, int x, int z, int height, Set<Long> seen, List<int[]> centres) {
        if (!seen.add(((long) x << 32) ^ (z & 0xFFFFFFFFL))) return;
        if (!entry.cores().contains(core(world, x, z, height))) return;
        centres.add(new int[]{x, z});
        collect(world, entry, x + 32, z, height, seen, centres);
        collect(world, entry, x - 32, z, height, seen, centres);
        collect(world, entry, x, z + 32, height, seen, centres);
        collect(world, entry, x, z - 32, height, seen, centres);
    }

    private static boolean blue(String state) {
        return AnvilWorld.bare(state).equals("minecraft:blue_terracotta");
    }

    static int topLayer(Blocks world, int x, int z) {
        for (int y = 160; y >= 12; y--) {
            String state = world.block(x, y, z);
            if (AnvilWorld.isAir(state)) continue;
            return AnvilWorld.bare(state).equals("minecraft:gold_block") ? y - 1 : y;
        }
        return 0;
    }

    static int core(Blocks world, int x, int z, int height) {
        int clamped = Math.max(11, Math.min(140, height));
        StringBuilder out = new StringBuilder(150);
        out.append("0".repeat(140 - clamped));
        int bedrock = 0;
        for (int y = clamped; y >= 12; y--) {
            String name = AnvilWorld.bare(world.block(x, y, z));
            if (name.equals(AnvilWorld.AIR) && bedrock >= 2 && y < 69) {
                out.append("0".repeat(y - 11));
                break;
            }
            if (name.equals("minecraft:bedrock")) {
                bedrock++;
            } else {
                bedrock = 0;
                if (name.equals("minecraft:oak_planks") || name.equals("minecraft:trapped_chest") || name.equals("minecraft:chest")) continue;
            }
            out.append("Block{").append(name).append('}');
        }
        return out.toString().hashCode();
    }
}
