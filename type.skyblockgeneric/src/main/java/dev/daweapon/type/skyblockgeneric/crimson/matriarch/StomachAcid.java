package dev.daweapon.type.skyblockgeneric.crimson.matriarch;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The rising stomach acid, drawn only for the swallowed player: every open block of the Belly floods one layer at a
 * time from the pit upward, and drains back to the real world when the run ends.
 */
final class StomachAcid {
    static final int START_Y = 11;
    static final int MAX_Y = 18;
    static final int TICKS_PER_LAYER = 200;
    private static final int MIN_X = -556;
    private static final int MAX_X = -516;
    private static final int MIN_Z = -908;
    private static final int MAX_Z = -868;
    private static volatile Map<Integer, List<Point>> layers;

    private final SkyBlockPlayer player;
    private final Instance instance;
    private int floodedTo = START_Y - 1;

    StomachAcid(SkyBlockPlayer player, Instance instance) {
        this.player = player;
        this.instance = instance;
    }

    int surfaceLayer(int ticks) {
        return Math.min(MAX_Y, START_Y + ticks / TICKS_PER_LAYER);
    }

    boolean touches(Point feet, int ticks) {
        return feet.y() < surfaceLayer(ticks) + 0.9;
    }

    void rise(int ticks) {
        int target = surfaceLayer(ticks);
        while (floodedTo < target) {
            floodedTo++;
            send(layer(floodedTo), Block.WATER);
        }
    }

    void drain() {
        for (int y = START_Y; y <= floodedTo; y++) {
            List<SendablePacket> packets = new ArrayList<>();
            for (Point cell : layer(y)) packets.add(new BlockChangePacket(cell, instance.getBlock(cell)));
            player.sendPackets(packets);
        }
        floodedTo = START_Y - 1;
    }

    private void send(List<Point> cells, Block block) {
        List<SendablePacket> packets = new ArrayList<>(cells.size());
        for (Point cell : cells) packets.add(new BlockChangePacket(cell, block));
        player.sendPackets(packets);
    }

    private List<Point> layer(int y) {
        Map<Integer, List<Point>> cached = layers;
        if (cached == null) {
            cached = mapInterior(instance);
            layers = cached;
        }
        return cached.getOrDefault(y, List.of());
    }

    private static Map<Integer, List<Point>> mapInterior(Instance instance) {
        Map<Integer, List<Point>> result = new TreeMap<>();
        Set<Long> seen = new HashSet<>();
        Deque<int[]> open = new ArrayDeque<>();
        Point start = BellyRun.ENTRY;
        for (int y = START_Y; y <= start.blockY(); y++) open.add(new int[]{start.blockX(), y, start.blockZ()});
        int[][] steps = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        while (!open.isEmpty()) {
            int[] cell = open.poll();
            int x = cell[0], y = cell[1], z = cell[2];
            if (x < MIN_X || x > MAX_X || z < MIN_Z || z > MAX_Z || y < START_Y || y > MAX_Y) continue;
            if (!seen.add(key(x, y, z))) continue;
            if (!instance.getBlock(x, y, z).isAir()) continue;
            result.computeIfAbsent(y, ignored -> new ArrayList<>()).add(new Vec(x, y, z));
            for (int[] step : steps) open.add(new int[]{x + step[0], y + step[1], z + step[2]});
        }
        return result;
    }

    private static long key(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }
}
