package dev.daweapon.type.catacombs.secret;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.type.catacombs.arena.RoomMechanisms;
import dev.daweapon.type.catacombs.engine.DungeonHost;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * The crackable walls of the run's real rooms: a 2 wide, 4 high, 2 deep plug of cracked stone bricks (as captured in
 * M7 run 3, Deathmite) that a Superboom used on any of its blocks, or next to it, removes at once for good.
 */
public final class SuperboomWalls {
    public static final int REACH = 1;
    private static final Sound BLAST = Sound.sound(Key.key("entity.generic.explode"), Sound.Source.BLOCK, 1f, 0.984f);

    public record PlacedWall(PlacedRoom room, RoomMechanisms.Wall wall, AtomicBoolean blown) {
        boolean near(int x, int y, int z) {
            for (int[] at : wall.blocks()) {
                if (Math.abs(at[0] - x) <= REACH && Math.abs(at[1] - y) <= REACH && Math.abs(at[2] - z) <= REACH) return true;
            }
            return false;
        }
    }

    private final DungeonHost host;
    private final List<PlacedWall> walls;

    public SuperboomWalls(DungeonHost host, FloorLayout layout, RoomMechanisms mechanisms) {
        this.host = host;
        this.walls = placed(layout, host.arena().blocks()::isReal, mechanisms);
    }

    public static List<PlacedWall> placed(FloorLayout layout, Predicate<PlacedRoom> real, RoomMechanisms mechanisms) {
        List<PlacedWall> placed = new ArrayList<>();
        for (PlacedRoom room : layout.rooms()) {
            if (!real.test(room)) continue;
            for (RoomMechanisms.Wall wall : mechanisms.walls(room)) placed.add(new PlacedWall(room, wall, new AtomicBoolean()));
        }
        return List.copyOf(placed);
    }

    /** Blows the standing wall at or next to the block a Superboom was used on; false when there is none. */
    public boolean blast(int x, int y, int z) {
        PlacedWall wall = walls.stream().filter(each -> !each.blown().get() && each.near(x, y, z)).findFirst().orElse(null);
        if (wall == null || !wall.blown().compareAndSet(false, true)) return false;
        List<int[]> blocks = wall.wall().blocks();
        for (int index = 0; index < blocks.size(); index++) {
            int[] at = blocks.get(index);
            host.instance().setBlock(at[0], at[1], at[2], Block.AIR);
            if (index % 2 == 0) {
                host.instance().sendGroupedPacket(new ParticlePacket(Particle.EXPLOSION, at[0] + 0.5, at[1] + 0.5, at[2] + 0.5,
                        0.15f, 0.25f, 0.15f, 0f, 1));
            }
        }
        host.instance().sendGroupedPacket(new ParticlePacket(Particle.EXPLOSION_EMITTER, x + 0.5, y + 0.5, z + 0.5, 0f, 0f, 0f, 0f, 1));
        host.instance().playSound(BLAST, x + 0.5, y + 0.5, z + 0.5);
        return true;
    }

    public boolean isWallBlock(int x, int y, int z) {
        for (PlacedWall placed : walls) {
            for (int[] at : placed.wall().blocks()) {
                if (at[0] == x && at[1] == y && at[2] == z) return true;
            }
        }
        return false;
    }

    public List<PlacedWall> walls() {
        return walls;
    }
}
