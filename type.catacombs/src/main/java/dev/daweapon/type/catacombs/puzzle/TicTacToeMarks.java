package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeBoard.Mark;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.map.MapColors;
import net.minestom.server.network.packet.server.play.MapDataPacket;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The Tic Tac Toe marks: Hypixel's red X and the player's red O, drawn on maps in item frames as captured (map images
 * saved by the M7 run 2 capture, in {@code configuration/skyblock/dungeons/puzzles/}).
 */
public final class TicTacToeMarks {
    private static final Path DIRECTORY = Path.of("./configuration/skyblock/dungeons");
    private static final int SIZE = 128;
    private static final int HYPIXEL_MAP_ID = 39_000;
    private static final int PLAYER_MAP_ID = 39_001;
    private static final Map<Mark, MapDataPacket> PACKETS = new EnumMap<>(Mark.class);

    private TicTacToeMarks() {
    }

    public static synchronized MapDataPacket packet(Mark mark, String image) {
        return PACKETS.computeIfAbsent(mark, _ -> new MapDataPacket(mapId(mark), (byte) 0, true, false, List.of(),
                new MapDataPacket.ColorContent((byte) SIZE, (byte) SIZE, (byte) 0, (byte) 0, colours(image))));
    }

    static int mapId(Mark mark) {
        return mark == Mark.HYPIXEL ? HYPIXEL_MAP_ID : PLAYER_MAP_ID;
    }

    private static byte[] colours(String image) {
        byte[] colours = new byte[SIZE * SIZE];
        try {
            BufferedImage picture = ImageIO.read(DIRECTORY.resolve(image).toFile());
            if (picture == null || picture.getWidth() != SIZE || picture.getHeight() != SIZE) return colours;
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) colours[y * SIZE + x] = MapColors.closestColor(picture.getRGB(x, y)).getIndex();
            }
        } catch (IOException exception) {
            Logger.error(exception, "Could not read the Tic Tac Toe mark {}", image);
        }
        return colours;
    }

    public static Entity frame(MapDataPacket mark, Direction facing) {
        return new MarkFrame(mark, facing);
    }

    private static final class MarkFrame extends Entity {
        private final MapDataPacket mark;

        private MarkFrame(MapDataPacket mark, Direction facing) {
            super(EntityType.ITEM_FRAME);
            this.mark = mark;
            setNoGravity(true);
            editEntityMeta(ItemFrameMeta.class, meta -> {
                meta.setDirection(facing);
                meta.setItem(ItemStack.builder(Material.FILLED_MAP).set(DataComponents.MAP_ID, mark.mapId()).build());
            });
        }

        @Override
        public void updateNewViewer(@NotNull Player player) {
            player.sendPacket(mark);
            super.updateNewViewer(player);
        }
    }

    static Pos at(int[] block) {
        return new Pos(block[0] + 0.5, block[1], block[2] + 0.5);
    }
}
