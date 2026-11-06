import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Saves a box of a captured world as a Sponge schematic, trimmed to its non-air blocks. Used for the Floor 1 boss
 * room: {@code java ExportRegion.java <capture>/worlds/world_002/region out.schem -72 -72 -1 63 55 145}. The optional last
 * two arguments keep only that Y range, which drops the marker blocks Hypixel leaves at the bottom and top of the world.
 */
class ExportRegion {
    public static void main(String[] args) throws IOException {
        AnvilWorld world = new AnvilWorld(Path.of(args[0]));
        int x0 = Integer.parseInt(args[2]), z0 = Integer.parseInt(args[3]), x1 = Integer.parseInt(args[4]), z1 = Integer.parseInt(args[5]);
        int y0 = args.length > 7 ? Integer.parseInt(args[6]) : 0, y1 = args.length > 7 ? Integer.parseInt(args[7]) : 255;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = y0; y <= y1; y++) {
                    if (AnvilWorld.isAir(world.block(x, y, z))) continue;
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    minZ = Math.min(minZ, z);
                    maxZ = Math.max(maxZ, z);
                }
            }
        }
        int width = maxX - minX + 1, height = maxY - minY + 1, length = maxZ - minZ + 1;
        ExtractRooms.Box box = new ExtractRooms.Box(width, height, length, minY, new String[width * height * length], new HashMap<>());
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                for (int y = minY; y <= maxY; y++) {
                    int index = box.index(x, y, z);
                    box.states()[index] = world.block(minX + x, y, minZ + z);
                    Nbt.Tag entity = world.blockEntity(minX + x, y, minZ + z);
                    if (entity != null) box.entities().put(index, entity);
                }
            }
        }
        ExtractRooms.sanitize(box, false, new java.util.ArrayList<>());
        Files.write(Path.of(args[1]), ExtractRooms.schematic(box));
        System.out.printf("blocks %d..%d, %d..%d, %d..%d (%dx%dx%d)%n", minX, maxX, minY, maxY, minZ, maxZ, width, height, length);
    }
}
