import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The interactive secret blocks of every room template, in the template's own frame (y above the dungeon floor):
 * the cracked stone brick plug each catalogue Superboom point marks, and the blocks each lever removes. Written to
 * {@code configuration/skyblock/dungeons/room_mechanisms.json}.
 *
 * <p>A lever's blocks come, in this order, from a captured pull ({@link #CAPTURED}), from secret guide videos
 * ({@link #SEEN}), or from the room's shape: the closed pockets of air near the lever or its secret that only a
 * one block thick plug separates from the open room. Those last ones are estimates and are marked so.
 */
final class Mechanisms {
    static final int FLOOR_Y = ExtractRooms.FLOOR_Y;
    static final int POCKET_REACH = 7;
    static final int MAX_POCKET = 400;
    static final int MAX_PLUG = 48;
    static final String CRACKED = "minecraft:cracked_stone_bricks";

    record Box3(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        List<int[]> blocks() {
            List<int[]> out = new ArrayList<>();
            for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++) out.add(new int[]{x, y, z});
            return out;
        }
    }

    /**
     * Lever pulls seen in the captures, per template and lever label, as template-frame boxes of the blocks that turned
     * to barrier and then air. m7_run1, Trap-Very-Hard-3 at cell (0,2), lever (-190, 66, -114): a 5 x 3 floor patch
     * and a 3 wide, 6 tall oak log wall (the room is turned a quarter in the template).
     */
    static final Map<String, List<Box3>> CAPTURED = Map.of(
            "trap|1 - Lever Behind Crypt", List.of(new Box3(3, -4, 8, 5, -4, 12), new Box3(3, 7, 7, 5, 12, 7)));

    /**
     * Levers whose door was seen in YouTube secret guides and then found in the template: the blocks of the listed kinds
     * inside the boxes. {@code freesSecret} is false where a second lever still stands between the door and the secret, or where
     * only gaps one block tall (too low for a player) join the secret's room to the rest.
     */
    record Seen(List<Box3> boxes, Set<String> kinds, String evidence, String confidence, boolean freesSecret) {
    }

    static final Map<String, Seen> SEEN = Map.of(
            "atlas|2 - Lever", new Seen(List.of(new Box3(6, 12, 24, 8, 15, 25)), Set.of("minecraft:oak_log"),
                    "82PZ4PdH0aA@38:48 pull, @38:56 walks up the spruce stairs through the open log doorway to 2 - Chest", "high", true),
            "museum|1 - Lever", new Seen(List.of(new Box3(29, 1, 47, 33, 9, 47)), Set.of("minecraft:oak_wood", "minecraft:spruce_stairs", "minecraft:oak_log"),
                    "82PZ4PdH0aA@40:35 pull, @40:42 through the wooden door between the magenta banners to 1 - Chest; door shut in 2RgXK8H4NII@0:00", "medium", false),
            "chambers|3 - Lever", new Seen(List.of(new Box3(34, 1, 3, 34, 4, 4)), Set.of("minecraft:spruce_log"),
                    "XeUPsiiy-vA@0:15.7 pull, @0:38 through the open 2 wide doorway at 3 - Entrance to 3 - Chest", "high", true),
            "hallway|2 - Lever", new Seen(List.of(new Box3(72, 1, 2, 73, 4, 3)), Set.of("minecraft:oak_log"),
                    "CvkfdhyqAIs@0:31 log door shut, @0:35 climbs to the lever, @0:39 through the open doorway to 2 - Chest", "high", true),
            "grass_ruin|3 - Lever 1", new Seen(List.of(new Box3(24, -6, 17, 38, 5, 17)), Set.of("minecraft:oak_wood", "minecraft:spruce_stairs", "minecraft:oak_log"),
                    "4zata5TZenI@0:01.5 pull at the top; the wooden door at 3 - Entrance 1 is the only way into the lower lever room", "medium", false),
            "grass_ruin|3 - Lever 2", new Seen(List.of(new Box3(26, -16, 19, 26, -10, 23)), Set.of("minecraft:iron_bars"),
                    "4zata5TZenI@0:11.6 pull, @0:12-0:13.4 through the iron bar window next to the lever to 3 - Chest; which of the two windows is a guess (the one by the lever)", "medium", false),
            "buttons|3 - Lever behind Crypt", new Seen(List.of(new Box3(55, 12, 15, 57, 20, 15)), Set.of("minecraft:oak_log"),
                    "NOXbttb-Zok@0:35 pull, @0:45-0:47 down the corridor behind the log wall to 3 - Chest", "medium", true),
            "deathmite|4 - Lever", new Seen(List.of(new Box3(67, -3, 14, 67, 2, 16), new Box3(66, -1, 15, 66, -1, 15)),
                    Set.of("minecraft:spruce_stairs", "minecraft:spruce_slab", "minecraft:oak_log", "minecraft:tripwire_hook"),
                    "wHSmClEOkK0@0:24 pull, @0:27 'You hear the sound of something opening...', @0:28.5 through the open 3 wide doorway (the Tripwire Hook on the door drops) and up the steps to 4 - Chest @0:30", "high", true),
            "rails|7 - Lever", new Seen(List.of(new Box3(21, -19, 53, 21, -14, 57)), Set.of("minecraft:cobblestone_wall"),
                    "Lk0rnjPEKZM@2:13 pull at world (37.9, 70, 109.3), @2:15.5-2:17 F3 track drops to y 49 and crosses x 21 at z 119-120 through the open cobblestone wall grille, @2:20 7 - Chest", "high", false),
            "trap_hard|2 - Lever", new Seen(List.of(new Box3(13, 13, 2, 13, 16, 5)), Set.of("minecraft:oak_log"),
                    "rtp6mN-RdSA@0:16.5 pull next to 1 - Chest, @0:17.5 'You hear the sound of something opening...', @0:24-0:25.5 open doorway beside the granite panel at the corridor end; also pulled in xc0NEWsdwLM@0:40. Which wall it opens is inferred: the only plug between the upper floor (2 - Chest, 3 - Bat) and the room", "medium", true));

    record Template(String id, String file, String shape, int sx, int sy, int sz, List<Map<String, Object>> sockets,
                    List<Map<String, Object>> anchors, List<Map<String, Object>> extras) {
    }

    record Lever(String label, int x, int y, int z, List<int[]> opens, String source, List<String> pairs, List<int[]> secretBlocks, boolean freesSecret,
                 String evidence, String confidence) {
        Lever(String label, int x, int y, int z, List<int[]> opens, String source, List<String> pairs, List<int[]> secretBlocks, boolean freesSecret) {
            this(label, x, y, z, opens, source, pairs, secretBlocks, freesSecret, null, null);
        }
    }

    record Plug(String label, List<int[]> blocks) {
    }

    private Mechanisms() {
    }

    @SuppressWarnings("unchecked")
    static void write(Path rooms, Path out, boolean verbose) throws IOException {
        Map<String, Object> report = new TreeMap<>();
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"source\": \"Generated by tools/catacombs-rooms/ExtractRooms.java --mechanisms from the room templates and their catalogue labels. ")
                .append("Template frame, y above the dungeon floor at 68. superbooms: the cracked stone brick plug at each catalogue Superboom point. ")
                .append("levers: the blocks a pull removes; source capture = seen in a captured Hypixel run, video = a door seen opening in YouTube secret guides (evidence: video id@time), ")
                .append("estimated = the one-block plugs of closed air pockets next to the lever or its secret.\",\n  \"rooms\": {");
        boolean firstRoom = true;
        for (Template template : templates(rooms)) {
            CaptureRooms.SchemBlocks blocks = CaptureRooms.SchemBlocks.read(rooms.resolve(template.file()), template.sx(), FLOOR_Y + template.sy(), template.sz());
            Room room = new Room(template, blocks);
            List<Plug> booms = new ArrayList<>();
            List<Lever> levers = new ArrayList<>();
            for (Map<String, Object> extra : template.extras()) {
                String kind = (String) extra.get("kind");
                String label = (String) extra.get("label");
                int x = number(extra.get("x")), y = number(extra.get("y")) + FLOOR_Y, z = number(extra.get("z"));
                if (kind.equals("SUPERBOOM") && !label.contains("Crypt")) {
                    List<int[]> plug = room.crackedPlug(x, y, z);
                    if (plug.isEmpty()) System.out.println(template.id() + " " + label + ": no cracked stone bricks near " + x + "," + (y - FLOOR_Y) + "," + z);
                    else booms.add(new Plug(label, plug));
                }
                if (kind.equals("LEVER")) levers.add(room.lever(label, x, y, z));
            }
            if (booms.isEmpty() && levers.isEmpty()) continue;
            json.append(firstRoom ? "\n" : ",\n").append("    \"").append(template.id()).append("\": {");
            firstRoom = false;
            List<String> parts = new ArrayList<>();
            if (!levers.isEmpty()) {
                StringBuilder part = new StringBuilder("\n      \"levers\": [");
                for (int i = 0; i < levers.size(); i++) {
                    Lever lever = levers.get(i);
                    part.append(i == 0 ? "\n" : ",\n").append(String.format(Locale.ROOT,
                            "        {\"label\": \"%s\", \"x\": %d, \"y\": %d, \"z\": %d, \"source\": \"%s\",%s \"secrets\": [%s], \"secretBlocks\": %s, \"freesSecret\": %s,\n          \"opens\": %s}",
                            lever.label(), lever.x(), lever.y() - FLOOR_Y, lever.z(), lever.source(),
                            lever.evidence() == null ? "" : " \"evidence\": \"" + lever.evidence() + "\", \"confidence\": \"" + lever.confidence() + "\",",
                            String.join(", ", lever.pairs().stream().map(pair -> "\"" + pair + "\"").toList()), blockList(lever.secretBlocks()), lever.freesSecret(), blockList(lever.opens())));
                    System.out.printf(Locale.ROOT, "%-18s %-24s %-9s %-5s %3d blocks %s%n", template.id(), lever.label(), lever.source(), lever.freesSecret(), lever.opens().size(), summary(room, lever.opens()));
                }
                parts.add(part.append("\n      ]").toString());
            }
            if (!booms.isEmpty()) {
                StringBuilder part = new StringBuilder("\n      \"superbooms\": [");
                for (int i = 0; i < booms.size(); i++) {
                    Plug plug = booms.get(i);
                    part.append(i == 0 ? "\n" : ",\n").append("        {\"label\": \"").append(plug.label()).append("\", \"blocks\": ").append(blockList(plug.blocks())).append("}");
                    if (verbose) System.out.printf(Locale.ROOT, "%-18s %-24s superboom %3d blocks %s%n", template.id(), plug.label(), plug.blocks().size(), size(plug.blocks()));
                }
                parts.add(part.append("\n      ]").toString());
            }
            json.append(String.join(",", parts)).append("\n    }");
        }
        json.append("\n  }\n}\n");
        Files.writeString(out, json.toString(), StandardCharsets.UTF_8);
    }

    private static String blockList(List<int[]> blocks) {
        List<String> items = new ArrayList<>();
        for (int[] at : blocks) items.add("[" + at[0] + ", " + (at[1] - FLOOR_Y) + ", " + at[2] + "]");
        return "[" + String.join(", ", items) + "]";
    }

    private static String summary(Room room, List<int[]> blocks) {
        Map<String, Integer> kinds = new TreeMap<>();
        for (int[] at : blocks) kinds.merge(AnvilWorld.bare(room.get(at[0], at[1], at[2])).replace("minecraft:", ""), 1, Integer::sum);
        return kinds + " " + size(blocks);
    }

    private static String size(List<int[]> blocks) {
        if (blocks.isEmpty()) return "";
        int[] lo = blocks.getFirst().clone(), hi = blocks.getFirst().clone();
        for (int[] at : blocks) for (int i = 0; i < 3; i++) {
            lo[i] = Math.min(lo[i], at[i]);
            hi[i] = Math.max(hi[i], at[i]);
        }
        return String.format(Locale.ROOT, "x %d..%d y %d..%d z %d..%d", lo[0], hi[0], lo[1] - FLOOR_Y, hi[1] - FLOOR_Y, lo[2], hi[2]);
    }

    @SuppressWarnings("unchecked")
    static List<Template> templates(Path rooms) throws IOException {
        List<Template> out = new ArrayList<>();
        for (String file : List.of("hypixel_f1.json", "hypixel_m7.json", "m7.json")) {
            Path path = rooms.resolve(file);
            if (!Files.exists(path)) continue;
            Map<String, Object> root = (Map<String, Object>) Json.parse(Files.readString(path));
            for (Object raw : (List<Object>) root.get("templates")) {
                Map<String, Object> t = (Map<String, Object>) raw;
                Map<String, Object> structure = (Map<String, Object>) t.get("structure");
                if (structure == null) continue;
                out.add(new Template((String) t.get("id"), (String) structure.get("file"), (String) t.get("shape"),
                        number(structure.get("x")), number(structure.get("y")), number(structure.get("z")),
                        (List<Map<String, Object>>) (Object) t.getOrDefault("doorSockets", List.of()),
                        (List<Map<String, Object>>) (Object) t.getOrDefault("secretAnchors", List.of()),
                        (List<Map<String, Object>>) (Object) t.getOrDefault("secretExtras", List.of())));
            }
        }
        out.sort(Comparator.comparing(Template::id));
        return out;
    }

    static int number(Object value) {
        return (int) Math.floor((Double) value);
    }

    static String number(String label) {
        return label.split(" ")[0];
    }

    /** Whether a lever label and a secret label share a catalogue number ("4/5 - Lever" pairs with "4 - Chest"). */
    static boolean paired(String lever, String secret) {
        Set<String> numbers = Set.of(number(lever).split("/"));
        for (String part : number(secret).split("/")) if (numbers.contains(part)) return true;
        return false;
    }

    static long key(int x, int y, int z) {
        return ExtractRooms.Analysis.pack(x, y, z);
    }

    static final int[][] SIX = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    /** One template's blocks with its open air worked out. */
    static final class Room {
        final Template template;
        final CaptureRooms.SchemBlocks blocks;
        final int[][] cells;
        final Set<Long> open = new HashSet<>();

        Room(Template template, CaptureRooms.SchemBlocks blocks) {
            this.template = template;
            this.cells = ExtractRooms.SHAPES.get(template.shape());
            String[] states = blocks.states().clone();
            for (Map<String, Object> socket : template.sockets()) {
                int col = number(socket.get("col")), row = number(socket.get("row"));
                int side = List.of(ExtractRooms.SIDES).indexOf((String) socket.get("side"));
                int[] out = {new int[]{0, 1, 0, -1}[side], new int[]{-1, 0, 1, 0}[side]};
                int edgeX = col * ExtractRooms.PITCH + (out[0] == 0 ? ExtractRooms.CENTRE : out[0] > 0 ? ExtractRooms.ROOM - 1 : 0);
                int edgeZ = row * ExtractRooms.PITCH + (out[1] == 0 ? ExtractRooms.CENTRE : out[1] > 0 ? ExtractRooms.ROOM - 1 : 0);
                for (int depth = 0; depth < DoorPieces.ROOM_DEPTH; depth++) {
                    for (int along = -1; along <= 1; along++) {
                        int x = edgeX - out[0] * depth - out[1] * along, z = edgeZ - out[1] * depth + out[0] * along;
                        for (int y = FLOOR_Y + 1; y <= FLOOR_Y + 4; y++) {
                            int bx = x - template.sx(), by = y - blocks.y0(), bz = z - template.sz();
                            if (bx < 0 || by < 0 || bz < 0 || bx >= blocks.width() || by >= blocks.height() || bz >= blocks.length()) continue;
                            states[bx + bz * blocks.width() + by * blocks.width() * blocks.length()] = AnvilWorld.AIR;
                        }
                    }
                }
            }
            this.blocks = new CaptureRooms.SchemBlocks(blocks.width(), blocks.height(), blocks.length(), blocks.x0(), blocks.y0(), blocks.z0(), states);
            ExtractRooms.Box box = new ExtractRooms.Box(blocks.width(), blocks.height(), blocks.length(), blocks.y0(), states, new HashMap<>());
            ExtractRooms.Analysis analysis = new ExtractRooms.Analysis(box, template.sx(), template.sz(), cells);
            Set<String> sockets = new HashSet<>();
            for (Map<String, Object> socket : template.sockets()) {
                sockets.add(number(socket.get("col")) + "," + number(socket.get("row")) + "," + List.of(ExtractRooms.SIDES).indexOf((String) socket.get("side")));
            }
            ArrayDeque<Long> queue = new ArrayDeque<>();
            for (long spot : analysis.reachable(sockets, new ArrayList<>())) {
                ExtractRooms.Spot at = ExtractRooms.Analysis.unpack(spot);
                for (int dy = 0; dy <= 1; dy++) if (open.add(key(at.x(), at.y() + dy, at.z()))) queue.add(key(at.x(), at.y() + dy, at.z()));
            }
            while (!queue.isEmpty()) {
                ExtractRooms.Spot at = ExtractRooms.Analysis.unpack(queue.poll());
                for (int[] step : SIX) {
                    int x = at.x() + step[0], y = at.y() + step[1], z = at.z() + step[2];
                    if (!inRoom(x, y, z) || !BlockStates.passable(get(x, y, z))) continue;
                    if (open.add(key(x, y, z))) queue.add(key(x, y, z));
                }
            }
        }

        String get(int x, int y, int z) {
            return blocks.block(x, y, z);
        }

        boolean inRoom(int x, int y, int z) {
            if (y < blocks.y0() || y >= blocks.y0() + blocks.height()) return false;
            if (x < template.sx() || z < template.sz() || x >= template.sx() + blocks.width() || z >= template.sz() + blocks.length()) return false;
            return x < 0 || z < 0 || ExtractRooms.containsCell(cells, x / ExtractRooms.PITCH, z / ExtractRooms.PITCH);
        }

        boolean cracked(int x, int y, int z) {
            return AnvilWorld.bare(get(x, y, z)).equals(CRACKED);
        }

        /** The cracked stone bricks around a Superboom point that hold together (each touching at least two others). */
        List<int[]> crackedPlug(int x, int y, int z) {
            int[] start = null;
            double best = Double.MAX_VALUE;
            for (int dx = -3; dx <= 3; dx++) for (int dy = -3; dy <= 3; dy++) for (int dz = -3; dz <= 3; dz++) {
                if (!cracked(x + dx, y + dy, z + dz)) continue;
                double distance = dx * dx + dy * dy + dz * dz;
                if (distance < best) {
                    best = distance;
                    start = new int[]{x + dx, y + dy, z + dz};
                }
            }
            if (start == null) return List.of();
            Map<Long, int[]> found = new LinkedHashMap<>();
            ArrayDeque<int[]> queue = new ArrayDeque<>();
            found.put(key(start[0], start[1], start[2]), start);
            queue.add(start);
            while (!queue.isEmpty() && found.size() < 200) {
                int[] at = queue.poll();
                for (int[] step : SIX) {
                    int[] next = {at[0] + step[0], at[1] + step[1], at[2] + step[2]};
                    if (Math.abs(next[0] - x) > 6 || Math.abs(next[1] - y) > 6 || Math.abs(next[2] - z) > 6) continue;
                    if (!cracked(next[0], next[1], next[2]) || found.containsKey(key(next[0], next[1], next[2]))) continue;
                    found.put(key(next[0], next[1], next[2]), next);
                    queue.add(next);
                }
            }
            boolean pruned = true;
            while (pruned) {
                pruned = false;
                for (Map.Entry<Long, int[]> entry : List.copyOf(found.entrySet())) {
                    int[] at = entry.getValue();
                    int touching = 0;
                    for (int[] step : SIX) if (found.containsKey(key(at[0] + step[0], at[1] + step[1], at[2] + step[2]))) touching++;
                    if (touching < 2 && found.size() > 1) {
                        found.remove(entry.getKey());
                        pruned = true;
                    }
                }
            }
            List<int[]> plug = new ArrayList<>(found.values());
            int[] lo = plug.getFirst().clone(), hi = plug.getFirst().clone();
            for (int[] at : plug) for (int i = 0; i < 3; i++) {
                lo[i] = Math.min(lo[i], at[i]);
                hi[i] = Math.max(hi[i], at[i]);
            }
            int volume = (hi[0] - lo[0] + 1) * (hi[1] - lo[1] + 1) * (hi[2] - lo[2] + 1);
            if (plug.size() < volume && plug.size() * 4 >= volume * 3) {
                plug.clear();
                for (int bx = lo[0]; bx <= hi[0]; bx++) for (int by = lo[1]; by <= hi[1]; by++) for (int bz = lo[2]; bz <= hi[2]; bz++) {
                    if (!BlockStates.passable(get(bx, by, bz))) plug.add(new int[]{bx, by, bz});
                }
            }
            plug.sort(Comparator.<int[]>comparingInt(at -> at[1]).thenComparingInt(at -> at[0]).thenComparingInt(at -> at[2]));
            return plug;
        }

        Lever lever(String label, int x, int y, int z) {
            int[] lever = {x, y, z};
            outer:
            for (int r = 0; r <= 2; r++) {
                for (int dx = -r; dx <= r; dx++) for (int dy = -r; dy <= r; dy++) for (int dz = -r; dz <= r; dz++) {
                    if (AnvilWorld.bare(get(x + dx, y + dy, z + dz)).equals("minecraft:lever")) {
                        lever = new int[]{x + dx, y + dy, z + dz};
                        break outer;
                    }
                }
            }
            List<String> pairs = new ArrayList<>();
            List<int[]> secrets = new ArrayList<>();
            for (Map<String, Object> anchor : template.anchors()) {
                String secret = (String) anchor.get("label");
                if (secret == null || !paired(label, secret)) continue;
                pairs.add(secret);
                secrets.add(new int[]{number(anchor.get("x")), number(anchor.get("y")) + FLOOR_Y, number(anchor.get("z"))});
            }
            String key = template.id() + "|" + label;
            Candidate chosen = choose(candidates(lever, secrets));
            if (CAPTURED.containsKey(key)) {
                if (chosen != null) System.out.println("  (shape guess for the captured " + key + ": " + size(chosen.plug()) + ")");
                return new Lever(label, lever[0], lever[1], lever[2], boxes(CAPTURED.get(key)), "capture", pairs, secrets, false);
            }
            if (SEEN.containsKey(key)) {
                Seen seen = SEEN.get(key);
                List<int[]> opens = boxes(seen.boxes()).stream().filter(at -> seen.kinds().contains(AnvilWorld.bare(get(at[0], at[1], at[2])))).toList();
                return new Lever(label, lever[0], lever[1], lever[2], opens, "video", pairs, secrets, seen.freesSecret(), seen.evidence(), seen.confidence());
            }
            if (chosen == null || (!chosen.holdsSecret() && chosen.gateShare() < 0.5)) {
                if (chosen != null) System.out.println("  (" + key + ": left shut, the nearest plug " + size(chosen.plug()) + " neither frees the secret nor is a gate)");
                return new Lever(label, lever[0], lever[1], lever[2], List.of(), "none", pairs, secrets, false);
            }
            List<int[]> opens = chosen.plug();
            if (chosen.gateShare() >= 0.5) opens = opens.stream().filter(at -> gate(get(at[0], at[1], at[2]))).toList();
            return new Lever(label, lever[0], lever[1], lever[2], opens, "estimated", pairs, secrets, chosen.holdsSecret());
        }

        private static List<int[]> boxes(List<Box3> boxes) {
            List<int[]> out = new ArrayList<>();
            for (Box3 box : boxes) for (int[] at : box.blocks()) out.add(new int[]{at[0], at[1] + FLOOR_Y, at[2]});
            return out;
        }

        record Candidate(List<int[]> plug, List<int[]> pocket, boolean holdsSecret, double gateShare, double distance) {
        }

        /** Every closed air pocket within reach of the lever or its secrets that a one-block plug separates from the open room. */
        List<Candidate> candidates(int[] lever, List<int[]> secrets) {
            List<int[]> points = new ArrayList<>(secrets);
            points.add(lever);
            Set<Long> visited = new HashSet<>();
            List<Candidate> out = new ArrayList<>();
            for (int[] point : points) {
                for (int dx = -POCKET_REACH; dx <= POCKET_REACH; dx++) for (int dy = -POCKET_REACH; dy <= POCKET_REACH; dy++) for (int dz = -POCKET_REACH; dz <= POCKET_REACH; dz++) {
                    int x = point[0] + dx, y = point[1] + dy, z = point[2] + dz;
                    if (!inRoom(x, y, z) || !BlockStates.passable(get(x, y, z)) || open.contains(key(x, y, z)) || visited.contains(key(x, y, z))) continue;
                    List<int[]> pocket = pocket(x, y, z, visited);
                    if (pocket.size() < 2 || pocket.size() > MAX_POCKET) continue;
                    List<int[]> plug = plug(pocket);
                    if (plug.isEmpty() || plug.size() > MAX_PLUG) continue;
                    boolean holds = false;
                    for (int[] secret : secrets) for (int[] air : pocket) holds |= Math.abs(air[0] - secret[0]) + Math.abs(air[1] - secret[1]) + Math.abs(air[2] - secret[2]) <= 1;
                    int gates = 0;
                    double distance = Double.MAX_VALUE;
                    for (int[] at : plug) {
                        if (gate(get(at[0], at[1], at[2]))) gates++;
                        distance = Math.min(distance, Math.sqrt(sq(at[0] - lever[0]) + sq(at[1] - lever[1]) + sq(at[2] - lever[2])));
                    }
                    out.add(new Candidate(plug, pocket, holds, gates / (double) plug.size(), distance));
                }
            }
            return out;
        }

        /** The pocket holding the lever's secret first, then the nearest iron-bar style gate, then the plug nearest the lever. */
        static Candidate choose(List<Candidate> candidates) {
            Comparator<Candidate> order = Comparator.<Candidate>comparingInt(candidate -> candidate.holdsSecret() ? 0 : 1)
                    .thenComparingInt(candidate -> candidate.gateShare() >= 0.5 ? 0 : 1)
                    .thenComparingDouble(Candidate::distance);
            return candidates.stream().min(order).orElse(null);
        }

        static boolean gate(String state) {
            String name = AnvilWorld.bare(state);
            return name.equals("minecraft:iron_bars") || name.endsWith("_fence") || name.endsWith("_pane") || name.endsWith("_trapdoor") || name.endsWith("_fence_gate");
        }

        static double sq(double value) {
            return value * value;
        }

        private List<int[]> pocket(int x, int y, int z, Set<Long> visited) {
            List<int[]> pocket = new ArrayList<>();
            ArrayDeque<int[]> queue = new ArrayDeque<>();
            visited.add(key(x, y, z));
            queue.add(new int[]{x, y, z});
            while (!queue.isEmpty()) {
                int[] at = queue.poll();
                pocket.add(at);
                if (pocket.size() > MAX_POCKET) return pocket;
                for (int[] step : SIX) {
                    int nx = at[0] + step[0], ny = at[1] + step[1], nz = at[2] + step[2];
                    if (!inRoom(nx, ny, nz) || !BlockStates.passable(get(nx, ny, nz)) || visited.contains(key(nx, ny, nz))) continue;
                    if (open.contains(key(nx, ny, nz))) return List.of();
                    visited.add(key(nx, ny, nz));
                    queue.add(new int[]{nx, ny, nz});
                }
            }
            return pocket;
        }

        /** The solid blocks of a pocket's shell that have open room air straight behind them, grouped by the largest face. */
        private List<int[]> plug(List<int[]> pocket) {
            Set<Long> inside = new HashSet<>();
            for (int[] at : pocket) inside.add(key(at[0], at[1], at[2]));
            Map<Integer, Map<Long, int[]>> byFace = new HashMap<>();
            for (int[] at : pocket) {
                for (int face = 0; face < SIX.length; face++) {
                    int[] step = SIX[face];
                    int sx = at[0] + step[0], sy = at[1] + step[1], sz = at[2] + step[2];
                    if (inside.contains(key(sx, sy, sz)) || BlockStates.passable(get(sx, sy, sz)) || cracked(sx, sy, sz)) continue;
                    if (!open.contains(key(sx + step[0], sy + step[1], sz + step[2]))) continue;
                    String name = AnvilWorld.bare(get(sx, sy, sz));
                    if (name.equals("minecraft:bedrock") || name.equals("minecraft:barrier") || name.contains("chest")) continue;
                    byFace.computeIfAbsent(face, ignored -> new LinkedHashMap<>()).putIfAbsent(key(sx, sy, sz), new int[]{sx, sy, sz});
                }
            }
            List<int[]> best = List.of();
            for (Map<Long, int[]> face : byFace.values()) if (face.size() > best.size()) best = new ArrayList<>(face.values());
            return best;
        }
    }
}
