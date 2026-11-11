import net.hollowcube.polar.*;
import net.minestom.server.instance.block.Block;

import java.nio.file.*;
import java.util.*;

public class CarnivalWorldPaste {
    record Box(int x1, int y1, int z1, int x2, int y2, int z2) {
        boolean has(int x, int y, int z) {
            return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
        }

        static Box parse(String text) {
            int[] v = Arrays.stream(text.split(",")).mapToInt(Integer::parseInt).toArray();
            return new Box(Math.min(v[0], v[3]), Math.min(v[1], v[4]), Math.min(v[2], v[5]),
                    Math.max(v[0], v[3]), Math.max(v[1], v[4]), Math.max(v[2], v[5]));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("""
                    Usage:
                      diff  <hub.polar> <capture-world-folder> <x1,y1,z1,x2,y2,z2>
                      paste <hub.polar> <capture-world-folder> <x1,y1,z1,x2,y2,z2> <out.polar>
                      extract <hub.polar> <capture-world-folder> <x1,y1,z1,x2,y2,z2> <out.polar>
                      render <hub.polar> <capture-world-folder> <x1,y1,z1,x2,y2,z2> <out-dir>
                      entities|column <hub.polar> <capture-world-folder> <x1,y1,z1,x2,y2,z2>
                    The capture may be an Anvil world folder or a .polar file.""");
            return;
        }
        net.minestom.server.MinecraftServer.init();
        PolarWorld hub = PolarReader.read(Files.readAllBytes(Path.of(args[1])));
        PolarWorld capture = args[2].endsWith(".polar") ? PolarReader.read(Files.readAllBytes(Path.of(args[2])))
                : AnvilPolar.anvilToPolar(Path.of(args[2]), ChunkSelector.all());
        Box box = Box.parse(args[3]);
        System.out.println("hub sections " + hub.minSection() + ".." + hub.maxSection() + ", capture sections "
                + capture.minSection() + ".." + capture.maxSection());
        switch (args[0]) {
            case "diff" -> diff(hub, capture, box);
            case "paste" -> {
                paste(hub, capture, box);
                Files.write(Path.of(args[4]), PolarWriter.write(hub));
                System.out.println("wrote " + args[4]);
            }
            case "extract" -> {
                List<PolarChunk> chunks = capture.chunks().stream()
                        .filter(chunk -> chunk.x() >= Math.floorDiv(box.x1(), 16) && chunk.x() <= Math.floorDiv(box.x2(), 16)
                                && chunk.z() >= Math.floorDiv(box.z1(), 16) && chunk.z() <= Math.floorDiv(box.z2(), 16))
                        .toList();
                Files.write(Path.of(args[4]), PolarWriter.write(new PolarWorld(capture.version(), capture.dataVersion(),
                        capture.compression(), capture.minSection(), capture.maxSection(), capture.userData(), chunks)));
                System.out.println("extracted " + chunks.size() + " chunks to " + args[4]);
            }
            case "entities" -> {
                for (PolarWorld world : List.of(hub, capture)) {
                    System.out.println(world == hub ? "hub:" : "capture:");
                    for (PolarChunk chunk : world.chunks()) {
                        for (PolarChunk.BlockEntity be : chunk.blockEntities()) {
                            int x = chunk.x() * 16 + be.x(), z = chunk.z() * 16 + be.z();
                            if (box.has(x, be.y(), z)) System.out.println("  " + x + "," + be.y() + "," + z + " " + be.id() + " "
                                    + blockAt(world, x, be.y(), z) + " " + String.valueOf(be.data()).substring(0, Math.min(300, String.valueOf(be.data()).length())));
                        }
                    }
                }
            }
            case "column" -> {
                PolarChunk lit = hub.chunkAt(Math.floorDiv(box.x1(), 16), Math.floorDiv(box.z1(), 16));
                System.out.println("hub light " + lit.sections()[4 - hub.minSection()].skyLightContent() + "/" + lit.sections()[4 - hub.minSection()].blockLightContent());
                for (int y = box.y2(); y >= box.y1(); y--) {
                    System.out.println(y + " hub=" + blockAt(hub, box.x1(), y, box.z1()) + " capture=" + blockAt(capture, box.x1(), y, box.z1()));
                }
            }
            case "render" -> {
                render(hub, box, Path.of(args[4], "hub.png"));
                render(capture, box, Path.of(args[4], "capture.png"));
            }
            default -> throw new IllegalArgumentException(args[0]);
        }
        net.minestom.server.MinecraftServer.stopCleanly();
    }

    static String blockAt(PolarWorld world, int x, int y, int z) {
        PolarChunk chunk = world.chunkAt(Math.floorDiv(x, 16), Math.floorDiv(z, 16));
        if (chunk == null) return null;
        int section = Math.floorDiv(y, 16) - world.minSection();
        if (section < 0 || section >= chunk.sections().length) return "minecraft:air";
        PolarSection s = chunk.sections()[section];
        int[] data = s.blockData();
        return s.blockPalette()[data == null ? 0 : data[index(x, y, z)]];
    }

    static int index(int x, int y, int z) {
        return (Math.floorMod(y, 16) << 8) | (Math.floorMod(z, 16) << 4) | Math.floorMod(x, 16);
    }

    static String normalise(String state) {
        if (state == null) return null;
        Block block = Block.fromState(state);
        return block == null ? state : block.stateId() + "";
    }

    static void diff(PolarWorld hub, PolarWorld capture, Box box) {
        int[] min = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        int[] max = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        int differing = 0, missing = 0;
        Map<String, Integer> added = new TreeMap<>();
        int columns = box.x2() - box.x1() + 1;
        int rows = box.z2() - box.z1() + 1;
        char[][] map = new char[rows][columns];
        for (char[] row : map) Arrays.fill(row, '.');
        for (int x = box.x1(); x <= box.x2(); x++) {
            for (int z = box.z1(); z <= box.z2(); z++) {
                int columnDiffs = 0;
                for (int y = box.y1(); y <= box.y2(); y++) {
                    String theirs = blockAt(capture, x, y, z);
                    if (theirs == null) {
                        missing++;
                        map[z - box.z1()][x - box.x1()] = ' ';
                        break;
                    }
                    String ours = blockAt(hub, x, y, z);
                    if (Objects.equals(normalise(ours), normalise(theirs))) continue;
                    differing++;
                    columnDiffs++;
                    added.merge(theirs.replaceAll("\\[.*", ""), 1, Integer::sum);
                    min[0] = Math.min(min[0], x); min[1] = Math.min(min[1], y); min[2] = Math.min(min[2], z);
                    max[0] = Math.max(max[0], x); max[1] = Math.max(max[1], y); max[2] = Math.max(max[2], z);
                }
                if (columnDiffs > 0) map[z - box.z1()][x - box.x1()] = columnDiffs > 9 ? '#' : (char) ('0' + columnDiffs);
            }
        }
        System.out.println("differing blocks " + differing + ", missing columns " + missing);
        System.out.println("difference bounds " + Arrays.toString(min) + " .. " + Arrays.toString(max));
        System.out.println("capture blocks in differences " + added);
        System.out.println("map (rows z " + box.z1() + ".." + box.z2() + ", columns x " + box.x1() + ".." + box.x2() + ")");
        for (int r = 0; r < rows; r++) System.out.printf("%5d %s%n", box.z1() + r, new String(map[r]));
    }

    static void render(PolarWorld world, Box box, Path out) throws Exception {
        int scale = 6;
        int width = box.x2() - box.x1() + 1, height = box.z2() - box.z1() + 1;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width * scale, height * scale, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        for (int x = box.x1(); x <= box.x2(); x++) {
            for (int z = box.z1(); z <= box.z2(); z++) {
                int rgb = 0;
                for (int y = box.y2(); y >= box.y1(); y--) {
                    String state = blockAt(world, x, y, z);
                    if (state == null || Block.fromState(state) == null || Block.fromState(state).isAir() || Block.fromState(state).compare(Block.LIGHT)) continue;
                    Block block = Block.fromState(state);
                    int base = block == null ? 0xff00ff : colour(block);
                    double shade = 0.55 + 0.45 * (y - box.y1()) / Math.max(1, box.y2() - box.y1());
                    rgb = ((int) (((base >> 16) & 255) * shade) << 16) | ((int) (((base >> 8) & 255) * shade) << 8) | (int) ((base & 255) * shade);
                    break;
                }
                g.setColor(new java.awt.Color(rgb));
                g.fillRect((x - box.x1()) * scale, (z - box.z1()) * scale, scale, scale);
            }
        }
        g.setColor(java.awt.Color.WHITE);
        for (int x = box.x1(); x <= box.x2(); x++) if (Math.floorMod(x, 10) == 0) g.drawString(String.valueOf(x), (x - box.x1()) * scale, 10);
        for (int z = box.z1(); z <= box.z2(); z++) if (Math.floorMod(z, 10) == 0) g.drawString(String.valueOf(z), 0, (z - box.z1()) * scale + 10);
        Files.createDirectories(out.getParent());
        javax.imageio.ImageIO.write(image, "png", out.toFile());
    }

    static int colour(Block block) {
        return (block.name().hashCode() * 0x2f1b37) & 0xffffff | 0x202020;
    }

    static void paste(PolarWorld hub, PolarWorld capture, Box box) {
        Set<String> unknown = new TreeSet<>();
        int changed = 0, entities = 0;
        for (int cx = Math.floorDiv(box.x1(), 16); cx <= Math.floorDiv(box.x2(), 16); cx++) {
            for (int cz = Math.floorDiv(box.z1(), 16); cz <= Math.floorDiv(box.z2(), 16); cz++) {
                PolarChunk target = hub.chunkAt(cx, cz);
                PolarChunk source = capture.chunkAt(cx, cz);
                if (target == null || source == null) throw new IllegalStateException("chunk " + cx + "," + cz + " missing");
                PolarSection[] sections = target.sections().clone();
                for (int s = 0; s < sections.length; s++) {
                    int baseY = (s + hub.minSection()) * 16;
                    if (baseY + 15 < box.y1() || baseY > box.y2()) continue;
                    PolarSection section = sections[s];
                    List<String> palette = new ArrayList<>(Arrays.asList(section.blockPalette()));
                    int[] data = section.blockData() == null ? new int[4096] : section.blockData().clone();
                    boolean touched = false;
                    for (int i = 0; i < 4096; i++) {
                        int x = cx * 16 + (i & 15), z = cz * 16 + ((i >> 4) & 15), y = baseY + (i >> 8);
                        if (!box.has(x, y, z)) continue;
                        String theirs = blockAt(capture, x, y, z);
                        if (Block.fromState(theirs) == null) {
                            unknown.add(theirs);
                            continue;
                        }
                        if (Objects.equals(normalise(palette.get(data[i])), normalise(theirs))) continue;
                        int id = palette.indexOf(theirs);
                        if (id < 0) {
                            palette.add(theirs);
                            id = palette.size() - 1;
                        }
                        data[i] = id;
                        touched = true;
                        changed++;
                    }
                    if (touched) sections[s] = compact(palette, data, section);
                }
                List<PolarChunk.BlockEntity> blockEntities = new ArrayList<>();
                for (PolarChunk.BlockEntity be : target.blockEntities()) {
                    if (!box.has(cx * 16 + be.x(), be.y(), cz * 16 + be.z())) blockEntities.add(be);
                }
                for (PolarChunk.BlockEntity be : source.blockEntities()) {
                    if (box.has(cx * 16 + be.x(), be.y(), cz * 16 + be.z())) {
                        blockEntities.add(be);
                        entities++;
                    }
                }
                hub.updateChunkAt(cx, cz, new PolarChunk(cx, cz, sections, blockEntities, target.heightmaps(), target.userData()));
            }
        }
        System.out.println("changed blocks " + changed + ", block entities pasted " + entities);
        if (!unknown.isEmpty()) System.out.println("skipped unknown states " + unknown);
    }

    static PolarSection compact(List<String> palette, int[] data, PolarSection old) {
        Map<Integer, Integer> remap = new LinkedHashMap<>();
        List<String> used = new ArrayList<>();
        int[] out = new int[4096];
        for (int i = 0; i < 4096; i++) {
            int id = data[i];
            Integer mapped = remap.get(id);
            if (mapped == null) {
                mapped = used.size();
                remap.put(id, mapped);
                used.add(palette.get(id));
            }
            out[i] = mapped;
        }
        return new PolarSection(used.toArray(String[]::new), used.size() == 1 ? null : out,
                old.biomePalette(), old.biomeData(), old.blockLightContent(), old.blockLight(),
                old.skyLightContent(), old.skyLight());
    }
}
