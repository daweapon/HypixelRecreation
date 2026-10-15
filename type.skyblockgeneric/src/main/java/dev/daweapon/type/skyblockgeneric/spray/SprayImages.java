package dev.daweapon.type.skyblockgeneric.spray;

import net.minestom.server.map.MapColors;
import net.minestom.server.network.packet.server.play.MapDataPacket;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Turns a spray's 128 x 128 picture into map colours. Each spray and direction gets its own map id
 * from {@value #FIRST_MAP_ID} up, well clear of the Hub map wall's ids.
 */
public final class SprayImages {
    private static final int SIZE = 128;
    private static final int FIRST_MAP_ID = 30_000;
    private static final Path IMAGE_DIRECTORY = SprayCatalogue.DIRECTORY.resolve("sprays");
    private static final Map<String, MapDataPacket> PACKETS = new ConcurrentHashMap<>();
    private static final Map<String, Integer> MAP_IDS = new HashMap<>();

    private SprayImages() {}

    public static synchronized int mapId(Spray spray, SprayDirection direction) {
        Integer existing = MAP_IDS.get(key(spray, direction));
        if (existing != null) return existing;
        int id = FIRST_MAP_ID + MAP_IDS.size();
        MAP_IDS.put(key(spray, direction), id);
        return id;
    }

    public static MapDataPacket packet(Spray spray, SprayDirection direction) {
        return PACKETS.computeIfAbsent(key(spray, direction), ignored -> new MapDataPacket(mapId(spray, direction),
                (byte) 0, true, false, List.of(),
                new MapDataPacket.ColorContent((byte) SIZE, (byte) SIZE, (byte) 0, (byte) 0, colours(spray, direction))));
    }

    private static String key(Spray spray, SprayDirection direction) {
        return spray.id() + ":" + direction.name();
    }

    private static byte[] colours(Spray spray, SprayDirection direction) {
        BufferedImage image = read(spray);
        double radians = Math.toRadians(direction.getDegreesAnticlockwise());
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double centre = (SIZE - 1) / 2D;
        byte[] colours = new byte[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dx = x - centre;
                double dy = y - centre;
                int sourceX = (int) Math.round(centre + dx * cos - dy * sin);
                int sourceY = (int) Math.round(centre + dx * sin + dy * cos);
                if (sourceX < 0 || sourceY < 0 || sourceX >= SIZE || sourceY >= SIZE) continue;
                int argb = image.getRGB(sourceX, sourceY);
                if ((argb >>> 24) < 128) continue;
                colours[y * SIZE + x] = MapColors.closestColor(argb).getIndex();
            }
        }
        return colours;
    }

    private static BufferedImage read(Spray spray) {
        try {
            BufferedImage image = ImageIO.read(IMAGE_DIRECTORY.resolve(spray.image()).toFile());
            if (image == null || image.getWidth() != SIZE || image.getHeight() != SIZE) {
                throw new IllegalStateException("Spray picture " + spray.image() + " must be a 128x128 PNG");
            }
            return image;
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read spray picture " + spray.image(), e);
        }
    }
}
