package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzleFactory;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.RoomBlock;
import dev.daweapon.dungeons.catacombs.puzzle.RoomSpot;
import dev.daweapon.dungeons.catacombs.puzzle.teleportmaze.MazePad;
import dev.daweapon.dungeons.catacombs.puzzle.teleportmaze.TeleportMaze;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Teleport Maze (both F1 captures): stepping on an end portal frame pad sends the player beside the pad it is joined
 * to, turned toward the one pad that leads to the centre. Reaching the centre lets its blessing chest open; the centre
 * pad leads back out to the entrance. The joins are dealt fresh every run.
 */
public final class TeleportMazeRoom extends PuzzleRoom {
    private static final double LANDING_HEIGHT = 0.5;
    private static final long PAD_COOLDOWN_MILLIS = 500;
    private static final Sound TELEPORT = Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.HOSTILE, 1f, 1f);

    private final PuzzleCatalogue.TeleportMazeRoom data;
    private final Map<Long, MazePad> padsByBlock = new HashMap<>();
    private final Map<UUID, Long> lastTeleport = new ConcurrentHashMap<>();
    private TeleportMaze maze;
    private PuzzleChest chest;
    private int padY;

    public TeleportMazeRoom(PuzzleContext context, PlacedRoom room, PuzzleCatalogue.TeleportMazeRoom data) {
        super(context, room, CatacombsPuzzle.TELEPORT_MAZE);
        this.data = data;
    }

    @Override
    public void start() {
        maze = TeleportMaze.deal(data.pads(), ThreadLocalRandom.current());
        for (MazePad pad : data.pads()) {
            int[] at = frame.block(new RoomBlock(pad.x(), data.y(), pad.z()));
            padY = at[1];
            padsByBlock.put(RoomFrame.key(at[0], 0, at[2]), pad);
        }
        chest = chest(frame.block(data.chest()));
    }

    @Override
    public void moved(SkyBlockPlayer player, Pos to, boolean onGround) {
        if (to.blockY() != padY) return;
        MazePad pad = padsByBlock.get(RoomFrame.key(to.blockX(), 0, to.blockZ()));
        if (pad == null) return;
        long now = System.currentTimeMillis();
        Long last = lastTeleport.get(player.getUuid());
        if (last != null && now - last < PAD_COOLDOWN_MILLIS) return;
        lastTeleport.put(player.getUuid(), now);
        step(player, pad);
    }

    private void step(SkyBlockPlayer player, MazePad pad) {
        if (pad.kind() == MazePad.Kind.CENTRE) {
            arrive(player, frame.spot(data.exit()));
            return;
        }
        MazePad to = maze.partner(pad);
        Pos landing = landing(to);
        if (to.kind() == MazePad.Kind.CENTRE) {
            Pos chestAt = frame.centre(data.chest());
            arrive(player, landing.withView(TeleportMaze.yawToward(landing.x(), landing.z(), chestAt.x(), chestAt.z()), 0));
            reachedCentre();
            return;
        }
        Pos target = landing(maze.finalPad());
        arrive(player, landing.withView(TeleportMaze.yawToward(landing.x(), landing.z(), target.x(), target.z()), 0));
    }

    private Pos landing(MazePad pad) {
        return frame.spot(new RoomSpot(pad.landX(), data.y() + LANDING_HEIGHT, pad.landZ(), 0));
    }

    private void arrive(SkyBlockPlayer player, Pos at) {
        player.teleport(at);
        instance().playSound(TELEPORT, at.x(), at.y(), at.z());
    }

    private synchronized void reachedCentre() {
        if (chest.unlocked()) return;
        advance(CatacombsPuzzleFactory.CENTRE_REACHED);
        chest.unlock();
    }

    @Override
    public synchronized boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        if (!chest.at(x, y, z)) return false;
        chest.click(player);
        return true;
    }

    @Override
    public void solve(SkyBlockPlayer staff) {
        reachedCentre();
    }

    @Override
    public List<String> info() {
        MazePad last = maze.finalPad();
        MazePad first = maze.partner(maze.entry());
        return List.of("<7>Entry pad leads to room <e>" + first.room() + " <7>(pad " + first.x() + ", " + first.z() + ")",
                "<7>Final pad: <a>" + last.x() + ", " + last.z() + " <7>in room <e>" + last.room() + " <7>(template frame)");
    }
}
