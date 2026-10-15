package dev.daweapon.type.crystalhollows.claim;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.crystalhollows.config.Box;
import dev.daweapon.type.crystalhollows.layout.CrystalHollowsLayout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class ClaimWalls {
    static final int BLOCKS_PER_TICK = 24;

    private ClaimWalls() {
    }

    static void open(SkyBlockPlayer player, Instance instance, ClaimSpot spot) {
        if (spot.walls().isEmpty()) return;
        Pos origin = CrystalHollowsLayout.origin(spot.structure());
        Pos crystal = spot.crystalPosition();
        if (origin == null || crystal == null) return;
        List<Point> blocks = solidBlocks(instance, spot.wallsAt(origin));
        blocks.sort(Comparator.comparingDouble(block -> block.distanceSquared(crystal)));
        int[] next = {0};
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            int end = Math.min(blocks.size(), next[0] + BLOCKS_PER_TICK);
            for (; next[0] < end; next[0]++) player.sendPacket(new BlockChangePacket(blocks.get(next[0]), Block.AIR));
            return next[0] >= blocks.size() ? TaskSchedule.stop() : TaskSchedule.tick(1);
        });
    }

    static List<Point> solidBlocks(Instance instance, List<Box> boxes) {
        List<Point> blocks = new ArrayList<>();
        for (Box box : boxes) {
            for (int x = (int) Math.floor(box.minX()); x < box.maxX(); x++) {
                for (int y = (int) Math.floor(box.minY()); y < box.maxY(); y++) {
                    for (int z = (int) Math.floor(box.minZ()); z < box.maxZ(); z++) {
                        if (!instance.getBlock(x, y, z).isAir()) blocks.add(new Vec(x, y, z));
                    }
                }
            }
        }
        return blocks;
    }
}
