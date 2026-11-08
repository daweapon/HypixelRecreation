package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import dev.daweapon.type.catacombs.map.DungeonMapView;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.map.MapColors;
import net.minestom.server.network.packet.server.play.MapDataPacket;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The "Your Score Summary" map Hypixel hands every player when a run is won (both F1 captures): slot 9 becomes a map of
 * the four score parts, the rank badge and the total, the player is switched to that slot and their view tilted 45
 * degrees down onto it. Art is cut from the captured map images in {@code configuration/skyblock/dungeons/score_map/}.
 */
public final class ScoreSummaryMap {
    static final Path DIRECTORY = Path.of("./configuration/skyblock/dungeons/score_map");
    static final int SIZE = 128;
    static final float PITCH = 45f;
    private static final int[] ROW_TOPS = {36, 55, 74, 93};
    private static final int ROW_RIGHT = 76;
    private static final int[] TOTAL_SLOTS = {92, 101, 110};
    private static final int TOTAL_TOP = 95;
    private static final int GLYPH_WIDTH = 7;
    private static final int ADVANCE = 9;
    private static final int BADGE_X = 81;
    private static final int BADGE_Y = 32;
    private static final AtomicInteger NEXT_ID = new AtomicInteger(41_000);

    private ScoreSummaryMap() {
    }

    public static void show(SkyBlockPlayer player, DungeonScoreBreakdown score) {
        byte[] colours;
        try {
            colours = colours(render(score, DIRECTORY));
        } catch (IOException | RuntimeException exception) {
            Logger.error(exception, "Could not draw the score summary map");
            return;
        }
        int mapId = NEXT_ID.getAndIncrement();
        player.sendPacket(new MapDataPacket(mapId, (byte) 0, true, false, List.of(),
                new MapDataPacket.ColorContent((byte) SIZE, (byte) SIZE, (byte) 0, (byte) 0, colours)));
        player.getInventory().setItemStack(DungeonMapView.SLOT, item(mapId));
        player.setHeldItemSlot((byte) DungeonMapView.SLOT);
        player.teleport(player.getPosition().withPitch(PITCH));
    }

    static ItemStack item(int mapId) {
        return ItemStacks.notEditable(ItemStacks.item(Material.FILLED_MAP, "<a><l>Your Score Summary"))
                .set(DataComponents.MAP_ID, mapId)
                .build();
    }

    static BufferedImage render(DungeonScoreBreakdown score, Path directory) throws IOException {
        BufferedImage map = read(directory.resolve("base.png"));
        if (map == null) throw new IOException("score map base missing in " + directory);
        int[] rows = {score.skill(), score.exploration(), score.speed(), score.bonus()};
        for (int row = 0; row < rows.length; row++) {
            String digits = Integer.toString(Math.max(0, rows[row]));
            int first = ROW_RIGHT - GLYPH_WIDTH + 1 - ADVANCE * (digits.length() - 1);
            for (int index = 0; index < digits.length(); index++) {
                paste(map, read(directory.resolve("white_" + digits.charAt(index) + ".png")), first + ADVANCE * index, ROW_TOPS[row]);
            }
        }
        BufferedImage badge = read(directory.resolve("badge_" + badgeName(score.rank()) + ".png"));
        if (badge != null) paste(map, badge, BADGE_X, BADGE_Y);
        String total = Integer.toString(Math.max(0, score.total()));
        int offset = TOTAL_SLOTS.length - total.length();
        for (int index = 0; index < total.length() && index + offset >= 0; index++) {
            paste(map, read(directory.resolve("gold_" + total.charAt(index) + ".png")), TOTAL_SLOTS[index + offset], TOTAL_TOP);
        }
        return map;
    }

    static String badgeName(DungeonScoreRank rank) {
        return rank == DungeonScoreRank.S_PLUS ? "S_PLUS" : rank.name();
    }

    private static @Nullable BufferedImage read(Path file) throws IOException {
        if (!Files.exists(file)) {
            Logger.warn("Score summary map art {} is missing", file);
            return null;
        }
        return ImageIO.read(file.toFile());
    }

    private static void paste(BufferedImage target, @Nullable BufferedImage piece, int left, int top) {
        if (piece == null) return;
        for (int y = 0; y < piece.getHeight(); y++) {
            for (int x = 0; x < piece.getWidth(); x++) {
                int argb = piece.getRGB(x, y);
                if ((argb >>> 24) == 0) continue;
                target.setRGB(left + x, top + y, argb);
            }
        }
    }

    static byte[] colours(BufferedImage picture) {
        byte[] colours = new byte[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) colours[y * SIZE + x] = MapColors.closestColor(picture.getRGB(x, y)).getIndex();
        }
        return colours;
    }
}
