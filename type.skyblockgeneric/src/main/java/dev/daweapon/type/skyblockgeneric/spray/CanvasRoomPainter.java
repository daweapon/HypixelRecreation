package dev.daweapon.type.skyblockgeneric.spray;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Marco's 32 x 32 wool canvas in the Canvas Room. Holding right-click with a Spray Can paints the
 * block the player aims at in their chosen palette colour; each player sees only their own painting
 * and no ink is used.
 */
public final class CanvasRoomPainter {
    private static final int MIN_X = 40;
    private static final int MAX_X = 71;
    private static final int CANVAS_MIN_Y = 22;
    private static final int CANVAS_MAX_Y = 53;
    private static final int CANVAS_Z = 45;
    private static final int PALETTE_MIN_Y = 17;
    private static final int PALETTE_MAX_Y = 18;
    private static final int PALETTE_Z = 46;
    private static final int PALETTE_SWATCH_WIDTH = 2;
    private static final double REACH = 48;
    private static final long STROKE_JOIN_MILLIS = 400;
    private static final List<Block> PALETTE = List.of(
            Block.BROWN_WOOL, Block.RED_WOOL, Block.ORANGE_WOOL, Block.YELLOW_WOOL,
            Block.LIME_WOOL, Block.GREEN_WOOL, Block.CYAN_WOOL, Block.LIGHT_BLUE_WOOL,
            Block.BLUE_WOOL, Block.PURPLE_WOOL, Block.MAGENTA_WOOL, Block.PINK_WOOL,
            Block.WHITE_WOOL, Block.LIGHT_GRAY_WOOL, Block.GRAY_WOOL, Block.BLACK_WOOL);

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    private CanvasRoomPainter() {}

    public static boolean isInCanvasRoom(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        return region != null && region.getType() == RegionType.CANVAS_ROOM;
    }

    public static boolean hasPainted(SkyBlockPlayer player) {
        Session session = SESSIONS.get(player.getUuid());
        return session != null && session.painted > 0;
    }

    public static void forget(UUID uuid) {
        SESSIONS.remove(uuid);
    }

    /** Returns true when the click landed on the palette and picked a colour. */
    public static boolean pickColour(SkyBlockPlayer player) {
        Optional<int[]> hit = aim(player, PALETTE_Z + 1);
        if (hit.isEmpty()) return false;
        int x = hit.get()[0];
        int y = hit.get()[1];
        if (y < PALETTE_MIN_Y || y > PALETTE_MAX_Y) return false;
        Block colour = PALETTE.get((x - MIN_X) / PALETTE_SWATCH_WIDTH);
        Session session = session(player);
        if (session.colour == colour) return true;
        session.colour = colour;
        player.sendMessage("<a>Selected " + colourName(colour) + "<a>!");
        return true;
    }

    /** Returns true when the player was aiming at the canvas. */
    public static boolean paint(SkyBlockPlayer player) {
        if (pickColour(player)) return true;
        Optional<int[]> hit = aim(player, CANVAS_Z + 1);
        if (hit.isEmpty()) return false;
        int x = hit.get()[0];
        int y = hit.get()[1];
        if (y < CANVAS_MIN_Y || y > CANVAS_MAX_Y) return false;

        Session session = session(player);
        long now = System.currentTimeMillis();
        if (now - session.lastStrokeAt <= STROKE_JOIN_MILLIS) {
            drawLine(player, session, session.lastX, session.lastY, x, y);
        } else {
            paintBlock(player, session, x, y);
        }
        session.lastX = x;
        session.lastY = y;
        session.lastStrokeAt = now;
        return true;
    }

    private static void drawLine(SkyBlockPlayer player, Session session, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int stepX = x0 < x1 ? 1 : -1;
        int stepY = y0 < y1 ? 1 : -1;
        int error = dx + dy;
        while (true) {
            paintBlock(player, session, x0, y0);
            if (x0 == x1 && y0 == y1) return;
            int doubled = 2 * error;
            if (doubled >= dy) {
                error += dy;
                x0 += stepX;
            }
            if (doubled <= dx) {
                error += dx;
                y0 += stepY;
            }
        }
    }

    private static void paintBlock(SkyBlockPlayer player, Session session, int x, int y) {
        player.sendPacket(new BlockChangePacket(new Vec(x, y, CANVAS_Z), session.colour.stateId()));
        session.painted++;
    }

    private static Optional<int[]> aim(SkyBlockPlayer player, double planeZ) {
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec direction = player.getPosition().direction();
        if (direction.z() >= 0) return Optional.empty();
        double distance = (planeZ - eye.z()) / direction.z();
        if (distance <= 0 || distance > REACH) return Optional.empty();
        int x = (int) Math.floor(eye.x() + direction.x() * distance);
        int y = (int) Math.floor(eye.y() + direction.y() * distance);
        if (x < MIN_X || x > MAX_X) return Optional.empty();
        return Optional.of(new int[]{x, y});
    }

    private static Session session(SkyBlockPlayer player) {
        return SESSIONS.computeIfAbsent(player.getUuid(), ignored -> new Session());
    }

    private static String colourName(Block wool) {
        String name = StringUtility.toNormalCase(wool.key().value().replace("_wool", ""));
        return "<f>" + name;
    }

    private static final class Session {
        private Block colour = Block.BLACK_WOOL;
        private int painted;
        private int lastX;
        private int lastY;
        private long lastStrokeAt;
    }
}
